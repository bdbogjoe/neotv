package net.cho20.neotv.script.service


import net.cho20.neotv.bean.Group
import net.cho20.neotv.bean.Movie

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class Processor {

    private ExecutorService executor = Executors.newFixedThreadPool(3)


    private Pattern p = Pattern.compile('(?:group-title="([^"]*)")?,(.*)$')

    private final String url
    private final String group
    private final String code
    private final String api
    private final Storage storage

    Processor(String code, String api, String group) {
        this.url = 'http://neotv.siptv-list.com/siptv.m3u?code=' + code
        this.group = group
        this.code = code
        this.api = api
        this.storage = new Storage()
    }

    Iterable<Group> process() {
        println url
        def now = new Date()
        def groups = [] as LinkedList
        def currentGroup
        new URL(url).eachLine { line ->
            def m = p.matcher(line)
            if (m.find()) {
                def g = m.group(1)
                if (g) {
                    println "Found group ${g}"
                    if (!group || g?.equals(group)) {
                        currentGroup = new Group(name: g)
                        groups << currentGroup
                    } else {
                        currentGroup = null
                    }
                } else {
                    if (currentGroup) {
                        def title = m.group(2)
                        def video = storage.find(title)
                        if(!video) {
                            video = new Movie(title: title, publish: now)
                            executor.submit(new MovieLoader(storage, api, video))
                        }
                        currentGroup.videos << video
                        if(!video.id){
                            executor.submit(new MovieLoader(storage, api, video))
                        }
                    }
                }
            } else if (currentGroup && currentGroup.videos && line.startsWith("http")) {
                //URL of previous movie
                def video = currentGroup.videos.last()
                video.url = line
            }
        }
        executor.shutdown()
        while (!executor.isTerminated()) {
            println "waiting..., remaining tasks : "+executor.queue.size()
            Thread.currentThread().sleep(1000)
        }
        groups.each{Group g->
            g.videos.each {v->
                //Need to store it
                if (!v.id) {
                    storage.insert(v)
                }
            }
        }
        return groups
    }
}
