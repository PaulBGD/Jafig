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

package net.burngames.jafig.serialize;

import net.burngames.jafig.serialize.types.SerializedValue;

import java.lang.reflect.Field;

/**
 * @author PaulBGD
 */
public abstract class JafigSerializer<T> {

    private final Class<T> tClass;
    private final Class<? extends T>[] others;

    public JafigSerializer(Class<T> tClass, Class<? extends T>... others) {
        this.tClass = tClass;
        this.others = others;
    }

    public Class<T> getTClass() {
        return tClass;
    }

    public Class<? extends T>[] getOthers() {
        return others;
    }

    public abstract SerializedValue serialize(T t, Field field);

    public abstract T deserialize(SerializedValue value, Field field);

}
