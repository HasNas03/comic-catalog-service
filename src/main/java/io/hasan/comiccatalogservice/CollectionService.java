package io.hasan.comiccatalogservice;

import io.hasan.comiccatalogservice.models.CatalogItem;
import io.hasan.comiccatalogservice.models.CollectionCatalog;
import io.hasan.comiccatalogservice.models.Collection;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

// service for collection/folder behavior inside comic-catalog-service
@Service
public class CollectionService {

    private final CollectionRepository CollectionRepository;
    private final ComicCatalogService comicCatalogService;

    public CollectionService(CollectionRepository CollectionRepository, ComicCatalogService comicCatalogService) {
        this.CollectionRepository = CollectionRepository;
        this.comicCatalogService = comicCatalogService;
    }

    // get all collections as frontend-ready DTOs
    public List<CollectionCatalog> getCollections() {
        return CollectionRepository.findAllByOrderByCollectionNameAsc()
                .stream()
                .map(collection -> getCollection(collection.getCollectionId()))
                .toList();
    }

    // Get one collection plus full CatalogItem objects for each comic inside it
    public CollectionCatalog getCollection(UUID collectionId) {
        // 1. load the collection row or throw 404 if it does not exist
        Collection collection = getCollectionOrThrow(collectionId);

        // 2. resolve each stored comicId into a CatalogItem
        List<CatalogItem> comics = collection.getComicIds()
                .stream()
                .map(this::getCatalogItemOrNull)
                .filter(Objects::nonNull)
                .toList();

        // return collectionCatalog object now usable for sending to frontend
        return new CollectionCatalog(
                collection.getCollectionId(),
                collection.getCollectionName(),
                comics
        );
    }

    // create a new collection folder
    public Collection addCollection(Collection collection) {
        // validate name
        validateCollectionName(collection.getCollectionName());
        // reject duplicate name
        if (CollectionRepository.existsByCollectionNameIgnoreCase(collection.getCollectionName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Collection name already exists");}

        // TODO: force null so JPA generates a fresh UUID instead of trusting client-supplied IDs.
        collection.setCollectionId(null);
        // TODO: start a new collection
        collection.getComicIds().clear();
        // TODO: save and return the persisted collection row
        return CollectionRepository.save(collection);
    }

    // update collection metadata like name/description
    public Collection updateCollection(UUID collectionId, Collection collectionUpdate) {
        // Load the existing collection or fail with 404.
        Collection collection = getCollectionOrThrow(collectionId);
        // validate new name
        validateCollectionName(collectionUpdate.getCollectionName());
        // reject a duplicate name
        if (CollectionRepository.existsByCollectionNameIgnoreCaseAndCollectionIdNot(collectionUpdate.getCollectionName(), collectionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection name already exists");}
        // apply updated name
        collection.setCollectionName(collectionUpdate.getCollectionName());
        // save
        return CollectionRepository.save(collection);
    }

    // Delete a collection and the stored comicId list
    public void deleteCollection(UUID collectionId) {
        // confirm the collection exists so deleting a missing collection returns 404
        getCollectionOrThrow(collectionId);
        // TODO: deleting the collection also deletes its @ElementCollection helper rows
        CollectionRepository.deleteById(collectionId);
    }

    //add an existing comic to an existing collection
    @Transactional
    public CollectionCatalog addComicToCollection(UUID collectionId, UUID comicId) {
        // confirm the collection exists before adding a comic ID to it
        Collection collection = getCollectionOrThrow(collectionId);
        // confirm the comic exists in comic-info-service through existing catalog flow
        comicCatalogService.getCatalogItem(comicId);
        // prevent duplicate comics in same collection
        if (collection.getComicIds().contains(comicId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Comic is already in this collection");
        }
        // add the comicId to the collection's stored list
        collection.getComicIds().add(comicId);
        // save the updated collection and its @ElementCollection rows
        CollectionRepository.save(collection);
        // return the updated collection page shape
        return getCollection(collectionId);
    }

    // remove a comic from a collection
    @Transactional
    public void removeComicFromCollection(UUID collectionId, UUID comicId) {
        // confirm the collection exists before removing from it
        Collection collection = getCollectionOrThrow(collectionId);
        // remove returns false if the comicId was not present
        boolean removed = collection.getComicIds().remove(comicId);
        // if comic is missing, return 404
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic is not in this collection");
        }
        // save the collection so JPA persists the updated comicId list
        CollectionRepository.save(collection);
    }

    // TODO: ---------------------------------- HELPERS ----------------------------------
    // shared collection lookup helper
    private Collection getCollectionOrThrow(UUID collectionId) {
        // findById returns Optional, so orElseThrow turns missing rows into an HTTP 404
        return CollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));
    }

    // resolve a comicId into a CatalogItem, but skip stale collection entries if the comic was deleted
    private CatalogItem getCatalogItemOrNull(UUID comicId) {
        try {
            return comicCatalogService.getCatalogItem(comicId);
        } catch (WebClientResponseException.NotFound ignored) {
            return null;
        }
    }

    // shared validation helper for collection names
    private void validateCollectionName(String collectionName) {
        // null names are invalid because the UI needs something to display
        if (collectionName == null || collectionName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection name is required");
        }
    }
}
