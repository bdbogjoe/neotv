package net.cho20.neotv.core.service

import net.cho20.neotv.core.bean.Movie
import net.cho20.neotv.core.bean.MovieEntity

interface Storage {
    MovieEntity find(String title)
    Iterable<MovieEntity> findAfter(Date date);
    void insert(Movie movie)
    void update(Movie movie)
}