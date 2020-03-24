package no.obos.util.template.dto;

import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class FakerUtil {
    public static LocalDate dateBetween(Faker instance, String start, String end) {
        return utilDateToLocalDate(utilDateBetween(instance, start, end));
    }

    private static LocalDate utilDateToLocalDate(Date date) {
        return date.toInstant()
        .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date utilDateBetween(Faker instance, String start, String end) {
        return instance.date().between(
                asDate(start),
                asDate(end));
    }

    private static Date asDate(String start) {
        return Date.from(LocalDate.parse(start).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Faker getFaker(Random random) {
        return random == null
            ? Faker.instance(new Locale("nb", "NO"))
            : Faker.instance(new Locale("nb", "NO"), random);
    }
}
