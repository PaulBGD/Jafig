package net.burngames.jafig;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents a Jafig object.
 * <p/>
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
