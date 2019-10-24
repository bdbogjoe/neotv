package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["name", "videos"])
class Group {
    String name
    Type type
    Iterable<Stream> streams = []

    Group(){

    }

    Group(String name, Type type, streams) {
        this.name = name
        this.type = type
        this.streams = streams
    }
}