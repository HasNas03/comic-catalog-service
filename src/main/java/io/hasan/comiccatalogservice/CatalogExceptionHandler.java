package io.hasan.comiccatalogservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class CatalogExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClientResponseException(WebClientResponseException exception) {
        if (exception.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Downstream response could not be decoded");
        }

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(exception.getResponseBodyAsString());
    }
}