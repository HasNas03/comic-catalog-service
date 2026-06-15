## Comic Catalog Project

- The Comic Catalog is a personal-use project aiming to develop a custom application for storing comic book reviews and reading progress


- This is a list of repositories for all microservices/servers used in this project
    - [Comic Catalog Microservice (primary client-facing API)](https://github.com/HasNas03/comic-catalog-service)
    - [Comic Info Service Microservice](https://github.com/HasNas03/comic-info-service)
    - [Comic Rating Microservice](https://github.com/HasNas03/comic-rating-service)
    - [Comic Catalog Discovery Server](https://github.com/HasNas03/discovery-server)


- The project is also a gateway for me to practice:
    - backend development (Java & Spring) best practices
    - working with REST APIs
    - cross-microservice integration/authentication
    - testing and security
    - database integration
    - cloud integration/hosting


- Technologies
    - Current technologies                 : Java, Spring (Boot, Web), Netflix Eureka, Maven, Git
    - Future best practices/implementations: external databases, isolate env. variables/secrets (e.g. Vault and/or external configs), high fault tolerance (e.g. Netflix Hystrix)
    - Future technologies                  : (SQL/MongoDB), Docker, AWS, Spring Security
---

---
## Comic Catalog Microservice

- The frontend-facing API for the Comic Catalog project.
- This service does not own persistent data. It calls the info and rating services through a load-balanced 
- `WebClient.Builder`, using Eureka service names instead of hardcoded ports.

### Models
`Comic DTO`
- `UUID id`
- `String title`
- `String publisher`
- `Integer startYear`
- `String description`

`Rating DTO`
- `UUID id`
- `UUID comicId`
- `int score`
- `String reviewText`

`CatalogItem`
- `UUID comicId`
- `String title`
- `String publisher`
- `Integer startYear`
- `String description`
- `UUID ratingId`
- `Integer score`
- `String reviewText`

### Endpoints

Combined catalog view:

```text
GET /catalog
```

Comic forwarding:

```text
GET    /catalog/comics
GET    /catalog/comics/{id}
POST   /catalog/comics
PUT    /catalog/comics/{id}
DELETE /catalog/comics/{id}
```

Rating forwarding:

```text
GET    /catalog/ratings
GET    /catalog/ratings/comics/{comicId}
POST   /catalog/ratings
PUT    /catalog/ratings/{id}
DELETE /catalog/ratings/{id}