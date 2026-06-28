package io.hasan.comiccatalogservice;

import io.hasan.comiccatalogservice.models.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<Collection, UUID> {

    // returns collections sorted by name for a predictable frontend list
    List<Collection> findAllByOrderByCollectionNameAsc();

    // checks duplicate collection names during create
    boolean existsByCollectionNameIgnoreCase(String collectionName);

    // checks duplicate collection names during update while ignoring the current collection
    boolean existsByCollectionNameIgnoreCaseAndCollectionIdNot(String collectionName, UUID collectionId);

    // finds every collection containing a comic so deletion can remove stale comic references across collection
    @Query("select collection from Collection collection join collection.comicIds comicId where comicId = :comicId")
    List<Collection> findCollectionsContainingComicId(@Param("comicId") UUID comicId);
}
