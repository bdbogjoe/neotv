package net.cho20.neotv.core.service

import net.cho20.neotv.core.bean.Movie
import net.cho20.neotv.core.bean.MovieAble
import net.cho20.neotv.core.bean.MovieEntity

trait MovieConverter {

    def moviesProps = new Movie().properties.keySet().findAll({ it != 'class' }) as Set

    Movie convert(MovieAble entity){
        new Movie(entity.properties.findAll({ moviesProps.contains(it.key) }))
    }

}