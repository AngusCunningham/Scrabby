package com.guscodes.scrabby;

import java.util.*;

public class Data {
    private static final Integer[] DLS_LOCATIONS_ARRAY = {3, 11, 36, 38, 45, 52, 59, 92, 96, 98, 102, 108, 116, 122,
                                                            126, 128, 132, 165, 172, 179, 186, 188, 213, 221};
    private static final Integer[] TLS_LOCATIONS_ARRAY = {20, 24, 76, 80, 84, 88, 136, 140, 144, 148, 200, 204};
    private static final Integer[] DWS_LOCATIONS_ARRAY = {16, 28, 32, 42, 48, 56, 64, 70, 112, 154, 160, 168, 176, 182,
                                                                                                    192, 196, 208};
    private static final Integer[] TWS_LOCATIONS_ARRAY = {0, 7, 14, 105, 119, 210, 217, 224};

    public static final Set<Integer> DLS_LOCATIONS = new HashSet<>(Arrays.asList(DLS_LOCATIONS_ARRAY));
    public static final Set<Integer> TLS_LOCATIONS = new HashSet<>(Arrays.asList(TLS_LOCATIONS_ARRAY));
    public static final Set<Integer> DWS_LOCATIONS = new HashSet<>(Arrays.asList(DWS_LOCATIONS_ARRAY));
    public static final Set<Integer> TWS_LOCATIONS = new HashSet<>(Arrays.asList(TWS_LOCATIONS_ARRAY));

    public static final int MAX_TRAY_SIZE = 7;
    public static final int EMPTY_RACK_BONUS = 7;
    public static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private static Map<Character, Integer> TILE_POINTS = new HashMap<>();
    static {
        TILE_POINTS.put('A', 1);
        TILE_POINTS.put('B', 3);
        TILE_POINTS.put('C', 3);
        TILE_POINTS.put('D', 2);
        TILE_POINTS.put('E', 1);
        TILE_POINTS.put('F', 4);
        TILE_POINTS.put('G', 2);
        TILE_POINTS.put('H', 4);
        TILE_POINTS.put('I', 1);
        TILE_POINTS.put('J', 8);
        TILE_POINTS.put('K', 5);
        TILE_POINTS.put('L', 1);
        TILE_POINTS.put('M', 3);
        TILE_POINTS.put('N', 1);
        TILE_POINTS.put('O', 1);
        TILE_POINTS.put('P', 3);
        TILE_POINTS.put('Q', 10);
        TILE_POINTS.put('R', 1);
        TILE_POINTS.put('S', 1);
        TILE_POINTS.put('T', 1);
        TILE_POINTS.put('U', 1);
        TILE_POINTS.put('V', 4);
        TILE_POINTS.put('W', 4);
        TILE_POINTS.put('X', 8);
        TILE_POINTS.put('Y', 4);
        TILE_POINTS.put('Z', 10);
        TILE_POINTS.put('~', 0);
        for (char lowerCaseLetter : "abcdefghijklmnopqrstuvwxyz".toCharArray()) {
            TILE_POINTS.put(lowerCaseLetter, 0);
        }
    }

    public static final Map<Character, Integer> LETTER_SCORES = new HashMap<>(TILE_POINTS);



}
