package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["title", "url"])
class Stream implements Comparable<Stream> {
    String title
    String url
    String image

    String buildUrl(String code) {
        url ? String.format(url, code) : null
    }

    @Override
    int compareTo(Stream o) {
        return title.compareTo(o.title)
    }
}
