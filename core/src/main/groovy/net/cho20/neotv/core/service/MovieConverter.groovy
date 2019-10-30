package net.cho20.neotv.core.service

import net.cho20.neotv.core.bean.MovieBean
import net.cho20.neotv.core.bean.MovieAble

trait MovieConverter {

    def moviesProps = new MovieBean().properties.keySet().findAll({ it != 'class' }) as Set

    MovieBean convert(MovieAble entity){
        new MovieBean(entity.properties.findAll({ moviesProps.contains(it.key) }))
    }

}