package net.cho20.neotv.script.bean

import groovy.transform.ToString

class Movie extends Stream implements Comparable<Movie>{

    Integer id_db
    String image
    String overview
    Date date
    Date publish

    @Override
    int compareTo(Movie o) {
        Integer out = 0
        if(out==0){
            out = -publish.compareTo(o.publish)
        }
        if(date && o.date) {
            out = -date.compareTo(o.date)
        }
        if(out==0 ){
            out = title.compareTo(o.title)
        }
        return out
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof Movie)) return false

        Movie movie = (Movie) o

        if (id != movie.id) return false
        if (title != movie.title) return false

        return true
    }

    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (title != null ? title.hashCode() : 0)
        return result
    }
}