package com.omatheusmesmo.shoppmate.shared.testutils;

import net.datafaker.Faker;
import java.util.Locale;

public class FakerUtil {

    private static final Faker FAKER = new Faker(Locale.forLanguageTag("en-US"));

    public static Faker getFaker() {
        return FAKER;
    }
}
