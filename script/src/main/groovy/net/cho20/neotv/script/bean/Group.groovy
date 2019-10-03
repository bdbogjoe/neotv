package net.cho20.neotv.script.bean

import groovy.transform.ToString

@ToString(includes = ["name", "videos"])
class Group {
    def name
    def videos = []
}