package net.cho20.neotv.server.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Language;
import net.cho20.neotv.core.bean.Type;
import net.cho20.neotv.server.service.ProcessorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("rest")
public class RestController {

    private final ProcessorService processorService;
    private String[] sorts = {"publish", "date", "title"};
    private Collection<String> revers = Arrays.stream(new String[]{"publish", "date"}).collect(Collectors.toSet());

    public RestController(ProcessorService processorService) {
        this.processorService = processorService;
    }


    @GetMapping("/refresh")
    public void refresh() {
        processorService.process();
    }




    @GetMapping("/all")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code) {
        return processorService.getGroups(code)
                .map(mapGroup -> new Group<Map<String, String>>(
                        mapGroup.getName(),
                        mapGroup.getType(),
                        mapGroup.getLanguage(),
                        StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                .sorted(new MapComparator(sorts, revers))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public Iterable<net.cho20.neotv.core.bean.Group> groups(@RequestParam(value = "code") String code,
                                                            @RequestParam(value = "type", required = false) String type,
                                                            @RequestParam(value = "language", required = false) String language,
                                                            @RequestParam(value = "name") String name) {
        return filter(code, type != null ? Type.valueOf(type) : null, language , name);
    }

    private Iterable<net.cho20.neotv.core.bean.Group> filter(String code, Type type, String language, String... name) {
        Language l = language!=null && !language.equals("undefined")?Language.valueOf(language.toUpperCase()):Language.FR;
        return processorService.getGroups(code)
                .filter(group -> type == null || group.getType() == type)
                .filter(group -> language == null || group.getLanguage() == l)
                .map(mapGroup -> new Group<Map<String, String>>(
                        mapGroup.getName(),
                        mapGroup.getType(),
                        mapGroup.getLanguage(),
                        StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                                .filter(stringStringMap ->
                                        name == null || name.length == 0 ||
                                                Arrays.stream(name).map(Pattern::compile).anyMatch(s -> {
                                                            String title = stringStringMap.get("title");
                                                            return s.matcher(title).find();
                                                        }
                                                )
                                )
                                .sorted(new MapComparator(sorts, revers))
                                .collect(Collectors.toList())
                ))
                .filter(group -> group.getStreams().iterator().hasNext())
                .collect(Collectors.toList());
    }

    @GetMapping("/movies")
    public Iterable<net.cho20.neotv.core.bean.Group> movies(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "language", required = false) String language
    ) {
        return filter(code, Type.MOVIE, language);
    }


    @GetMapping("/cartoons")
    public Iterable<net.cho20.neotv.core.bean.Group> cartoons(
            @RequestParam(value = "code") String code,
            @RequestParam(value = "language", required = false) String language
    ) {
        return filter(code, Type.CARTOON, language );
    }

    @GetMapping("/tv")
    public Iterable<net.cho20.neotv.core.bean.Group> tv(@RequestParam(value = "code") String code,
                                                        @RequestParam(value = "language", required = false) String language
    ) {
        return filter(code, Type.TV, language);
    }

    @GetMapping("/sport")
    public Iterable<net.cho20.neotv.core.bean.Group> sport(@RequestParam(value = "code") String code) {
        return filter(code, Type.TV, null, ".*[sS]port.*", "^Canal\\+$");
    }

    private static class MapComparator implements Comparator<Map<String, String>> {

        private final String[] sorts;
        private final Collection<String> revers;

        MapComparator(String[] sorts, Collection<String> revers) {
            this.sorts = sorts;
            this.revers = revers;
        }

        @Override
        public int compare(Map<String, String> o1, Map<String, String> o2) {
            int out = 0;
            for (String p : sorts) {
                out = compare(o1, o2, p);
                if (out != 0) {
                    if (revers.contains(p)) {
                        out = -out;
                    }
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
