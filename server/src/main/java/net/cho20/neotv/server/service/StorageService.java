package net.cho20.neotv.server.service;

import java.util.Date;

import net.cho20.neotv.script.bean.Movie;
import net.cho20.neotv.script.service.Storage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class StorageService implements InitializingBean {

    private Storage storage;


    @Override
    public void afterPropertiesSet() throws Exception {
        storage = new Storage();
    }

    public Iterable<Movie> findAfter(Date date){
        return storage.findAfter(date);
    }
}
