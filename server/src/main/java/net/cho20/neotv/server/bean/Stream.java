package net.cho20.neotv.server.bean;

public class Stream {

    private final String title;
    private final String url;
    private String image;

    public Stream(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
