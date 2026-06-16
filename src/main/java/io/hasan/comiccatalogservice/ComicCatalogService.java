package io.hasan.comiccatalogservice;

import io.hasan.comiccatalogservice.models.CatalogItem;
import io.hasan.comiccatalogservice.models.Comic;
import io.hasan.comiccatalogservice.models.Rating;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ComicCatalogService {
    // attributes
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String COMIC_INFO_SERVICE_URL = "http://comic-info-service/comics";
    private static final String COMIC_RATING_SERVICE_URL = "http://comic-rating-service/ratings";
    private final WebClient.Builder webClientBuilder;

    // constructor
    public ComicCatalogService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;}

    // 1. get all Comics and their Ratings -> merge into List<CatalogItem>
    public List<CatalogItem> getCatalog() {
        // calls getComics() to comic-info-service to get List<Comic>.
        List<Comic> comics = getComics();
        // calls getRatings() to comic-rating-service to get List<Rating>.
        List<Rating> ratings = getRatings();
        // converts List<Rating> into  Map<UUID, Rating> for easier retrieval, key is each rating's comicId attribute
        Map<UUID, Rating> ratingsByComicId = ratings.stream()
                .collect(Collectors.toMap(Rating::getComicId, Function.identity(), (first, second) -> first));
        // for each comic object, find rating object in 'ratingsByComicId' and use both objects to create CatalogItem list
        return comics.stream()
                .map(comic -> toCatalogItem(comic, ratingsByComicId.get(comic.getComicId())))
                .toList();
    }
    // 2. get 1 Comic + its Rating
    public CatalogItem getCatalogItem(UUID id) {
        Comic comic = getComic(id); // 404 here => comic doesn't exist, propagates correctly
        Rating rating;
        try {
            rating = getRatingForComic(id);
        } catch (WebClientResponseException.NotFound e) {
            rating = null; // comic exists but has no rating yet
        }
        return toCatalogItem(comic, rating);
    }

    // 3. add a new comic
    public Comic addComic(Comic comic) {
        return webClientBuilder.build()
                .post().uri(COMIC_INFO_SERVICE_URL)
                .bodyValue(comic)
                .retrieve()
                .bodyToMono(Comic.class)
                .block(REQUEST_TIMEOUT);
    }
    // 4. update an existing comic
    public Comic updateComic(UUID comicId, Comic comic) {
        return webClientBuilder.build()
                .put().uri(COMIC_INFO_SERVICE_URL + "/" + comicId)
                .bodyValue(comic)
                .retrieve()
                .bodyToMono(Comic.class)
                .block(REQUEST_TIMEOUT);
    }
    // 5. delete an existing comic
    public void deleteComic(UUID comicId) {
        // delete the comic
        webClientBuilder.build()
                .delete().uri(COMIC_INFO_SERVICE_URL + "/" + comicId)
                .retrieve()
                .toBodilessEntity()
                .block(REQUEST_TIMEOUT);
        // delete it's associated rating (if exists)
        deleteRatingForComic(comicId);
    }

    // 6. add a Rating for an existing comic
    public Rating addRating(Rating rating) {
        verifyComicExists(rating);

        return webClientBuilder.build()
                .post().uri(COMIC_RATING_SERVICE_URL)
                .bodyValue(rating)
                .retrieve()
                .bodyToMono(Rating.class)
                .block(REQUEST_TIMEOUT);
    }
    // 7. update a Rating for an existing comic
    public Rating updateRating(UUID ratingId, Rating rating) {
        // TODO: check if this call is needed
        verifyComicExists(rating);

        return webClientBuilder.build()
                .put().uri(COMIC_RATING_SERVICE_URL + "/" + ratingId)
                .bodyValue(rating)
                .retrieve()
                .bodyToMono(Rating.class)
                .block(REQUEST_TIMEOUT);
    }
    // 8. delete a Rating for an existing comic
    public void deleteRating(UUID ratingId) {
        webClientBuilder.build()
                .delete()
                .uri(COMIC_RATING_SERVICE_URL + "/" + ratingId)
                .retrieve()
                .toBodilessEntity()
                .block(REQUEST_TIMEOUT);
    }

    // -------------------------- HELPERS --------------------------

    // get list of all Comics
    private List<Comic> getComics() {
        return webClientBuilder.build()
                // issues the GET http://comic-info-service/comics
                .get().uri(COMIC_INFO_SERVICE_URL)
                .retrieve()
                // deserializes the JSON response body into List<Comic>
                .bodyToMono(new ParameterizedTypeReference<List<Comic>>() {})
                // converts the reactive Mono<List<Comic>> into a synchronous blocking call, waiting up to 10 seconds
                .block(REQUEST_TIMEOUT);
    }
    // get list of all Ratings
    private List<Rating> getRatings() {
        return webClientBuilder.build()
                // issues the GET http://comic-rating-service/ratings
                .get().uri(COMIC_RATING_SERVICE_URL)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Rating>>() {})
                .block(REQUEST_TIMEOUT);
    }
    // merge Comic and Rating object into Catalog item
    private CatalogItem toCatalogItem(Comic comic, Rating rating) {
        if (rating == null) {
            return new CatalogItem(
                    comic.getComicId(),
                    comic.getComicTitle(),
                    comic.getComicIssue(),
                    Integer.parseInt(comic.getComicStartYear()),
                    comic.getComicDesc(),
                    null,
                    null,
                    null
            );
        }
        return new CatalogItem(
                comic.getComicId(),
                comic.getComicTitle(),
                comic.getComicIssue(),
                Integer.parseInt(comic.getComicStartYear()),
                comic.getComicDesc(),
                rating.getRatingId(),
                rating.getRatingScore(),
                rating.getRatingReview()
        );
    }
    // get a Comic
    private Comic getComic(UUID comicId) {
        return webClientBuilder.build()
                .get().uri(COMIC_INFO_SERVICE_URL + "/" + comicId)
                .retrieve()
                .bodyToMono(Comic.class)
                .block(REQUEST_TIMEOUT);
    }
    // get a Rating for comic based on comicId
    private Rating getRatingForComic(UUID comicId) {
        return webClientBuilder.build()
                .get().uri(COMIC_RATING_SERVICE_URL + "/comics/" + comicId) // + "/comics/"
                .retrieve()
                .bodyToMono(Rating.class)
                .block(REQUEST_TIMEOUT);
    }
    // verify if a comic exists
    private void verifyComicExists(Rating rating) {
        if (rating.getComicId() != null) {
            getComic(rating.getComicId());
        }
    }
    // delete Rating
    private void deleteRatingForComic(UUID comicId) {
        try {
            Rating rating = getRatingForComic(comicId);
            deleteRating(rating.getRatingId());
        } catch (WebClientResponseException.NotFound ignored) {
        }
    }
    //temp
//    List<CatalogItem> buildCatalog(List<Comic> comics, List<Rating> ratings) {
//        Map<UUID, Rating> ratingsByComicId = ratings.stream()
//                .collect(Collectors.toMap(Rating::getComicId, Function.identity(), (first, second) -> first));
//
//        return comics.stream()
//                .map(comic -> toCatalogItem(comic, ratingsByComicId.get(comic.getComicId())))
//                .toList();
//    }
}
