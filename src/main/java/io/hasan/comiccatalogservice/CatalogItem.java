package io.hasan.comiccatalogservice;

public class CatalogItem {

    // attributes
    String comicName;
    String comicDesc;
    private int comicRating;

    // constructors
    public CatalogItem(){};
    public CatalogItem(String comicName, String comicDesc, int comicRating) {
        this.comicName = comicName;
        this.comicDesc = comicDesc;
        this.comicRating = comicRating;
    }

    // getters and setters
    public String getComicName() {return comicName;}
    public void setComicName(String comicName) {this.comicName = comicName;}
    public String getComicDesc() {return comicDesc;}
    public void setComicDesc(String comicDesc) {this.comicDesc = comicDesc;}
    public int getComicRating() {return comicRating;}
    public void setComicRating(int comicRating) {this.comicRating = comicRating;}
}
