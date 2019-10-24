package net.cho20.neotv.server.controller;


import java.util.stream.Collectors;

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
}
