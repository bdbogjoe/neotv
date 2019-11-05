package net.cho20.neotv.core.service

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.core.bean.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.regex.Pattern

class M3uProcessor implements Processor, MovieConverter {


    private static final Logger LOG = LoggerFactory.getLogger(M3uProcessor.class)

    private final static Pattern CODE = Pattern.compile('(code=)(?:\\d+)')


    private Pattern p = Pattern.compile('(?:group-title="([^"]*)")?\\s*tvg-logo="([^"]*)",(.*)$')

    private final String url
    private final Map<String, M3uGroup> groups
    private final String code
    private final String api
    private final Storage storage

    M3uProcessor(Storage storage, String code, String api, Collection<M3uGroup> groups) {
        this.url = 'http://neotv.siptv-list.com/siptv.m3u?code=' + code
        this.groups = groups.collectEntries({ [(it.name): it] })
        this.code = code
        this.api = api
        this.storage = storage
    }

    Iterable<Group> process(ExecutorService executorService) {
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
                            M3uGroup gr = groups.get(g)
                            if (!groups || gr != null) {
                                if (currentGroup?.name != g) {
                                    LOG.info("Found group : {}", g)
                                    currentGroup = foundGroup.find {
                                        it.name == g
                                    }
                                    if (!currentGroup) {
                                        currentGroup = new Group(name: g)
                                        if (g.toLowerCase().contains('vod')) {
                                            currentGroup.type = Type.MOVIE
                                        } else if (g.toLowerCase().contains("box")) {
                                            currentGroup.type = Type.BOX_OFFICE
                                        } else {
                                            currentGroup.type = Type.TV
                                        }
                                        if (g.toLowerCase().contains("english")) {
                                            currentGroup.language = Language.EN
                                        } else {
                                            currentGroup.language = Language.FR
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
                            if (logo) {
                                try {
                                    new URL(logo).openStream()
                                } catch (IOException ioe) {
                                    logo = null
                                }
                            }
                            def title = m.group(3)
                            if (groups == null || groups.get(currentGroup.name).match(title)) {
                                if (currentGroup.type == Type.MOVIE) {
                                    def video = storage?.find(title)
                                    if (video) {
                                        video = convert(video)
                                    } else {
                                        video = new MovieBean(title: title, publish: now)
                                        if (storage) {
                                            executorService.submit(new MovieLoader(api, video.title) {
                                                @Override
                                                void onComplete(Movie movie) {
                                                    ['tmdb', 'image', 'overview', 'date'].each {
                                                        video[it] = found[it]
                                                    }
                                                    storage.insert(video)
                                                }
                                            })
                                        }
                                    }
                                    currentGroup.streams << video
                                } else {
                                    currentGroup.streams << new StreamBean(title: title, image: logo)
                                }
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
        LOG.info("Processed : {}", url)
        return foundGroup
    }
}
