package io.hasan.comiccatalogservice.Models;

public class Comic {

    private String comicId;
    private String comicName;
    private String comicDesc;

    public Comic(String comicId, String comicName, String comicDesc) {
        this.comicId = comicId;
        this.comicName = comicName;
        this.comicDesc = comicDesc;
    }

    public String getComicId() {return comicId;}
    public void setComicId(String comicId) {this.comicId = comicId;}
    public String getComicName() {return comicName;}
    public void setComicName(String comicName) {this.comicName = comicName;}
    public String getComicDesc() {return comicDesc;}
    public void setComicDesc(String comicDesc) {this.comicDesc = comicDesc;}
}
