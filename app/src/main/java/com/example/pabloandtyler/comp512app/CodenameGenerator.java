
package com.example.pabloandtyler.comp512app;

import java.util.Random;
/**
 * @url https://github.com/googlesamples/android-nearby/blob/master/connections/rockpaperscissors/app/src/main/java/com/google/location/nearby/apps/rockpaperscissors/CodenameGenerator.java
 * Citation for reference on generating a semi-unique name for peer discovery
 */

final class CodenameGenerator {
    private static final String[] COLORS =
            new String[] {
                    "Amazon",
                    "Amber",
                    "Pink",
                    "Red",
                    "Orange",
                    "Yellow",
                    "Green",
                    "Blue",
                    "Indigo",
                    "Violet",
                    "Purple",
                    "Lavender",
                    "Fuchsia",
                    "Plum",
                    "Orchid",
                    "Magenta",
                    "Jeremy",
                    "Linda",
                    "Sukmoon",
                    "Omar",
                    "Thang",
                    "Hexi"


            };

    private static final String[] TREATS =
            new String[] {
                    "Alpha",
                    "Beta",
                    "Gamma",
                    "Theta",
                    "Cupcake",
                    "Donut",
                    "Eclair",
                    "Froyo",
                    "Gingerbread",
                    "Honeycomb",
                    "Ice Cream Sandwich",
                    "Jellybean",
                    "Kit Kat",
                    "Lollipop",
                    "Marshmallow",
                    "Nougat",
                    "Oreo",
                    "Hexi",
                    "Leader",
                    "Mutex",
                    "Blum",
                    "Null",
                    "Chang",
                    "El Ariss",
                    "Bui"
            };

    private static final Random generator = new Random();

    private CodenameGenerator() {}

    /** Generate a random friendly name*/
    static String generate() {
        String color = COLORS[generator.nextInt(COLORS.length)];
        String treat = TREATS[generator.nextInt(TREATS.length)];
        return color + " " + treat;
    }
}
