# Gateway application

The Gateway Application serves as an interface for handling JSON and XML requests, 
managing sessions, and integrating with RabbitMQ for message queuing.
RabbitMQ is used for asynchronous message processing.
Redis is used for caching session data to improve performance.
This service-oriented architecture keeps the application modular, scalable, and maintainable.


## How to run Gateway Application locally

Install Docker Compose

Run PostgreSQL, RabbitMQ and Redis containers

```
docker-compose up -d

docker-compose ps
```
Build and run the application
```
mvn clean package
java -jar target/gateway-application.jar
```
gateway-0.0.1-SNAPSHOT.jar
### Access Swagger UI

API documentation:

http://localhost:8080/swagger-ui/index.html

### Clearing PostgreSQL Data
Open Terminal

#### Find PostgreSQL Container ID or Name:
Use the following sh command to list all running Docker containers and find the container ID or name for your PostgreSQL container:
```
docker ps
```
#### Access PostgreSQL Container:
Use the following sh command to access the PostgreSQL container's shell:
```
docker exec -it postgres bash
```
#### Connect to PostgreSQL:
Use the psql command to connect to the PostgreSQL database.
```
psql -U postgres -d gateway
```
#### Clear Data:
Execute SQL commands to clear the data from your tables:
```
TRUNCATE TABLE request, session CASCADE;
```
#### Exit:
```
gateway=# \q
(or exit)
```
### Clearing Redis Data
#### Find Redis Container ID or Name:
Use the following sh command to list all running Docker containers and find the container ID or name for your Redis container:
```
docker ps
```
#### Access Redis Container:
Use the following sh command to access the Redis container's shell:
```
docker exec -it redis sh
```
#### Connect to Redis:
Use the redis-cli command to connect to the Redis instance:
```
redis-cli
```
#### Clear Data:
Use the following command to clear all data from Redis:
```
FLUSHALL
```
#### Exit:
```
exit
```
### Sample Insert Request (POST /json_api/insert)
* Request URL: http://localhost:8080/json_api/insert
* Method: POST
* Body (json):
```
{
"requestId": "b89577fe-8c37-4962-8af3-7cb89a245160",
"timestamp": 1586335186721,
"producerId": "1234",
"sessionId": "47966003032113150"
}
```

### Sample Find Request (POST /json_api/find)

* Request URL: http://localhost:8080/json_api/find
* Method: POST
* Body (json):
```
{
"requestId": "b89577fe-8c37-4962-8af3-7cb89a245160",
"sessionId": "47966003032113150"
}
```

### Sample Insert Command (POST /xml_api/command)
* Request URL: http://localhost:8080/xml_api/command
* Method: POST
* Body (XML):
```
<command id="1234">
  <enter session="13617162">
    <timestamp>1586335186721</timestamp>
    <player>238485</player>
  </enter>
</command>
```
### Sample Find Command (POST /xml_api/command)
* Request URL: http://localhost:8080/xml_api/command
* Method: POST
* Body (XML):
```
<command id="1234-8785">
  <get session="13617162" />
</command>
```

