package net.cho20.neotv.server.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.MovieBean;
import net.cho20.neotv.core.bean.StreamBean;
import net.cho20.neotv.core.bean.Type;
import net.cho20.neotv.core.service.JsonProcessor;
import net.cho20.neotv.core.service.M3uProcessor;
import net.cho20.neotv.core.service.Processor;
import net.cho20.neotv.core.service.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessorService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorService.class);

    private Collection<Processor> processors = new ArrayList<>();
    private Iterable<Group<StreamBean>> groups = Collections.emptyList();

    public ProcessorService(StorageService storage, String code, String api, String groups) {
        this.processors.add(new M3uProcessor(storage,false, code, api, groups.split(";")));
        this.processors.add(new JsonProcessor(storage, 309, "Cartoon FR", Type.CARTOON));
    }


    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void process() {
        try {
            groups = processors
                    .stream()
                    .map(Processor::process)
                    .flatMap((Function<Iterable<Group<StreamBean>>, java.util.stream.Stream<Group<StreamBean>>>) gr -> StreamSupport.stream(gr.spliterator(), false))
                    .collect(Collectors.toList());
        }catch(Exception e){
            LOG.error("Error while processing groups", e);
        }
    }


    public java.util.stream.Stream<Group<Map<String, String>>> getGroups(String code) {
        return StreamSupport.stream(groups.spliterator(), false)
                .map(group ->
                        new Group<>(
                                group.getName(),
                                group.getType(),
                                StreamSupport.stream(group.getStreams().spliterator(), false)
                                        .map(stream -> clone(code, stream))
                                        .collect(Collectors.toList())
                        )
                );
    }




    private Map<String, String> clone(String code, StreamBean stream){
        Map<String, String> out = new LinkedHashMap<>();
        out.put("title", stream.getTitle());
        out.put("url", stream.buildUrl(code));
        String image = stream.getImage();
        if(stream instanceof MovieBean){
            if(((MovieBean) stream).getId_db()!=null){
               image = "https://image.tmdb.org/t/p/w400"+((MovieBean) stream).getImage();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(((MovieBean) stream).getOverview()!=null) {
                out.put("overview", ((MovieBean) stream).getOverview());
            }
            if(((MovieBean) stream).getDate()!=null) {
                out.put("date", sdf.format(((MovieBean) stream).getDate()));
            }
            if(((MovieBean) stream).getPublish()!=null) {
                out.put("publish", sdf.format(((MovieBean) stream).getPublish()));
            }
        }
        if(image!=null) {
            out.put("image", image);
        }

        return out;
    }
}
