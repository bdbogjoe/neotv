package net.cho20.neotv.core.service

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.core.bean.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern

class JsonProcessor implements Processor, MovieConverter {
    private final Integer[] pack
    private final String name
    private final String api
    private final Language language
    private final Type type

    private Storage storage

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessor.class);

    JsonProcessor(Storage storage, String api, String name, Language language, Type type, Integer... pack) {
        this.pack = pack
        this.name = name
        this.type = type
        this.language = language
        this.storage = storage
        this.api = api
    }

    @Override
    Iterable<Group> process(ExecutorService executorService) {
        def out = new Group(name, type, language, [])
        for (p in pack) {
            def now = new Date()
            def url = "http://vod.ddnb.tn/channels.php?login=%25s&pack_id=${p}"
            LOG.info("Processing {} {} {} : {}", name, type, language, url);
            def http = new HTTPBuilder(url)
            http.request(Method.GET, ContentType.TEXT) { req ->
                response.success = { resp, reader ->
                    def jsonSlurper = new JsonSlurper()
                    def content = jsonSlurper.parse(reader);
                    for (ch in content.channels) {
                        LOG.debug("Found stream : {}", ch.name)
                        def video = storage?.find(ch.name)
                        def insert = false
                        if (video) {
                            video = convert(video)
                        } else {
                            insert = true
                            video = new MovieBean(publish: now)
                        }
                        String logo = ch.logo
                        if (logo && !video.image) {
                            try {
                                new URL(logo).openStream()
                            } catch (IOException ignore) {
                                for(pp in ['((?:.*)[^\\.])png', '(.*)\\.(?:\\w+)png$'].collect({Pattern.compile(it)})){
                                    Matcher m = pp.matcher(ch.logo)
                                    if(m.matches()){
                                        logo = m.group(1)+'.png'
                                        try {
                                            new URL(logo).openStream()
                                            break
                                        } catch (IOException ignore2) {
                                            logo = null
                                        }
                                    }
                                }
                                if (logo == null) {
                                    LOG.info("Unable to get image {} using tmdb", ch.logo)
                                    insert = false
                                    executorService.submit(new MovieLoader(api, ch.name){
                                        @Override
                                        void onComplete(Movie movie) {
                                            if(movie?.image) {
                                                video.image = movie.image
                                                video.tmdb = movie.tmdb
                                                if (storage) {
                                                    storage.insert(video)
                                                }
                                            }
                                        }
                                    })
                                }
                            }
                        }
                        video.title = ch.name
                        video.url = ch.ch
                        if (logo) {
                            video.image = logo
                        }
                        video.overview = ch.desc
                        video.date = new SimpleDateFormat('yyyy-MM-dd').parse(ch.date)
                        if (storage && insert) {
                            storage.insert(video)
                        }
                        out.streams.add(video)
                    }

                }
            }
            LOG.info("Processed {} {} {} : {}", name, type, language, url);
        }
        return [out]
    }

    static void main(String[] args) {
        for (int i = 1; i < 1000; i++) {
            def p = new JsonProcessor(null, i, "${i}", null, null)
            def groups = p.process()
            if (groups && groups[0].streams) {
                def stream = groups[0].streams[0]
                if (!stream.title.contains(" s0") && !stream.title.contains("_s0") && !stream.title.contains("_ep") && !stream.title.contains(" ep")) {
                    println(i + "     " + stream.url + "     " + stream.title + "----" + stream.overview)
                }
            }
        }
    }
}
