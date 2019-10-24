package net.cho20.neotv.server.bean;

public class Stream {

    private String title;
    private String url;
    private String image;

    public Stream(String title, String url, String image) {
        this.title = title;
        this.url = url;
        this.image=image;
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
