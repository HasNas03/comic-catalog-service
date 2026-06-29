package io.hasan.comiccatalogservice.models;
import java.util.UUID;

// comic DTO
public class Comic {

    // attributes
    private UUID comicId;
    private String comicTitle;
    private String comicIssue;
    private String comicStartYear;
    private String comicDesc;
    private String comicImagePath;
    private String comicStatus;

    // constructor
    public Comic() {}
    public Comic(String comicTitle, String comicIssue, String comicStartYear, String comicDesc) {
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
    public String getComicImagePath() {return comicImagePath;}
    public void setComicImagePath(String comicImagePath) {this.comicImagePath = comicImagePath;}
    public String getComicStatus() {return comicStatus;}
    public void setComicStatus(String comicStatus) {this.comicStatus = comicStatus;}
}
