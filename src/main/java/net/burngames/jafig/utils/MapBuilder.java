/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Burn Games LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
