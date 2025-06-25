# Car count
## A test kafka consumer for unique vehicle per day counting

The program requires Kafka and Redis  

Config properties are defined in `Config.java`

To install dependencies:  

`mvn install` 

To run the consumer:  

`mvn exec:run`

To test producer + consumer integration:  

`mvn test`
