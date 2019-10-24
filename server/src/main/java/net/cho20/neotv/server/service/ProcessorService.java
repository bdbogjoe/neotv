package net.cho20.neotv.server.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Movie;
import net.cho20.neotv.core.bean.Stream;
import net.cho20.neotv.core.service.JsonProcessor;
import net.cho20.neotv.core.service.M3uProcessor;
import net.cho20.neotv.core.service.Processor;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessorService {

    private Collection<Processor> processors = new ArrayList<>();
    private Iterable<Group> groups = Collections.emptyList();

    public ProcessorService(String code, String api, String groups) {
        this.processors.add(new M3uProcessor(code, api, groups.split(";")));
        this.processors.add(new JsonProcessor(309, "Cartoon FR"));
    }

    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void process() {
        groups = processors
                .stream()
                .map(Processor::process)
                .flatMap((Function<Iterable<Group>, java.util.stream.Stream<Group>>) gr -> StreamSupport.stream(gr.spliterator(), false))
                .collect(Collectors.toList());
    }


    public java.util.stream.Stream<Group> getGroups(String code) {
        return StreamSupport.stream(groups.spliterator(), false)
                .map(group ->
                        new Group(

                                group.getName(),
                                group.getType(),
                                StreamSupport.stream(group.getStreams().spliterator(), false)
                                        .map((Function<Stream, net.cho20.neotv.server.bean.Stream>) stream ->
                                            clone(code, stream)
                                        ).collect(Collectors.toList())
                        )
                )
                ;
    }

    private net.cho20.neotv.server.bean.Stream clone(String code, net.cho20.neotv.core.bean.Stream stream){
        net.cho20.neotv.server.bean.Stream out = new net.cho20.neotv.server.bean.Stream(stream.getTitle(), stream.buildUrl(code));
        if(stream instanceof Movie){
            if(((Movie) stream).getId_db()!=null){
                out.setImage("https://image.tmdb.org/t/p/w400"+((Movie) stream).getImage());
            }else {
                out.setImage(((Movie) stream).getImage());
            }
        }
        return out;
    }
}
