package net.cho20.neotv.core.service


import net.cho20.neotv.core.bean.MovieAble

interface Storage {
    MovieAble find(String title)
    Iterable<MovieAble> findAfter(Date date);
    void insert(MovieAble movie)
    void update(MovieAble movie)
}