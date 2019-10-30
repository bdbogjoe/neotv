package net.cho20.neotv.core.service

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.core.bean.Group
import net.cho20.neotv.core.bean.MovieBean
import net.cho20.neotv.core.bean.StreamBean
import net.cho20.neotv.core.bean.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class M3uProcessor implements Processor, MovieConverter {



    private static final Logger LOG = LoggerFactory.getLogger(M3uProcessor.class)

    private final static Pattern CODE = Pattern.compile('(code=)(?:\\d+)')

    private ExecutorService executor = Executors.newFixedThreadPool(3)


    private Pattern p = Pattern.compile('(?:group-title="([^"]*)")?\\s*tvg-logo="([^"]*)",(.*)$')

    private final String url
    private final Set<String> groups
    private final String code
    private final String api
    private final Storage storage
    private final boolean wait

    M3uProcessor(Storage storage, boolean wait, String code, String api, def groups) {
        this.url = 'http://neotv.siptv-list.com/siptv.m3u?code=' + code
        this.groups = groups.collect() as Set
        this.code = code
        this.api = api
        this.wait = wait
        this.storage = storage
    }

    Iterable<Group> process() {
        LOG.info("Processing : {}", url)
        def now = new Date()
        def foundGroup = [] as LinkedList
        def currentGroup
        def http = new HTTPBuilder(url)
        http.request(Method.GET, ContentType.TEXT) { req ->
            response.success = { resp, reader ->
                reader.eachLine { line ->
                    def m = p.matcher(line)
                    if (m.find()) {
                        def g = m.group(1)
                        if (g) {
                            if (!groups || groups.contains(g) ) {
                                if(currentGroup?.name!=g) {
                                    LOG.info("Found group : {}", g)
                                    currentGroup = foundGroup.find {
                                        it.name==g
                                    }
                                    if(!currentGroup) {
                                        currentGroup = new Group(name: g)
                                        if (g.contains('VOD')) {
                                            currentGroup.type = Type.MOVIE
                                        }else if(g.toLowerCase().contains("box")){
                                            currentGroup.type = Type.BOX_OFFICE
                                        } else {
                                            currentGroup.type = Type.TV
                                        }
                                        foundGroup << currentGroup
                                    }
                                }
                            } else {
                                currentGroup = null
                            }
                        }
                        if (currentGroup) {
                            def logo = m.group(2)
                            if(logo) {
                                try {
                                    new URL(logo).openStream()
                                } catch (IOException ioe) {
                                    logo = null
                                }
                            }
                            def title = m.group(3)
                            if (currentGroup.type == Type.MOVIE) {
                                def video = storage?.find(title)
                                if (video) {
                                    video = convert(video)
                                } else {
                                    video = new MovieBean(title: title, publish: now)
                                    if (storage) {
                                        executor.submit(new MovieLoader(storage, api, video))
                                    }
                                }
                                currentGroup.streams << video
                            } else {
                                currentGroup.streams << new StreamBean(title: title, image: logo)
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
                        LOG.debug("Found stream : {}", stream)
                    }

                }
            }
        }
        executor.shutdown()
        if (wait) {
            while (!executor.isTerminated()) {
                LOG.debug("Waiting..., remaining tasks : " + executor.queue.size())
                Thread.currentThread().sleep(1000)
            }
        }
        LOG.info("Processed : {}", url)
        return foundGroup
    }
}
