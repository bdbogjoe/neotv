package net.cho20.neotv.service

import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.cho20.neotv.bean.Movie

import java.text.SimpleDateFormat

class MovieLoader implements Runnable{
    private String db = 'https://api.themoviedb.org/3/search/movie'


    private Movie movie
    private String api
    private Storage storage

    MovieLoader(Storage storage, String api, Movie movie) {
        this.storage=storage
        this.api=api
        this.movie = movie
    }

    @Override
    void run() {
        println "Processing ${movie}"
        def http = new HTTPBuilder(db)
        // Used for all other failure codes not handled by a code-specific handler:
        http.handler.failure = { resp ->
            println "Unexpected failure: ${resp.statusLine}"
        }
        http.request(Method.GET, ContentType.TEXT) { req ->
            uri.query = [api_key: api, language: 'fr_FR', query: movie.title, include_adult: false]

            response.success = { resp, reader ->
                assert resp.status == 200
                def jsonSlurper = new JsonSlurper()
                def content = jsonSlurper.parse(reader)
                if (content.total_results) {
                    def results = content.results.sort { row1, row2 ->
                        row2.popularity.compareTo(row1.popularity)
                    }
                    def result = results.get(0)
                    movie.id_db = result.id
                    movie.overview = result.overview
                    if (result.release_date) {
                        movie.date = new SimpleDateFormat("yyyy-MM-dd").parse(result.release_date)
                    }
                    movie.image = result.poster_path
                }
            }

            // called only for a 404 (not found) status code:
            response.'404' = { resp ->
                println 'Not found'
            }
            response.'401' = { resp ->
                println "Access denied"
            }
            response.'429' = {
                run()
            }
        }
        if(movie.url){
            storage.insert(movie)
        }
    }
}
