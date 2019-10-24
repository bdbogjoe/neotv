package net.cho20.neotv.server.controller;


import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public Iterable<net.cho20.neotv.core.bean.Group> filterByType(String code, Type type) {
        return processorService.getGroups(code).filter(group -> group.getType()==type).collect(Collectors.toList());
    }
}
