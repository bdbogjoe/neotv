package net.cho20.neotv.script.bean

import groovy.transform.ToString

import java.text.MessageFormat


@ToString(includes = ["title", "url"])
class Stream {
    Integer id
    String title
    String url

    String buildUrl(String code){
        String.format(url,code)
    }
}
