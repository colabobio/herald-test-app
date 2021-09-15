package com.example.heraldtest;

import java.util.HashMap;

/**
 * Represents an epidemiological status for an individual in relation to a single pathogen/toxin.
 * Includes methods for marshalling to/from an integer
 */
public enum IllnessStatusCode {
    susceptible(1),
    infected(2), // Also same for infestation
    transmittable(3),
    illAndTransmittable(4), // aka has developed serious disease, and can transmit
    illAndNonTransmittable(5), // aka has developed serious disease, and cannot transmit
    recovered(6),
    vaccinated(7),
    immune(8),
    nonTransmittable(9); // E.g. host is deceased or isolated.

    // Integer map pattern

    private int value;
    private static HashMap map = new HashMap<>();

    private IllnessStatusCode(int value) {
        this.value = value;
    }

    static {
        for (IllnessStatusCode illnessStatus : IllnessStatusCode.values()) {
            map.put(illnessStatus.value, illnessStatus);
        }
    }

    public static IllnessStatusCode valueOf(int pageType) {
        return (IllnessStatusCode) map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}