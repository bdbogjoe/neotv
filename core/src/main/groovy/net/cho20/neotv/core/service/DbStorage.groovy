package net.cho20.neotv.core.service

import groovy.sql.Sql
import net.cho20.neotv.core.bean.MovieAble
import net.cho20.neotv.core.bean.MovieEntity

class DbStorage implements Storage, Closeable {

    def moviesProps = new MovieEntity().properties.keySet().findAll({ it != 'class' })

    def CREATE_TABLE_MOVIES = '''
CREATE TABLE MOVIES (
  id int PRIMARY KEY auto_increment,
  id_db int,
  title VARCHAR(128) not null,
  image VARCHAR(128),
  overview VARCHAR(2048),
  date date,
  publish date not null
)
'''

    Sql sql

    DbStorage() {
        sql = Sql.newInstance("jdbc:h2:file:./neotv", "sa", "sa", "org.h2.Driver")
        boolean found
        sql.eachRow("SELECT * FROM INFORMATION_SCHEMA.TABLES") { row ->
            if (row.TABLE_NAME == 'MOVIES') {
                found = true
            }
        }
        if (!found) {
            sql.execute(CREATE_TABLE_MOVIES)
        }
    }

    void insert(MovieAble video){
        sql.executeUpdate("insert into MOVIES(id_db, title, image, overview, date, publish) values(:id_db, :title, :image, :overview, :date, :publish)", video.properties)
        sql.commit()
    }

    void update(MovieAble video){
        sql.executeUpdate("delete from MOVIES where title=?", [video.title])
        insert(video)
    }

    Iterable<MovieAble> findAfter(Date date=null){
        if(!date){
            sql.eachRow("select max(publish) publish from movie", {
                date = row.publish
            })
        }
        def out=[]
        sql.eachRow("select * from MOVIES where publish>?", [title], { row ->
            out <<bind(row)
        })
        return out
    }


    private MovieEntity bind(row){
        def video = new MovieEntity()
        moviesProps.each {
            video[it] = row[it.toUpperCase()]
        }
        return video
    }

    MovieAble find(String title){
        def video
        sql.eachRow("select * from MOVIES where title=?", [title], { row ->
            video = bind(row)
        })
        return video
    }

    void close(){
        sql.close()
    }


}
