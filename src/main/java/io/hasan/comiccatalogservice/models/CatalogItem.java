package io.hasan.comiccatalogservice.models;
import java.util.UUID;

public class CatalogItem {

    // attributes
    private UUID comicId;
    private String comicTitle;
    private String comicIssue;
    private Integer comicStartYear;
    private String comicDesc;
    private UUID ratingId;
    private Integer ratingScore;
    private String ratingReview;

    // constructors
    public CatalogItem(){};
    public CatalogItem(UUID comicId, String comicTitle, String comicIssue, Integer comicStartYear, String comicDesc, UUID ratingId, Integer ratingScore, String ratingReview) {
        this.comicId = comicId;
        this.comicTitle = comicTitle;
        this.comicIssue = comicIssue;
        this.comicStartYear = comicStartYear;
        this.comicDesc = comicDesc;
        this.ratingId = ratingId;
        this.ratingScore = ratingScore;
        this.ratingReview = ratingReview;}

    // getters and setters
    public UUID getComicId() {return comicId;}
    public void setComicId(UUID comicId) {this.comicId = comicId;}
    public String getComicTitle() {return comicTitle;}
    public void setComicTitle(String comicTitle) {this.comicTitle = comicTitle;}
    public String getComicIssue() {return comicIssue;}
    public void setComicIssue(String comicIssue) {this.comicIssue = comicIssue;}
    public Integer getComicStartYear() {return comicStartYear;}
    public void setComicStartYear(Integer comicStartYear) {this.comicStartYear = comicStartYear;}
    public String getComicDesc() {return comicDesc;}
    public void setComicDesc(String comicDesc) {this.comicDesc = comicDesc;}
    public UUID getRatingId() {return ratingId;}
    public void setRatingId(UUID ratingId) {this.ratingId = ratingId;}
    public Integer getRatingScore() {return ratingScore;}
    public void setRatingScore(Integer ratingScore) {this.ratingScore = ratingScore;}
    public String getRatingReview() {return ratingReview;}
    public void setRatingReview(String ratingReview) {this.ratingReview = ratingReview;}
}
