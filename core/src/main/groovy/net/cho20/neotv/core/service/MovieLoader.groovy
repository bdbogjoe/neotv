package net.cho20.neotv.core.service

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.core.bean.Movie
import org.apache.commons.lang.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

class MovieLoader implements Runnable {
    private String db = 'https://api.themoviedb.org/3/search/movie'

    private static final Logger LOG = LoggerFactory.getLogger(MovieLoader.class);


    private Movie movie
    private String api
    private Storage storage

    MovieLoader(Storage storage, String api, Movie movie) {
        this.storage = storage
        this.api = api
        this.movie = movie
    }

    @Override
    void run() {
        LOG.info("Processing {}", movie.title)
        def http = new HTTPBuilder(db)
        // Used for all other failure codes not handled by a code-specific handler:
        http.handler.failure = { resp ->
            println "Unexpected failure: ${resp.statusLine}"
        }
        http.request(Method.GET, ContentType.TEXT) { req ->
            uri.query = [api_key: api, language: 'fr', query: movie.title, include_adult: false]

            response.success = { resp, reader ->
                assert resp.status == 200
                def jsonSlurper = new JsonSlurper()
                def content = jsonSlurper.parse(reader)
                if (content.total_results) {
                    def results = content.results.sort { row1, row2 ->
                        int d1 = StringUtils.getLevenshteinDistance(row1.original_title, movie.title)
                        int d2 = StringUtils.getLevenshteinDistance(row2.original_title, movie.title)
                        int out = d1.compareTo(d2)
                        if(out==0) {
                            out = row2.popularity.compareTo(row1.popularity)
                        }
                        return out
                    }
                    def result = results.get(0)
                    movie.id_db = result.id
                    movie.overview = result.overview
                    if (result.release_date) {
                        movie.date = new SimpleDateFormat("yyyy-MM-dd").parse(result.release_date)
                    }
                    movie.image = result.poster_path
                    storage.update(movie)
                }
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                LOG.warn('Not found for {}', movie.title)
            }
            response.'401' = { resp ->
                LOG.warn("Access denied for : {}", movie.title)
            }
            response.'429' = {
                run()
            }
        }
        if (movie.url && storage) {
            storage.insert(movie)
        }
    }
}
