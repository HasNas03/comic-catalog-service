**Comic Catalog Project**

- The Comic Catalog is a personal-use project aiming to develop a custom application for storing comic book reviews and reading progress


- This is a list of repositories for all microservices/servers used in this project
    - [Comic Catalog Microservice (primary client-facing API)](https://github.com/HasNas03/comic-catalog-service)
    - [Comic Info Service Microservice](https://github.com/HasNas03/comic-info-service)
    - [Comic Rating Microservice](https://github.com/HasNas03/comic-rating-service)
    - [Comic Catalog Discovery Server](https://github.com/HasNas03/discovery-server)


- The project is also a gateway for me to learn and practice:
    - backend development (Java & Spring) best practices
    - developing REST APIs
    - connecting to external APIs
    - cross-microservice integration/authentication
    - testing and security
    - external database integration
    - Cloud integration/hosting

- Technologies
    - Current technologies: Java, Spring (Boot, Web), Netflix Eureka, Maven, Git
    - Future technologies: (SQL/MongoDB), Docker, AWS, Spring Security
---

---
**Comic Catalog Microservice**

- The catalog service is responsible for instantiating intern API calls to the appropriate microservices. This is done to retrieve rating/comic info so that it can be formatted for the client response
- The Comic Rating API provides REST endpoints for the Comic Catalog Microservice to interact with the Rating database


- Architecture (suggestions/improvements are welcome/encouraged!):
  - **CatalogItem (Model)**
    - CatalogItem(String comicId, String comicName, String comicDesc, int comicRating)
    - DTO used in API responses to combine rating and comic information for responses

  - **CatalogController**
    - REST controller for the Catalog microservice (/catalog)

  - **CatalogService**
    - Service layer containing the business logic
