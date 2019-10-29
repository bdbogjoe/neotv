package net.cho20.neotv.core.service

import net.cho20.neotv.core.bean.Movie
import net.cho20.neotv.core.bean.MovieAble
import net.cho20.neotv.core.bean.MovieEntity

interface Storage {
    MovieAble find(String title)
    Iterable<MovieAble> findAfter(Date date);
    void insert(Movie movie)
    void update(Movie movie)
}