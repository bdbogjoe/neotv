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

class JsonProcessor implements Processor {
    private Integer pack
    private String name

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessor.class);

    JsonProcessor(Integer pack, String name) {
        this.pack = pack
        this.name = name
    }

    @Override
    Iterable<Group> process() {
        def now = new Date()
        def url = "http://vod.ddnb.tn/channels.php?login=%25s&pack_id=${pack}"
        LOG.info("Processing {}", url);
        def http = new HTTPBuilder(url)
        def out = new Group(name, Type.MOVIE, [])
        http.request(Method.GET, ContentType.TEXT) { req ->
            response.success = { resp, reader ->
                def jsonSlurper = new JsonSlurper()
                def content = jsonSlurper.parse(reader);
                for (ch in content.channels) {
                    out.streams.add(new Movie(
                            title: ch.name,
                            url: ch.ch,
                            image: ch.logo,
                            overview: ch.desc,
                            date: new SimpleDateFormat('YYYY-MM-dd').parse(ch.date),
                            publish: now,
                    )
                    )
                }

            }
        }
        return [out]
    }
}
