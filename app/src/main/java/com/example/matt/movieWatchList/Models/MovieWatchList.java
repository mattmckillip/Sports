package com.example.matt.movieWatchList.Models;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Matt on 5/10/2016.
 */
public class MovieWatchList extends RealmObject {
    private Integer id;
    private RealmList<JSONMovie> movieList;

    public MovieWatchList() { }

    public RealmList<JSONMovie> getMovieList() {
        return movieList;
    }

    public void setMovieList(RealmList<JSONMovie> movieList) {
        this.movieList = movieList;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}