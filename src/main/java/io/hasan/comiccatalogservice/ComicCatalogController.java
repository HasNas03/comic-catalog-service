package io.hasan.comiccatalogservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.hasan.comiccatalogservice.models.CatalogItem;
import io.hasan.comiccatalogservice.models.CollectionCatalog;
import io.hasan.comiccatalogservice.models.Collection;
import io.hasan.comiccatalogservice.models.Comic;
import io.hasan.comiccatalogservice.models.Rating;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class ComicCatalogController {

    private final ComicCatalogService comicCatalogService;
    private final CollectionService CollectionService;

    public ComicCatalogController(ComicCatalogService comicCatalogService, CollectionService CollectionService) {
        this.comicCatalogService = comicCatalogService;
        this.CollectionService = CollectionService;}

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


    // ------------------------------------ COLLECTIONS -----------------------------

    // retrieve all collection folders stored by catalog
    @GetMapping("/collections")
    public List<CollectionCatalog> getCollections() {
        // Return frontend-ready collection DTOs instead of raw JPA entities.
        return CollectionService.getCollections();
    }

    // retrieve one collection folder plus its resolved catalog items
    @GetMapping("/collections/{collectionId}")
    public CollectionCatalog getCollection(@PathVariable UUID collectionId) {
        // The service loads collection membership rows, then resolves each comicId into CatalogItem data.
        return CollectionService.getCollection(collectionId);
    }

    // create a new collection folder
    @PostMapping("/collections")
    public ResponseEntity<Collection> addCollection(@RequestBody Collection collection) {
        // The request body contains collectionName and optional collectionDesc.
        Collection created = CollectionService.addCollection(collection);
        // 201 tells the frontend a new persistent collection row was created.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // update a collection folder's name/description
    @PutMapping("/collections/{collectionId}")
    public Collection updateCollection(@PathVariable UUID collectionId, @RequestBody Collection collection) {
        // The path ID decides which collection is edited; the body provides the new values.
        return CollectionService.updateCollection(collectionId, collection);
    }

    // delete a collection folder and its saved comic memberships
    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID collectionId) {
        // Deleting the folder does not delete comics from comic-info-service.
        CollectionService.deleteCollection(collectionId);
        // 204 means delete succeeded and no response body is needed.
        return ResponseEntity.noContent().build();
    }

    // add an existing comic to an existing collection folder
    @PostMapping("/collections/{collectionId}/comics/{comicId}")
    public CollectionCatalog addComicToCollection(@PathVariable UUID collectionId, @PathVariable UUID comicId) {
        // The service verifies both IDs, saves the membership row, and returns the updated collection.
        return CollectionService.addComicToCollection(collectionId, comicId);
    }

    // remove one comic from one collection folder
    @DeleteMapping("/collections/{collectionId}/comics/{comicId}")
    public ResponseEntity<Void> removeComicFromCollection(@PathVariable UUID collectionId, @PathVariable UUID comicId) {
        // Removing from a collection only deletes the membership row, not the comic itself.
        CollectionService.removeComicFromCollection(collectionId, comicId);
        // 204 means the membership was removed and no response body is needed.
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------ COMIC COVER IMAGE -----------------------------

    @PostMapping(value = "/comics/{comicId}/image", consumes = "multipart/form-data")
    public Comic addComicImage(@PathVariable UUID comicId, @RequestPart("image") MultipartFile image) {
        // Receives the uploaded cover image file from the frontend
        // forwards image to comic-info-service
        // comicId identifies which existing Comic record should receive this cover image via matching the comicId
        return comicCatalogService.addComicImage(comicId, image);
    }

    @PutMapping(value = "/comics/{comicId}/image", consumes = "multipart/form-data")
    public Comic updateComicImage(@PathVariable UUID comicId, @RequestPart("image") MultipartFile image) {
        // receives a replacement cover image file for an existing comic
        // CatalogService forwards this as a PUT request to comic-info-service, overwriting the old image
        return comicCatalogService.updateComicImage(comicId, image);
    }

    @GetMapping(value = "/comics/{comicId}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getComicImage(@PathVariable UUID comicId) {
        // Ask catalog service to fetch the raw image bytes from comic-info-service.
        byte[] image = comicCatalogService.getComicImage(comicId);

        // Return those bytes as a real JPEG response so browsers can render it in an img tag.
        // contentLength is not required, but it helps confirm the response is not empty.
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(image.length)
                .body(image);
    }

    @DeleteMapping("/comics/{comicId}/image")
    public ResponseEntity<Void> deleteComicImage(@PathVariable UUID comicId) {
        // Tell comic-info-service to delete the stored image file and clear comicImagePath.
        comicCatalogService.deleteComicImage(comicId);
        // 204 means the delete succeeded and there is no response body to return.
        return ResponseEntity.noContent().build();
    }

}
