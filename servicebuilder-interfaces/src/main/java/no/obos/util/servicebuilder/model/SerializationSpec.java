package no.obos.util.servicebuilder.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SerializationSpec {
    public static final String PRETTY_PRINT = "PRETTY_PRINT";
    public static final String TOLERATE_UNRECOGNIZED_FIELDS = "TOLERATE_UNRECOGNIZED_FIELDS";
    public static final String TOLERATE_MISSING_FIELDS = "TOLERATE_MISSING_FIELDS";
    public static final String ISO_DATES = "ISO_DATES";
    public static final String GUAVA_TYPES = "GUAVA_TYPES";

    private final Set<String> options;

    public Set<String> getOptions() {
        return new HashSet<>(options);
    }

    public static SerializationSpec create(String... options) {
        return new SerializationSpec(new HashSet<>(Arrays.asList(options)));
    }

    public final static SerializationSpec standard = create(
            PRETTY_PRINT,
            TOLERATE_MISSING_FIELDS,
            TOLERATE_UNRECOGNIZED_FIELDS,
            ISO_DATES,
            GUAVA_TYPES);


}
