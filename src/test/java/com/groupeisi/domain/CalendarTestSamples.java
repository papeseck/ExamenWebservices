package com.groupeisi.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CalendarTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Calendar getCalendarSample1() {
        return new Calendar().id(1L).date(1L).dayofweek("dayofweek1");
    }

    public static Calendar getCalendarSample2() {
        return new Calendar().id(2L).date(2L).dayofweek("dayofweek2");
    }

    public static Calendar getCalendarRandomSampleGenerator() {
        return new Calendar().id(longCount.incrementAndGet()).date(longCount.incrementAndGet()).dayofweek(UUID.randomUUID().toString());
    }
}
