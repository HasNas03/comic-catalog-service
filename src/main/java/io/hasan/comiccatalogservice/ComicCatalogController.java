package io.hasan.comiccatalogservice;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(method= RequestMethod.GET,value="/catalog")
public class ComicCatalogController {

    @RequestMapping(method= RequestMethod.GET,value="/{userId}")
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
        // this will return the big response, which requires:
        // 1. userId - already have
        // 2. comic id/name/desc - from comic-info-service
        // 3. comic rating - from comic-rating-service
        // list of catalog items, where each item is comic (2.) + rating (3.)

        //temp
        return Collections.singletonList(
                new CatalogItem("7", "Secret Wars", "final event", 10));
    }

}
