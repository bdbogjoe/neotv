package net.cho20.neotv.server.service;

import java.util.Date;

import net.cho20.neotv.core.bean.MovieAble;
import net.cho20.neotv.core.service.Storage;
import net.cho20.neotv.server.persistence.JpaStorage;
import org.springframework.stereotype.Component;

@Component
public class StorageService implements Storage {

    private final Storage storage;

    public StorageService(JpaStorage storage) {
        this.storage = storage;
    }

    @Override
    public MovieAble find(String title) {
        return storage.find(title);
    }

    @Override
    public Iterable<MovieAble> findAfter(Date date) {
        return storage.findAfter(date);
    }

    @Override
    public void insert(MovieAble movie) {
        storage.insert(movie);
    }

    @Override
    public void update(MovieAble movie) {
        storage.update(movie);
    }
}
