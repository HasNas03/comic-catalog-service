package io.hasan.comiccatalogservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.hasan.comiccatalogservice.models.CatalogItem;
import io.hasan.comiccatalogservice.models.Comic;
import io.hasan.comiccatalogservice.models.Rating;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class ComicCatalogController {

    private final ComicCatalogService comicCatalogService;

    public ComicCatalogController(ComicCatalogService comicCatalogService) {
        this.comicCatalogService = comicCatalogService;}

    // ------------------------------------ COMICS -----------------------------
    // retrieves all comics + ratings
    @GetMapping
    public List<CatalogItem> getCatalog() {
        return comicCatalogService.getCatalog();}

    // retrieve ONE comic + its rating
    @GetMapping("/{id}")
    public CatalogItem getCatalogItem(@PathVariable UUID id) {
        return comicCatalogService.getCatalogItem(id);}

    // send request to create one Comic entry
    @PostMapping("/comics")
    public ResponseEntity<Comic> addComic(@RequestBody Comic comic) {
        Comic created = comicCatalogService.addComic(comic);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);}

    // send request to update a Comic entry
    @PutMapping("/comics/{id}")
    public Comic updateComic(@PathVariable UUID id, @RequestBody Comic comic) {
        return comicCatalogService.updateComic(id, comic);}

    // send request to delete a Comic entry
    @DeleteMapping("/comics/{id}")
    public ResponseEntity<Void> deleteComic(@PathVariable UUID id) {
        comicCatalogService.deleteComic(id);
        return ResponseEntity.noContent().build();}

    // ------------------------------------ RATINGS -----------------------------

    // send request to create a Rating entry
    @PostMapping("/ratings")
    public ResponseEntity<Rating> addRating(@RequestBody Rating rating) {
        Rating created = comicCatalogService.addRating(rating);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);}

    // send request to modify a Rating entry
    @PutMapping("/ratings/{id}")
    public Rating updateRating(@PathVariable UUID id, @RequestBody Rating rating) {
        return comicCatalogService.updateRating(id, rating);}

    // send request to delete a Rating entry
    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID id) {
        comicCatalogService.deleteRating(id);
        return ResponseEntity.noContent().build();}
}