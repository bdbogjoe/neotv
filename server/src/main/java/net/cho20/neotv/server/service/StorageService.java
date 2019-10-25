package net.cho20.neotv.server.service;

import java.util.Date;

import net.cho20.neotv.core.bean.Movie;
import net.cho20.neotv.core.service.DbStorage;
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
    public Movie find(String title) {
        return storage.find(title);
    }

    @Override
    public Iterable<Movie> findAfter(Date date) {
        return storage.findAfter(date);
    }

    @Override
    public void insert(Movie movie) {
        storage.insert(movie);
    }
}
