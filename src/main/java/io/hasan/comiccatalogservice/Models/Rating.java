package io.hasan.comiccatalogservice.Models;

public class Rating {

    // attributes
    private String userId;
    private String comicId;
    private int comicRating;

    // constructor
    public Rating(String userId, String comicId, int comicRating) {
        this.userId = userId;
        this.comicId = comicId;
        this.comicRating = comicRating;
    }

    // getters and setters
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    public String getComicId() {return comicId;}
    public void setComicId(String comicId) {this.comicId = comicId;}
    public int getComicRating() {return comicRating;}
    public void setComicRating(int comicRating) {this.comicRating = comicRating;}
}
