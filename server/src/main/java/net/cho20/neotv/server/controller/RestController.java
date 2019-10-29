package net.cho20.neotv.server.controller;


import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
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
    private String[] sorts = {"publish", "date", "title"};

    public RestController(ProcessorService processorService) {
        this.processorService = processorService;
    }


    @RequestMapping("/refresh")
    public void refresh() {
        processorService.process();
    }


    @RequestMapping("/all")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code) {
        return processorService.getGroups(code)
                .map(mapGroup -> new Group<Map<String, String>>(
                        mapGroup.getName(),
                        mapGroup.getType(),
                        StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                .sorted(new MapComparator(sorts))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @RequestMapping("/search")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code, @RequestParam(value = "type", required = false) String type, @RequestParam(value = "name") String name) {
        return filterByTypeAndName(code, type != null ? Type.valueOf(type) : null, name);
    }

    private Iterable<net.cho20.neotv.core.bean.Group> filterByTypeAndName(String code, Type type, String... name) {
        return processorService.getGroups(code)
                .filter(group -> type == null || group.getType() == type)
                .map(mapGroup -> new Group<Map<String, String>>(
                        mapGroup.getName(),
                        mapGroup.getType(),
                        StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                .filter(stringStringMap -> Arrays.stream(name).anyMatch(s -> stringStringMap.get("title").matches(s)))
                                .sorted(new MapComparator(sorts))
                                .collect(Collectors.toList())
                ))
                .filter(group -> group.getStreams().iterator().hasNext())
                .collect(Collectors.toList());
    }

    @RequestMapping("/movies")
    public Iterable<net.cho20.neotv.core.bean.Group> movies(@RequestParam(value = "code") String code) {
        return filterByType(code, Type.MOVIE);
    }

    private Iterable<net.cho20.neotv.core.bean.Group> filterByType(String code, Type type) {
        return processorService.getGroups(code)
                .filter(group -> group.getType() == type)
                .map(mapGroup -> new Group<Map<String, String>>(
                                mapGroup.getName(),
                                mapGroup.getType(),
                                StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                        .sorted(new MapComparator(sorts))
                                        .collect(Collectors.toList())
                        )
                )
                .collect(Collectors.toList());
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
        return filterByTypeAndName(code, Type.TV, ".*[sS]port.*", "^Canal\\+$");
    }

    private static class MapComparator implements Comparator<Map<String, String>> {

        private String[] sorts;

        protected MapComparator(String[] sorts) {
            this.sorts = sorts;
        }

        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            int out = 0;
            for (String p : sorts) {
                out = compare(o1, o2, p);
                if (out != 0) {
                    break;
                }
            }
            return out;
        }

        @SuppressWarnings("unchecked")
        private int compare(Map<String, String> o1, Map<String, String> o2, String prop) {
            Comparable c1 = o1.get(prop);
            Comparable c2 = o2.get(prop);
            if (c1 != null && c2 != null) {
                return c1.compareTo(c2);
            } else {
                return 0;
            }
        }
    }
}
