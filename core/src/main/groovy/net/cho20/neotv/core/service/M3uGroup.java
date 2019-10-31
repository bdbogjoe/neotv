package net.cho20.neotv.core.service;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class M3uGroup {
    private final String name;
    private final Collection<Pattern> excludes;

    public M3uGroup(String name, Collection<String> exclude) {
        this.name = name;
        if(exclude!=null) {
            this.excludes = exclude.stream().map(Pattern::compile).collect(Collectors.toList());
        }else{
            this.excludes=null;
        }
    }

    String getName() {
        return name;
    }

    boolean match(String title){
        return excludes==null || excludes.stream().noneMatch(pattern -> pattern.matcher(title).matches());
    }
}
