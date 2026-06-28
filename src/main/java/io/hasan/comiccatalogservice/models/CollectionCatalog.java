package io.hasan.comiccatalogservice.models;

import java.util.List;
import java.util.UUID;

// DTO of the Collection object sent to frontend by catalog
// basically a Collection but instead of List <comicId> it resolves that and sends a List <catalogItem> with the Collection attributes
public class CollectionCatalog {

    // Collection ID from catalog database
    private UUID collectionId;
    // Collection display name
    private String collectionName;
    // list of fully resolved comics (so including comic metadata and rating fields)
    private List<CatalogItem> comics;

    public CollectionCatalog() {}
    public CollectionCatalog(UUID collectionId, String collectionName, List<CatalogItem> comics) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.comics = comics;
    }

    // getters and setters
    public UUID getCollectionId() {return collectionId;}
    public void setCollectionId(UUID collectionId) {this.collectionId = collectionId;}
    public String getCollectionName() {return collectionName;}
    public void setCollectionName(String collectionName) {this.collectionName = collectionName;}
    public List<CatalogItem> getComics() {return comics;}
    public void setComics(List<CatalogItem> comics) {this.comics = comics;}
}
