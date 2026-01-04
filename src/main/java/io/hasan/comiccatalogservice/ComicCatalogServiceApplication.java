package io.hasan.comiccatalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ComicCatalogServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(ComicCatalogServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    // adding .Builder registers a builder that Spring Cloud can reliably “decorate” with
    // the load-balancer exchange filter, which asks eureka
    public WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }

}
