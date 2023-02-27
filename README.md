# Demo for Saga using Camunda Platform 7

Demo project for ordering stuff with a simple Ordering Saga utilizing [Camunda Platform 7](https://docs.camunda.org/get-started/) to take into account possible compensations in case ordering process fails mid-step.

Note that the newest version is Camunda 8, however Camunda 7 is open-source, while Camunda 8 is closed-source cloud-based SaaS solution.

* By [Maxym Mykhalchuk](https://blog.maxym.dp.ua), see [license](LICENSE)
* Built on Spring Boot 3 / Spring Framework 6
* REST microservices (no CQRS/Event-Sourcing)
* Uses MongoDB as storage

### To run
* Start MongoDB locally on port 27017 with no password
  * For example, using docker `docker run --env=MONGODB_REPLICA_SET_MODE=primary --env=ALLOW_EMPTY_PASSWORD=yes -p 27017:27017 --name demo6-mongodb -d bitnami/mongodb:latest`
  * To remove, `docker stop demo6-mongodb` and `docker rm demo6-mongodb`
* Then start all the services `./gradlew bootRun --parallel --max-workers 3`

### To test
* Open the services
  * [Inventory Service](http://localhost:8084)
  * [Payment Service](http://localhost:8083) to see remaining user credit
  * Main [Order Service](http://localhost:8081) to create orders and employ the Saga that uses Camunda Platform 7
* Open Saga monitoring using Camunda
  * Open [Camunda Cockpit](http://127.0.0.1:8081/camunda/app/cockpit/default/#/processes) with user `demo` and password `demo`
  * And click 'Order Workflow'
* Order anything via [Order Service](http://localhost:8081)
* Observe decreased user credit in [Payment Service](http://localhost:8083)
* Then, after some time, Saga will fail on the last step, and user credit will be restored