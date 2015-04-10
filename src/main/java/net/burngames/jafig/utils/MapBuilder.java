package net.burngames.jafig.utils;

import java.util.HashMap;

/**
 * An easy util for building hashmaps
 *
 * @author PaulBGD
 */
public class MapBuilder {

    private final HashMap<String, Object> map = new HashMap<String, Object>();

    public MapBuilder set(String path, Object object) {
        String[] split = path.split("\\.");
        HashMap<String, Object> currentMap = map;
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            if (i == splitLength - 1) {
                continue;
            }
            String s = split[i];
            if (currentMap.containsKey(s) && currentMap.get(s) instanceof HashMap) {
                continue;
            }
            HashMap<String, Object> newMap = new HashMap<String, Object>();
            currentMap.put(s, newMap);
            currentMap = newMap;
        }
        currentMap.put(split[split.length - 1], object);
        return this;
    }

    public HashMap<String, Object> getMap() {
        return map;
    }
}
