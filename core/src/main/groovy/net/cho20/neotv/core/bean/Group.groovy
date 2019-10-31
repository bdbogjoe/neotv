package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["name", "language", "type"])
class Group<T> {
    Language language
    String name
    Type type
    Iterable<T> streams = [] as LinkedHashSet

    Group(){

    }

    Group(String name, Type type, Language language, streams) {
        this.name = name
        this.type = type
        this.language=language
        this.streams = streams
    }
}