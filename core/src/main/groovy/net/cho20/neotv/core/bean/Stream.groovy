package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["title", "url"])
class Stream implements Comparable<Stream>{
    String title
    String url

    String buildUrl(String code){
        String.format(url,code)
    }

    @Override
    int compareTo(Stream o) {
        return title.compareTo(o.title)
    }
}
