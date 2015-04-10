package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;

/**
 * Different types of Jafig files you can currently use.
 *
 * @author PaulBGD
 */
public class JafigType {

    public static final Class<? extends Jafig> YAML = JafigYAML.class;
    public static final Class<? extends Jafig> JSON = JafigJSON.class;

}
