package net.cho20.neotv.server.service;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.script.bean.Group;
import net.cho20.neotv.script.bean.Stream;
import net.cho20.neotv.script.service.Processor;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessorService {

    private Processor processor;
    private Iterable<Group> groups;

    public ProcessorService(String code, String api, String groups) {
        this.processor = new Processor(code, api, groups.split(";"));
    }

    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void process() {
        groups = processor.process();
    }


    public java.util.stream.Stream<Group> getGroups(String code) {
        return StreamSupport.stream(groups.spliterator(), false)
                .map(group ->
                        new Group(

                                group.getName(),
                                group.getType(),
                                StreamSupport.stream(group.getStreams().spliterator(), false)
                                        .map((Function<Stream, net.cho20.neotv.server.bean.Stream>) stream ->
                                                new net.cho20.neotv.server.bean.Stream(stream.getTitle(), stream.buildUrl(code))
                                        )
                                        .collect(Collectors.toList())
                        )
                )
                ;
    }
}
