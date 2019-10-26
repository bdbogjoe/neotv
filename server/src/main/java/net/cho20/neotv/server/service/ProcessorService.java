package net.cho20.neotv.server.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Movie;
import net.cho20.neotv.core.bean.Stream;
import net.cho20.neotv.core.bean.Type;
import net.cho20.neotv.core.service.JsonProcessor;
import net.cho20.neotv.core.service.M3uProcessor;
import net.cho20.neotv.core.service.Processor;
import net.cho20.neotv.core.service.Storage;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessorService {

    private Collection<Processor> processors = new ArrayList<>();
    private Iterable<Group<Stream>> groups = Collections.emptyList();

    public ProcessorService(Storage storage, String code, String api, String groups) {
        this.processors.add(new M3uProcessor(storage,false, code, api, groups.split(";")));
        this.processors.add(new JsonProcessor(storage, 309, "Cartoon FR", Type.CARTOON));
    }


    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void process() {
        groups = processors
                .stream()
                .map(Processor::process)
                .flatMap((Function<Iterable<Group<Stream>>, java.util.stream.Stream<Group<Stream>>>) gr -> StreamSupport.stream(gr.spliterator(), false))
                .collect(Collectors.toList());
    }


    public java.util.stream.Stream<Group<Map<String, String>>> getGroups(String code) {
        return StreamSupport.stream(groups.spliterator(), false)
                .map(group ->
                        new Group<>(
                                group.getName(),
                                group.getType(),
                                StreamSupport.stream(group.getStreams().spliterator(), false)
                                        .sorted()
                                        .map(stream ->
                                                clone(code, stream)
                                        ).collect(Collectors.toList())
                        )
                );
    }

    private Map<String, String> clone(String code, net.cho20.neotv.core.bean.Stream stream){
        Map<String, String> out = new LinkedHashMap<>();
        out.put("title", stream.getTitle());
        out.put("url", stream.buildUrl(code));
        if(stream instanceof Movie){
            String image;
            if(((Movie) stream).getId_db()!=null){
               image = "https://image.tmdb.org/t/p/w400"+((Movie) stream).getImage();
            }else {
                image = ((Movie) stream).getImage();
            }
            if(image!=null) {
                out.put("image", image);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(((Movie) stream).getOverview()!=null) {
                out.put("overview", ((Movie) stream).getOverview());
            }
            if(((Movie) stream).getDate()!=null) {
                out.put("date", sdf.format(((Movie) stream).getDate()));
            }
            if(((Movie) stream).getPublish()!=null) {
                out.put("publish", sdf.format(((Movie) stream).getPublish()));
            }
        }
        return out;
    }
}
