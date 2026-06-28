package io.hasan.comiccatalogservice.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// JPA database entity for ONE user-created folder/collection in the catalog service database
@Entity
@Table(name = "comic_collections")
public class Collection {


    @Id // primary key for each collection
    @GeneratedValue(strategy = GenerationType.UUID) // Hibernate generates UUID when new collection saved
    @Column(name = "collection_id")
    private UUID collectionId;

    // store human-readable collection name shown in frontend
    @Column(name = "collection_name", nullable = false, unique = true)
    private String collectionName;

    // TODO: Stores the comic IDs that belong to this collection.
    @ElementCollection(fetch = FetchType.EAGER) // one collection object owns a List<UUID comicID>
    // TODO: JPA stores the list in a small helper table linked by collection_id.
    @CollectionTable(name = "comic_collection_items", joinColumns = @JoinColumn(name = "collection_id"))
    // TODO: Each row in the helper table stores one comicId from comic-info-service.
    @Column(name = "comic_id", nullable = false)
    // TODO: Stores the list index so the collection keeps a stable display order.
    @OrderColumn(name = "comic_order")
    private List<UUID> comicIds = new ArrayList<>();

    // available for JPA to create entity objects when reading database rows
    public Collection() {}
    public Collection(String collectionName) {
        this.collectionName = collectionName;
    }

    // getters and setters
    public UUID getCollectionId() {return collectionId;}
    public void setCollectionId(UUID collectionId) {this.collectionId = collectionId;}
    public String getCollectionName() {return collectionName;}
    public void setCollectionName(String collectionName) {this.collectionName = collectionName;}
    public List<UUID> getComicIds() {return comicIds;}
    public void setComicIds(List<UUID> comicIds) {this.comicIds = comicIds;}
}
