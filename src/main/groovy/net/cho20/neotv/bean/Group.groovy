package net.cho20.neotv.bean

import groovy.transform.ToString

@ToString(includes = ["name", "videos"])
class Group {
    def name
    def videos = []
}