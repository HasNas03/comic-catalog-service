package io.hasan.comiccatalogservice.Models;
import java.util.UUID;

public class Comic {

    // attributes
    private UUID comicId;
    private String comicTitle;
    private String comicIssue;
    private String comicStartYear;
    private String comicDesc;

    // constructor
    public Comic() {}
    public Comic(UUID comicId, String comicTitle, String comicIssue, String comicStartYear, String comicDesc) {
        this.comicId = comicId;
        this.comicTitle = comicTitle;
        this.comicIssue = comicIssue;
        this.comicStartYear = comicStartYear;
        this.comicDesc = comicDesc;}

    // getters and setters
    public UUID getComicId() {return comicId;}
    public void setComicId(UUID comicId) {this.comicId = comicId;}
    public String getComicTitle() {return comicTitle;}
    public void setComicTitle(String comicTitle) {this.comicTitle = comicTitle;}
    public String getComicIssue() {return comicIssue;}
    public void setComicIssue(String comicIssue) {this.comicIssue = comicIssue;}
    public String getComicStartYear() {return comicStartYear;}
    public void setComicStartYear(String comicStartYear) {this.comicStartYear = comicStartYear;}
    public String getComicDesc() {return comicDesc;}
    public void setComicDesc(String comicDesc) {this.comicDesc = comicDesc;}
}
