package com.revature.daka.models;

import com.revature.daka.persistence.Column;
import com.revature.daka.persistence.Entity;
import com.revature.daka.persistence.Id;
import com.revature.daka.persistence.Table;

@Entity
@Table(name = "test_1")
public class TestOne {
    @Id(type = "default")
    @Column(name = "id")
    private int id = 1;
    @Column(name = "name")
    private String title = "placeholder";
    @Column(name = "genre")
    private String genre = "generic";
    @Column(name = "on_fire")
    private boolean onFire = true;
    @Column(name = "real_test")
    private int realTest = 69;

    public TestOne() {
    }


    public TestOne(int id, String title, String genre, boolean onFire, int realTest) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.onFire = onFire;
        this.realTest = realTest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isOnFire() {
        return onFire;
    }

    public void setOnFire(boolean onFire) {
        this.onFire = onFire;
    }

    @Override
    public String toString() {
        return "TestOne{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", onFire=" + onFire +
                ", realTest=" + realTest +
                '}';
    }
}
