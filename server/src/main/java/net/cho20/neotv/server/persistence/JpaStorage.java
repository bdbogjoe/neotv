package net.cho20.neotv.server.persistence;

import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.cho20.neotv.core.bean.MovieAble;
import net.cho20.neotv.core.service.Storage;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class JpaStorage implements Storage {

    public JpaStorage(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    private MovieEntity convert(MovieAble entity){
        MovieEntity out = new MovieEntity();
        BeanUtils.copyProperties(entity, out, MovieAble.class);
        return out;
    }

    private final MovieRepository movieRepository;

    @Override
    public MovieAble find(String title) {
        return movieRepository.findByTitle(title);
    }

    @Override
    public Iterable<MovieAble> findAfter(Date date) {
        return movieRepository.findAllByDateAfter(date).stream().map((Function<MovieEntity, MovieAble>) movieEntity -> movieEntity).collect(Collectors.toList());
    }

    @Override
    public void insert(MovieAble movie) {
        movieRepository.saveAndFlush(convert(movie));
    }

    @Override
    public void update(MovieAble movie) {
        movieRepository.saveAndFlush(convert(movie));
    }


}
