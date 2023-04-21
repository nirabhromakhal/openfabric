# Example APIs for interacting with local docker engine using Java SpringBoot

### Get PostgreSQL

Download and run PostgreSQL server on localhost.
Create a new user or database and/or edit the .env file to allow liquibase to connect to your postgre datasource.

### Get Docker

Get docker desktop installed and running with default settings.
Pull some test images like ```testcontainers/helloworld``` to test our application.

### Start the Spring Application and test the APIs

Go to http://localhost:8080/swagger-ui/#/ and test the APIs.

The ```create``` API is used to create a container by specifying an image, name and optionally at most 1 port.

The ```start``` and ```stop``` APIs are used to start and stop containers by specifying names (container names are unique in Docker)

The ```info``` API gets the worker details.

The ```list``` API gets all the workers.

The ```stats``` API gives the stats of the workers.