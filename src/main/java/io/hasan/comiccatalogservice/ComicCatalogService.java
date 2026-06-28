package io.hasan.comiccatalogservice;

import io.hasan.comiccatalogservice.models.CatalogItem;
import io.hasan.comiccatalogservice.models.Comic;
import io.hasan.comiccatalogservice.models.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayOutputStream;
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
    private final CollectionRepository CollectionRepository;

    // constructor used by Spring during normal application startup
    @Autowired
    public ComicCatalogService(WebClient.Builder webClientBuilder, CollectionRepository CollectionRepository) {
        this.webClientBuilder = webClientBuilder;
        this.CollectionRepository = CollectionRepository;}

    // constructor kept for older unit tests that manually create ComicCatalogService without Spring/JPA
    public ComicCatalogService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
        this.CollectionRepository = null;}

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
    @Transactional
    public void deleteComic(UUID comicId) {
        // delete the comic from comic-info-service, which owns comic metadata
        webClientBuilder.build()
                .delete().uri(COMIC_INFO_SERVICE_URL + "/" + comicId)
                .retrieve()
                .toBodilessEntity()
                .block(REQUEST_TIMEOUT);
        // delete its associated rating from comic-rating-service if one exists
        deleteRatingForComic(comicId);
        // remove this comicId from any catalog-owned collections so folders do not point at a deleted comic
        if (CollectionRepository != null) {
            CollectionRepository.findCollectionsContainingComicId(comicId).forEach(collection -> {
                collection.getComicIds().remove(comicId);
                CollectionRepository.save(collection);
            });
        }
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

    // 9. add comic cover image
    public Comic addComicImage(UUID comicId, MultipartFile image) {
        // helper builds the multipart request and sends it to comic-info-service,
        // with POST meaning "ADD a cover image to this comic"
        return sendComicImage(comicId, image, true);
    }

    // 10. update comic cover image
    public Comic updateComicImage(UUID comicId, MultipartFile image) {
        // helper builds the multipart request and sends it to comic-info-service,
        // with PUT meaning "UPDATE a cover image to this comic"
        return sendComicImage(comicId, image, false);
    }

    // 11. get a comic cover image
    public byte[] getComicImage(UUID comicId) {
        // build the internal comic-info-service URL using the service name registered in Eureka
        String imageUrl = COMIC_INFO_SERVICE_URL + "/" + comicId + "/image";

        // Fetch the image as raw DataBuffer chunks instead of bodyToMono(byte[].class).
        // In this Spring/WebClient setup, decoding image/jpeg directly into byte[] produced an empty response.
        // DataBuffer lets us stream the raw HTTP body bytes without asking WebClient to understand JPEG.
        return webClientBuilder.build()
                .get().uri(imageUrl)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .reduce(new ByteArrayOutputStream(), (outputStream, dataBuffer) -> {
                    // Allocate exactly enough space for this response chunk.
                    byte[] chunk = new byte[dataBuffer.readableByteCount()];
                    // Copy the chunk bytes out of the DataBuffer.
                    dataBuffer.read(chunk);
                    // Append this chunk to the full image response.
                    outputStream.writeBytes(chunk);
                    // Release the DataBuffer so Netty/Spring can clean up pooled memory.
                    DataBufferUtils.release(dataBuffer);
                    // Return the same output stream so reduce can keep accumulating chunks.
                    return outputStream;
                })
                // Convert the accumulated stream into the final byte[] returned to the controller.
                .map(ByteArrayOutputStream::toByteArray)
                // Block because this service is written in a simple synchronous MVC style for now.
                .block(REQUEST_TIMEOUT);
    }

    // 12. delete a comic cover image
    public void deleteComicImage(UUID comicId) {
        // Forward the delete request to comic-info-service, which owns image storage.
        webClientBuilder.build()
                .delete().uri(COMIC_INFO_SERVICE_URL + "/" + comicId + "/image")
                .retrieve()
                // We only need to know the request succeeded; there is no useful response body.
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
                    comic.getComicImagePath(),
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
                comic.getComicImagePath(),
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
    // Shared helper for add/update image uploads.
    private Comic sendComicImage(UUID comicId, MultipartFile image, boolean create) {
        // MultipartBodyBuilder creates the form-data body expected by comic-info-service.
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // MultipartFile wraps the uploaded file from the frontend.
        // getResource() exposes the file as streamable bytes for WebClient.
        Resource imageResource = image.getResource();

        // The part name "image" must match @RequestPart("image") in comic-info-service.
        // The filename is forwarded so the downstream request still looks like a normal file upload.
        bodyBuilder.part("image", imageResource)
                .filename(image.getOriginalFilename());

        // POST adds an image for the first time; PUT replaces an existing image.
        WebClient.RequestBodySpec request = create
                ? webClientBuilder.build().post().uri(COMIC_INFO_SERVICE_URL + "/" + comicId + "/image")
                : webClientBuilder.build().put().uri(COMIC_INFO_SERVICE_URL + "/" + comicId + "/image");

        // Send the multipart body to comic-info-service and deserialize the updated Comic it returns.
        return request
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(Comic.class)
                .block(REQUEST_TIMEOUT);
    }

}
