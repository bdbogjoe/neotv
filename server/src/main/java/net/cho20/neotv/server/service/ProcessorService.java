package net.cho20.neotv.server.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.core.bean.*;
import net.cho20.neotv.core.service.JsonProcessor;
import net.cho20.neotv.core.service.M3uGroup;
import net.cho20.neotv.core.service.M3uProcessor;
import net.cho20.neotv.core.service.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ProcessorService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessorService.class);

    private Collection<Processor> processors = new ArrayList<>();
    private Iterable<Group<StreamBean>> groups = Collections.emptyList();

    public ProcessorService(StorageService storage, String code, String api, String groups) {
        this.processors.add(new M3uProcessor(storage, code, api, Arrays.stream(groups.split(";")).map(s -> {
            String[] tab = s.split(":");
            return new M3uGroup(tab[0], tab.length > 1 ? Arrays.stream(tab).skip(1).collect(Collectors.toList()) : null);
        }).collect(Collectors.toList())));
        this.processors.add(new JsonProcessor(storage, api, "VOD", Language.FR, Type.MOVIE, 126));
        this.processors.add(new JsonProcessor(storage, api, "VOD", Language.EN, Type.MOVIE, 343));
        this.processors.add(new JsonProcessor(storage, api, "Cartoons", Language.FR, Type.CARTOON, 309, 342));
        this.processors.add(new JsonProcessor(storage, api, "Cartoons", Language.EN, Type.CARTOON, 131));
    }


    @Scheduled(fixedDelay = 1 * 3600 * 1000)
    public void process() {
        try {
            LOG.info("Start loading goups");
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            groups = processors
                    .stream()
                    .map((Function<Processor, Iterable<Group<StreamBean>>>) processor -> {
                        try {
                            return processor.process(executorService);
                        } catch (Exception e) {
                            LOG.warn("Error while loading group", e);
                            return Collections.emptyList();
                        }
                    })
                    .flatMap((Function<Iterable<Group<StreamBean>>, java.util.stream.Stream<Group<StreamBean>>>) gr -> StreamSupport.stream(gr.spliterator(), false))
                    .collect(Collectors.toList());
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                LOG.info("Waiting..., remaining tasks : {}", ((ThreadPoolExecutor)executorService).getQueue().size());
                Thread.sleep(1000);
            }
            LOG.info("Groups loaded");
        } catch (Exception e) {
            LOG.error("Error while processing groups", e);
        }
    }


    public java.util.stream.Stream<Group<Map<String, String>>> getGroups(String code) {
        return StreamSupport.stream(groups.spliterator(), false)
                .map(group ->
                        new Group<>(
                                group.getName(),
                                group.getType(),
                                group.getLanguage(),
                                StreamSupport.stream(group.getStreams().spliterator(), false)
                                        .map(stream -> clone(code, stream))
                                        .collect(Collectors.toList())
                        )
                );
    }


    private Map<String, String> clone(String code, StreamBean stream) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("title", stream.getTitle());
        out.put("url", stream.buildUrl(code));
        String image = stream.getImage();
        if (stream instanceof MovieBean) {
            if (((MovieBean) stream).getTmdb() != null) {
                image = "https://image.tmdb.org/t/p/w400" + stream.getImage();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (((MovieBean) stream).getOverview() != null) {
                out.put("overview", ((MovieBean) stream).getOverview());
            }
            if (((MovieBean) stream).getDate() != null) {
                out.put("date", sdf.format(((MovieBean) stream).getDate()));
            }
            if (((MovieBean) stream).getPublish() != null) {
                out.put("publish", sdf.format(((MovieBean) stream).getPublish()));
            }
        }
        if (image != null) {
            out.put("image", image);
        }

        return out;
    }
}
