package net.cho20.neotv.server.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Language;
import net.cho20.neotv.core.bean.Type;
import net.cho20.neotv.server.service.ProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/rest")
public class RestController {

    private static final String[] URLS = {"tv", "sport", "movies", "cartoons"};
    private static final String[] LNG = {"movies", "cartoons"};

    private final ProcessorService processorService;
    @Value("${app.code}")
    private String internalCode;
    private final String[] SORTS = {"publish", "date", "title"};
    private final Collection<String> SORTS_REVERS = Arrays.stream(new String[] {"publish", "date"}).collect(Collectors.toSet());

    public RestController(ProcessorService processorService) {
        this.processorService = processorService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Collection<String>>> home(HttpServletRequest request) {
        boolean prod = request.getHeader("prod")!=null;
        String scheme = "http"+(prod?"s":"");
        Map<String, Collection<String>> out = Arrays.stream(URLS)
            .collect(Collectors.toMap(s -> s, type -> {
                if (Arrays.asList(LNG).contains(type)){
                    return Arrays.stream(Language.values()).map(s -> ServletUriComponentsBuilder
                        .fromCurrentRequestUri()
                        .scheme(scheme)
                        .path("/" + type)
                        .queryParam("language", s)
                        .toUriString()).collect(Collectors.toList());
                }else{
                    return Collections.singleton(ServletUriComponentsBuilder
                        .fromCurrentRequestUri()
                        .scheme(scheme)
                        .path("/" + type)
                        .toUriString());
                }
                }

            ));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(out);

    }

    @GetMapping("/refresh")
    public void refresh() {
        processorService.process();
    }

    private String getCode(HttpServletRequest request, String code) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(request.getRemoteAddr());
        if (code == null && (address.isLoopbackAddress() || address.isSiteLocalAddress())) {
            return internalCode;
        } else {
            if (code == null) {
                throw new IllegalArgumentException("Code is mandatory");
            }
            return code;
        }
    }

    @GetMapping("/all")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> groups(HttpServletRequest request, @RequestParam(value = "code", required = false) String code) throws UnknownHostException {
        return processorService.getGroups(getCode(request, code))
            .map(mapGroup -> new Group<Map<String, String>>(
                mapGroup.getName(),
                mapGroup.getType(),
                mapGroup.getLanguage(),
                StreamSupport.stream(mapGroup.getStreams().spliterator(), false)
                    .sorted(new MapComparator(SORTS, SORTS_REVERS))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> groups(HttpServletRequest request, @RequestParam(value = "code", required = false) String code,
                                                               @RequestParam(value = "type", required = false) String type,
                                                               @RequestParam(value = "language", required = false) String language,
                                                               @RequestParam(value = "name") String name) throws UnknownHostException {
        return filter(getCode(request, code), type != null ? Type.valueOf(type) : null, language, name);
    }

    private Iterable<net.cho20.neotv.core.bean.Group<?>> filter(String code, Type type, String language, String... name) {
        Language l = language != null && !language.isEmpty() && !language.equals("undefined") ? Language.valueOf(language.toUpperCase()) : Language.FR;
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
                    .sorted(new MapComparator(SORTS, SORTS_REVERS))
                    .collect(Collectors.toList())
            ))
            .filter(group -> group.getStreams().iterator().hasNext())
            .collect(Collectors.toList());
    }

    @GetMapping("/movies")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> movies(HttpServletRequest request,
                                                               @RequestParam(value = "code", required = false) String code,
                                                               @RequestParam(value = "language", required = false) String language
    ) throws UnknownHostException {
        return filter(getCode(request, code), Type.MOVIE, language);
    }

    @GetMapping("/cartoons")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> cartoons(HttpServletRequest request,
                                                                 @RequestParam(value = "code", required = false) String code,
                                                                 @RequestParam(value = "language", required = false) String language
    ) throws UnknownHostException {
        return filter(getCode(request, code), Type.CARTOON, language);
    }

    @GetMapping("/tv")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> tv(HttpServletRequest request, @RequestParam(value = "code", required = false) String code,
                                                           @RequestParam(value = "language", required = false) String language
    ) throws UnknownHostException {
        return filter(getCode(request, code), Type.TV, language);
    }

    @GetMapping("/sport")
    public Iterable<net.cho20.neotv.core.bean.Group<?>> sport(HttpServletRequest request, @RequestParam(value = "code", required = false) String code) throws UnknownHostException {
        return filter(getCode(request, code), Type.TV, null, ".*[sS]port.*", "^Canal\\+$", ".*foot.*");
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
