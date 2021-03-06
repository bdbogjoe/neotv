package net.cho20.neotv.core.bean

import groovy.transform.ToString

@ToString(includes = ["title", "url"])
class StreamBean {
    String title
    String url
    String image

    String buildUrl(String code) {
        url ? String.format(url, code) : null
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof StreamBean)) return false

        StreamBean stream = (StreamBean) o

        if (title != stream.title) return false

        return true
    }

    int hashCode() {
        int result
        result = title.hashCode()
        return result
    }
}
