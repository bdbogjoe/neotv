package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["title", "url"])
class Stream {
    Integer id
    String title
    String url

    String buildUrl(String code){
        String.format(url,code)
    }
}
