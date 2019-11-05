package net.cho20.neotv.server.persistence;

import javax.transaction.Transactional;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.cho20.neotv.core.bean.Movie;
import net.cho20.neotv.core.bean.MovieVod;
import net.cho20.neotv.core.service.Storage;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class JpaStorage implements Storage {

    private final MovieRepository movieRepository;

    public JpaStorage(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public Movie find(String title) {
        return movieRepository.findByTitle(title);
    }

    @Override
    public Iterable<Movie> findAfter(Date date) {
        return movieRepository.findAllByPublishAfter(date).stream().map((Function<MovieEntity, Movie>) movieEntity -> movieEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void insert(Movie movie) {
        save(movie, true);
    }

    @Override
    @Transactional
    public void update(Movie movie) {
        save(movie, true);
    }

    private void save(Movie movie, boolean retry) {
        MovieEntity toSave = convert(movie);
        MovieEntity existing = movieRepository.findByTitle(movie.getTitle());
        if(existing!=null) {
            toSave.setPublish(existing.getPublish());
        }
        if (movie instanceof MovieVod && ((MovieVod) movie).getTmdb() != null) {
            MovieEntity found = movieRepository.findByTmdb(((MovieVod) movie).getTmdb());
            if (found!=null) {
                fill(movie, found);
                toSave=found;
            }
        }
        try {
            movieRepository.saveAndFlush(toSave);
        }catch(DataIntegrityViolationException e){
            if(retry) {
                save(movie, false);
            }else{
                throw e;
            }
        }
    }

    private void fill(Movie source, MovieEntity target) {
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(Movie.class)) {
            PropertyDescriptor targetPd = BeanUtils.getPropertyDescriptor(target.getClass(), pd.getName());
            if (targetPd != null && targetPd.getWriteMethod() != null) {
                try {
                    pd.getReadMethod().setAccessible(true);
                    Object value = pd.getReadMethod().invoke(source);
                    targetPd.getWriteMethod().setAccessible(true);
                    targetPd.getWriteMethod().invoke(target, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private MovieEntity convert(Movie entity) {
        MovieEntity out = new MovieEntity();
        fill(entity, out);
        return out;
    }


}
