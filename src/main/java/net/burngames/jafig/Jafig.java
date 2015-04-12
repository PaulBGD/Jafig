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

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a Jafig object.
 * As well, has a static method to
 * create a new Jafig object.
 *
 * @author PaulBGD
 */
public abstract class Jafig {

    public Jafig(Object[] parameters) {

    }

    public static Jafig create(Class<? extends Jafig> jafig, Object... parameters) {
        try {
            return jafig.getConstructor(Object[].class).newInstance(new Object[]{parameters});
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return new Jafig(parameters) {
            @Override
            public <T> T load(Class<T> clazz) {
                return null;
            }

            @Override
            public void save(Object object) {

            }
        };
    }

    public abstract <T> T load(Class<T> clazz);

    public abstract void save(Object object);

}
