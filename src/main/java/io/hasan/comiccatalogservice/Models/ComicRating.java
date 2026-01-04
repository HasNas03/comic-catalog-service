package io.hasan.comiccatalogservice.Models;

public class ComicRating {
    String id;
    Integer rating;

    public ComicRating(String id, Integer rating) {
        this.id = id;
        this.rating = rating;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public Integer getRating() {return rating;}
    public void setRating(Integer rating) {this.rating = rating;}
}
