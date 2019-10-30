package net.cho20.neotv.server.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.Date;

import net.cho20.neotv.core.bean.MovieAble;
import org.hibernate.validator.constraints.UniqueElements;

@Entity
public class MovieEntity implements MovieAble {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true)
    private Integer id_db;

    @Column(unique = true)
    private String title;

    private String image;
    private String overview;
    private Date date;
    private Date publish;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId_db() {
        return id_db;
    }

    public void setId_db(Integer id_db) {
        this.id_db = id_db;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public Date getPublish() {
        return publish;
    }

    public void setPublish(Date publish) {
        this.publish = publish;
    }
}
