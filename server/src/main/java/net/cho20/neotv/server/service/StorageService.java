package net.cho20.neotv.server.service;

import java.util.Date;

import net.cho20.neotv.core.bean.Movie;
import net.cho20.neotv.core.service.DbStorage;
import net.cho20.neotv.core.bean.MovieEntity;
import net.cho20.neotv.core.service.Storage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class StorageService implements InitializingBean, Storage {

    private Storage storage;


    @Override
    public void afterPropertiesSet() throws Exception {
        storage = new DbStorage();
    }

    @Override
    public MovieEntity find(String title) {
        return storage.find(title);
    }

    @Override
    public Iterable<MovieEntity> findAfter(Date date) {
        return storage.findAfter(date);
    }

    @Override
    public void insert(Movie movie) {
        storage.insert(movie);
    }

    @Override
    public void update(Movie movie) {
        storage.update(movie);
    }
}
