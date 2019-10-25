package net.cho20.neotv.server.controller;


import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Type;
import net.cho20.neotv.server.service.ProcessorService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("rest")
public class RestController {

    private final ProcessorService processorService;

    public RestController(ProcessorService processorService) {
        this.processorService = processorService;
    }

    @RequestMapping("/all")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code) {
        return processorService.getGroups(code).collect(Collectors.toList());
    }

    @RequestMapping("/search")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code,@RequestParam(value = "type", required = false) String type,@RequestParam(value = "name") String name) {
        return filterByTypeAndName(code, type!=null?Type.valueOf(type):null, name);
    }

    @RequestMapping("/movies")
    public Iterable<net.cho20.neotv.core.bean.Group> movies(@RequestParam(value = "code") String code) {
        return filterByType(code, Type.MOVIE);
    }
    @RequestMapping("/cartoons")
    public Iterable<net.cho20.neotv.core.bean.Group> cartoons(@RequestParam(value = "code") String code) {
        return filterByType(code, Type.CARTOON);
    }
    @RequestMapping("/tv")
    public Iterable<net.cho20.neotv.core.bean.Group> tv(@RequestParam(value = "code") String code) {
        return filterByType(code, Type.TV);
    }

    @RequestMapping("/sport")
    public Iterable<net.cho20.neotv.core.bean.Group> sport(@RequestParam(value = "code") String code) {
        return filterByTypeAndName(code, Type.TV, "sport");
    }

    private Iterable<net.cho20.neotv.core.bean.Group> filterByType(String code, Type type) {
        return processorService.getGroups(code).filter(group -> group.getType()==type).collect(Collectors.toList());
    }
    private Iterable<net.cho20.neotv.core.bean.Group> filterByTypeAndName(String code, Type type, String... name) {
        return processorService.getGroups(code)
                .filter(group -> type==null || group.getType()==type)
                .map(mapGroup -> new Group<Map<String, String>>(
                        mapGroup.getName(),
                        mapGroup.getType(),
                        StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                .filter(stringStringMap -> Arrays.stream(name).anyMatch(s -> stringStringMap.get("title").toUpperCase().contains(s.toUpperCase())))
                                .collect(Collectors.toList())
            ))
                .filter(group-> group.getStreams().iterator().hasNext())
                .collect(Collectors.toList());
    }
}
