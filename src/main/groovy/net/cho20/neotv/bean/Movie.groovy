package net.cho20.neotv.bean

import groovy.transform.ToString

@ToString(includes = ["title", "url"])
class Movie {
    Integer id
    Integer id_db
    String title
    String url
    String image
    String overview
    Date date
    Date publish
}