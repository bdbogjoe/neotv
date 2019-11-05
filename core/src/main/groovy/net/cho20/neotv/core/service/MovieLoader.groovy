package net.cho20.neotv.core.service

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import me.xdrop.fuzzywuzzy.FuzzySearch
import net.cho20.neotv.core.bean.Movie
import net.cho20.neotv.core.bean.MovieBean
import net.cho20.neotv.core.bean.MovieVod
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.function.Supplier

abstract class MovieLoader implements Runnable {
    private String db = 'https://api.themoviedb.org/3/search/movie'

    private static final Logger LOG = LoggerFactory.getLogger(MovieLoader.class);


    private final String  title
    private final String api

    MovieLoader(String api, String title) {
        this.api = api
        this.title = title
    }

    abstract void onComplete(Movie movie);

    protected String getSearchTitle(){
        return title
    }

    @Override
    void run() {
        Movie movie;
        LOG.info("Processing {}", title)
        def http = new HTTPBuilder(db)
        // Used for all other failure codes not handled by a code-specific handler:
        http.handler.failure = { resp ->
            LOG.error("Unexpected failure: {}", resp.statusLine)
        }
        http.request(Method.GET, ContentType.TEXT) { req ->
            uri.query = [api_key: api, language: 'fr', query: getSearchTitle(), include_adult: false]

            response.success = { resp, reader ->
                assert resp.status == 200
                def jsonSlurper = new JsonSlurper()
                def content = jsonSlurper.parse(reader)
                if (content.total_results) {
                    def results = content.results.sort { row1, row2 ->
                        int d1 = FuzzySearch.ratio(row1.original_title, getSearchTitle())
                        int d2 = FuzzySearch.ratio(row2.original_title, getSearchTitle())
                        int out = d2.compareTo(d1)
                        if(out==0) {
                            out = row2.popularity.compareTo(row1.popularity)
                        }
                        return out
                    }
                    def result = results.get(0)
                    movie = new MovieBean(title: getSearchTitle())
                    movie.tmdb = result.id
                    movie.overview = result.overview
                    if (result.release_date && !result.release_date.startsWith("00")) {
                        movie.date = new SimpleDateFormat("yyyy-MM-dd").parse(result.release_date)
                    }
                    movie.image = result.poster_path
                }
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                LOG.warn('Not found for {}', title)
            }
            response.'401' = { resp ->
                LOG.warn("Access denied for : {}", title)
            }
            response.'429' = {
                run()
            }
        }
        LOG.info("Processed {} ", getSearchTitle())
        onComplete(movie)
    }
}
