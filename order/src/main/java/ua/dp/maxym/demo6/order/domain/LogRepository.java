package ua.dp.maxym.demo6.order.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface LogRepository extends MongoRepository<Log, String> {

    default void log(String logMessage, Object... args) {
        insert(new Log(new Date(), String.format(logMessage, args)));
    }

}
