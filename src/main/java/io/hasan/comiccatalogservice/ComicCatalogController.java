package io.hasan.comiccatalogservice;

import io.hasan.comiccatalogservice.Models.CatalogItem;
import io.hasan.comiccatalogservice.Models.UserRatings;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(method= RequestMethod.GET,value="/catalog")
public class ComicCatalogController {

    private final WebClient webClient;

    public ComicCatalogController(WebClient webClient) {
        this.webClient = webClient;
    }

    @RequestMapping(method= RequestMethod.GET,value="/{userId}")
    public UserRatings getCatalog(@PathVariable("userId") String userId){
        // this will return the big response, which requires:
        // 1. userId
        // 2. comic rating - from comic-rating-service
        UserRatings user_ratings = webClient
                .get()
                .uri("http://localhost:8083/ratings/"+userId)
                .retrieve()
                .bodyToMono(UserRatings.class)
                .block(Duration.ofSeconds(5));

        // 3. using comicIds from user_ratings, get comic info from comic-info-service
        // to do

        // 4. create list of CatalogItem objects, where each item is getting its attribute ingo from 2. and 3.
        // to do

        //temp
        return user_ratings;
    }

}
