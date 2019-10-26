package net.cho20.neotv.core.service

import net.cho20.neotv.core.bean.Movie

interface Storage {
    Movie find(String title)
    Iterable<Movie> findAfter(Date date);
    void insert(Movie movie)
}