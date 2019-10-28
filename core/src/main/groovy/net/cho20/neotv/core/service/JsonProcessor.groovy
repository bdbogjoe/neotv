package net.cho20.neotv.core.service


import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.core.bean.Group
import net.cho20.neotv.core.bean.Movie
import net.cho20.neotv.core.bean.Type
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

class JsonProcessor implements Processor , MovieConverter{
    private final Integer pack
    private final String name
    private final Type type

    private Storage storage

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessor.class);

    JsonProcessor(Storage storage, Integer pack, String name, Type type) {
        this.pack = pack
        this.name = name
        this.type=type
        this.storage=storage
    }

    @Override
    Iterable<Group> process() {
        def now = new Date()
        def url = "http://vod.ddnb.tn/channels.php?login=%25s&pack_id=${pack}"
        LOG.info("Processing {}", url);
        def http = new HTTPBuilder(url)
        def out = new Group(name, type, [])
        http.request(Method.GET, ContentType.TEXT) { req ->
            response.success = { resp, reader ->
                def jsonSlurper = new JsonSlurper()
                def content = jsonSlurper.parse(reader);
                for (ch in content.channels) {
                    def video = storage?.find(ch.name)
                    def insert=false
                    if (video) {
                        video = convert(video)
                    }else {
                        insert=true
                        video = new Movie(publish: now)
                    }
                    video.title=ch.name
                    video.url=ch.ch
                    video.image=ch.logo
                    video.overview=ch.desc
                    video.date=new SimpleDateFormat('YYYY-MM-dd').parse(ch.date)
                    if (storage && insert) {
                        storage.insert(video)
                    }
                    out.streams.add(video)
                }

            }
        }
        return [out]
    }
}
