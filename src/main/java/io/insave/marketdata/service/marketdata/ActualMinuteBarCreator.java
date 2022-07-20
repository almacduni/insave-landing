package io.insave.marketdata.service.marketdata;

import io.insave.marketdata.entity.Bar;
import io.insave.marketdata.entity.TimeFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class ActualMinuteBarCreator {

    public static final long SECOND_IN_MILLIS = 1000;
    public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

    private final MongoTemplate mongoTemplate;

    public void saveOrUpdateBar(Bar bar, String ticker, TimeFrame timeFrame, long timeFrameInterval) {
        long date = bar.getDate();
        long offsetTime = date % timeFrameInterval;
        long openTime = date - offsetTime;

        bar.setTicker(ticker);

        Query query = new Query();
        query.addCriteria(Criteria.where("date").is(openTime));
        String collectionName = ticker + getCollectionPostfix(timeFrame);

        Optional<Bar> currentBarOptional = Optional.ofNullable(
                mongoTemplate.findOne(query, Bar.class, collectionName));

        if (currentBarOptional.isPresent()) {
            Bar currentBar = currentBarOptional.get();
            currentBar.setClose(bar.getClose());
            if (currentBar.getHigh().compareTo(bar.getHigh()) < 0) {
                currentBar.setHigh(bar.getHigh());
            }
            if (currentBar.getLow().compareTo(bar.getLow()) > 0) {
                currentBar.setLow(bar.getLow());
            }
            log.debug(timeFrame.getPolygonTimeFrame() + " bar: " + currentBar);
            mongoTemplate.findAndReplace(query, currentBar, collectionName);
        } else {
            bar.setDate(openTime);
            log.debug("New Bar Created =================================== ");
            mongoTemplate.save(bar, collectionName);
        }
    }

    public abstract String getCollectionPostfix(TimeFrame timeFrame);

}
