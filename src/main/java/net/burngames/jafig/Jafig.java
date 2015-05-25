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

package net.burngames.jafig;

import net.burngames.jafig.serialize.JafigSerializer;
import net.burngames.jafig.serialize.SerializeUtil;
import net.burngames.jafig.types.JafigObject;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * Represents a Jafig object.
 * As well, has a static method to
 * create a new Jafig object.
 *
 * @author PaulBGD
 */
public abstract class Jafig {

    protected static final Logger logger = Logger.getLogger("Jafig");

    public Jafig(Object[] parameters) {

    }

    /**
     * Creates a new Jafig object with the specified parameters
     *
     * @param jafig the type of Jafig to create
     * @param parameters the parameters to pass
     * @param <T> the type of Jafig
     * @return a new Jafig
     */
    public static <T extends Jafig> T create(Class<T> jafig, Object... parameters) {
        try {
            return jafig.getConstructor(Object[].class).newInstance(new Object[]{parameters});
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) new JafigObject(null);
    }

    public static void addSerializer(JafigSerializer serializer) {
        SerializeUtil.addSerializer(serializer);
    }

    public abstract <T> T load(Class<T> clazz);

    public abstract void save(Object object);

}
