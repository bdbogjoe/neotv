package net.cho20.neotv.script.service


import net.cho20.neotv.script.bean.Group
import net.cho20.neotv.script.bean.Movie
import net.cho20.neotv.script.bean.Stream
import net.cho20.neotv.script.bean.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class Processor {

    private static final Logger LOG = LoggerFactory.getLogger(Processor.class);

    private final static Pattern CODE = Pattern.compile('(code=)(?:\\d+)')

    private ExecutorService executor = Executors.newFixedThreadPool(3)


    private Pattern p = Pattern.compile('(?:group-title="([^"]*)")?,(.*)$')

    private final String url
    private final Set<String> groups
    private final String code
    private final String api
    private final Storage storage

    Processor(String code, String api, def groups) {
        this.url = 'http://neotv.siptv-list.com/siptv.m3u?code=' + code
        this.groups = groups.collect() as Set
        this.code = code
        this.api = api
        this.storage = new Storage()
    }

    Iterable<Group> process() {
        println url
        def now = new Date()
        def foundGroup = [] as LinkedList
        def currentGroup
        new URL(url).eachLine { line ->
            def m = p.matcher(line)
            if (m.find()) {
                def g = m.group(1)
                if (g) {
                    LOG.info("Found group {}", g)
                    if (!groups || groups.contains(g)) {
                        currentGroup = new Group(name: g)
                        if (g.contains('VOD')) {
                            currentGroup.type = Type.MOVIE
                        }else{
                            currentGroup.type = Type.TV
                        }
                        foundGroup << currentGroup
                    } else {
                        currentGroup = null
                    }
                }
                if (currentGroup) {
                    def title = m.group(2)
                    if (currentGroup.type == Type.MOVIE) {
                        def video = storage.find(title)
                        if (!video) {
                            video = new Movie(title: title, publish: now)
                            //executor.submit(new MovieLoader(storage, api, video))
                        }
                        currentGroup.streams << video
                        if (!video.id) {
                            executor.submit(new MovieLoader(storage, api, video))
                        }
                    } else {
                        currentGroup.streams << new Stream(title: title)
                    }
                }
            } else if (currentGroup && currentGroup.streams && line.startsWith("http")) {
                //URL of previous stream
                def stream = currentGroup.streams.last()
                def matcher = CODE.matcher(line)
                def sb = new StringBuffer()
                if (matcher.find()) {
                    matcher.appendReplacement(sb, '$1%s')
                }
                matcher.appendTail(sb)
                stream.url = sb.toString()
                LOG.info("Found stream : {}", stream)
            }
        }

        executor.shutdown()
        while (!executor.isTerminated()) {
            println "waiting..., remaining tasks : " + executor.queue.size()
            Thread.currentThread().sleep(1000)
        }
        foundGroup.each { Group g ->
            g.streams.each { v ->
                //Need to store it
                if (v instanceof Movie) {
                    if (!v.id) {
                        storage.insert(v)
                    }
                }
            }
        }
        return foundGroup
    }
}
