import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.ToString
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import java.text.SimpleDateFormat
import java.util.regex.Pattern

@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')
@Grab(group = 'com.h2database', module = 'h2', version = '1.3.176')

def cli = new CliBuilder(usage: 'groovy <script> [options]')
cli._(longOpt: 'code', args: 1, argName: 'code', 'Set neotv code', required: true)
cli._(longOpt: 'api', args: 1, argName: 'API', 'Set the api', required: true)
cli._(longOpt: 'group', args: 1, argName: 'code', 'Set neotv group', required: true)

def options = cli.parse(args)

@ToString(includes = ["title", "url"])
class Movie {
    Integer id
    String title
    String url
    String image
    String overview
    Date date
}

@ToString(includes = ["name", "videos"])
class Group {
    def name
    def videos = []
}

def moviesProps = new Movie().properties.keySet().findAll({it!='class'})

Pattern p = Pattern.compile('(?:group-title="([^"]*)")?,(.*)$')

if (options) {
    def url = 'http://neotv.siptv-list.com/siptv.m3u?code=' + options.code
    def db = 'https://api.themoviedb.org/3/search/movie'
    def sql = Sql.newInstance("jdbc:h2:file:./neotv", "sa", "sa", "org.h2.Driver")
    try {
        boolean found
        sql.eachRow("SELECT * FROM INFORMATION_SCHEMA.TABLES") { row ->
            if (row.TABLE_NAME == 'MOVIES') {
                found = true
            }
        }
        if (!found) {
            sql.execute('''
CREATE TABLE MOVIES (
  id INT PRIMARY KEY,
  title VARCHAR(128),
  url VARCHAR(128),
  image VARCHAR(128),
  overview VARCHAR(1024),
  date date
)
''')
        }
        println url
        def groups = [] as LinkedList
        def currentGroup
        new URL(url).eachLine { line ->
            def m = p.matcher(line)
            if (m.find()) {
                def g = m.group(1)
                if (g) {
                    println "Found group ${g}"
                    if (g?.equals(options.group)) {
                        currentGroup = new Group(name: g)
                        groups << currentGroup
                    } else {
                        currentGroup = null
                    }
                } else {
                    if (currentGroup) {
                        def title = m.group(2)
                        def video
                        sql.eachRow("select * from MOVIES where title=?", [title], { row ->
                            video = new Movie()
                            moviesProps.each {
                                video[it] = row[it.toUpperCase()]
                            }
                        })
                        if (!video) {
                            println "Processing $title"
                            video = new Movie(title: title)
                            def http = new HTTPBuilder(db)
                            // Used for all other failure codes not handled by a code-specific handler:
                            http.handler.failure = { resp ->
                                println "Unexpected failure: ${resp.statusLine}"
                            }
                            http.request(Method.GET, ContentType.TEXT) { req ->
                                uri.query = [api_key: options.api, language: 'fr_FR', query: title, include_adult: false]

                                response.success = { resp, reader ->
                                    assert resp.status == 200
                                    def jsonSlurper = new JsonSlurper()
                                    def content = jsonSlurper.parse(reader)
                                    if (content.total_results) {
                                        def results = content.results.sort { row1, row2 ->
                                            row2.popularity.compareTo(row1.popularity)
                                        }
                                        def result = results.get(0)
                                        video.id = result.id
                                        video.overview = result.overview
                                        if(result.release_date) {
                                            video.date = new SimpleDateFormat("yyyy-MM-dd").parse(result.release_date)
                                        }
                                        video.image = "https://image.tmdb.org/t/p/w500${result.poster_path}"
                                    }
                                }

                                // called only for a 404 (not found) status code:
                                response.'404' = { resp ->
                                    println 'Not found'
                                }
                                response.'401' = { resp ->
                                    println "Access denied"
                                }
                            }
                        }
                        currentGroup.videos << video
                    }
                }
            } else if (currentGroup && currentGroup.videos && line.startsWith("http")) {
                //URL of previous movie
                def video = currentGroup.videos.last()
                video.url = line
                //Need to store it
                if (video.id) {
                    int row = sql.executeUpdate("delete from MOVIES where id=?", [video.id])
                    if(row){
                        println "Updating : ${video.title}"
                    }else{
                        println "Inserting : ${video.title}"
                    }
                    row = sql.executeUpdate("insert into MOVIES(id, title, url, image, overview, date) values(:id, :title, :url, :image, :overview, :date)", video.properties)
                    if(!row){
                        throw new Exception("unable to insert : "+video)
                    }
                    sql.commit()
                }
            }
        }
        JsonBuilder builder = new JsonBuilder(groups);

        println builder.toPrettyString()
    }finally{
        sql.close()
    }
} else {
    cli.usage()
    System.exit(1)
}


