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

import net.burngames.jafig.annotations.Accept;
import net.burngames.jafig.annotations.Discard;
import net.burngames.jafig.annotations.Options;
import net.burngames.jafig.serialize.types.*;
import net.burngames.jafig.utils.Primitives;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

/**
 * Internal functions for representing data
 *
 * @author PaulBGD
 */
public class SerializeUtil {

    private static final HashMap<Class, JafigSerializer> serializers = new HashMap<Class, JafigSerializer>();

    public static SerializedValue serialize(Object value) {
        return serialize(value, null);
    }

    private static SerializedValue serialize(Object value, Field currentField) {
        if (value == null) {
            // nothing to do here
            return new SerializedPrimitive(null);
        }

        Class<?> theClass;
        if (currentField == null) {
            theClass = value.getClass();
        } else {
            theClass = currentField.getType();
        }

        if (serializers.containsKey(theClass)) {
            return serializers.get(theClass).serialize(value, currentField);
        } else if (value instanceof Class) {
            throw new IllegalArgumentException("Classes cannot be serialized");
        } else {
            if (value instanceof BigDecimal) {
                if (currentField == null) {
                    value = ((BigDecimal) value).floatValue();
                } else {
                    Class<?> currentClass = Primitives.unwrap(currentField.getClass());
                    if (currentClass == float.class) {
                        value = ((BigDecimal) value).floatValue();
                    } else if (currentClass == double.class) {
                        value = ((BigDecimal) value).doubleValue();
                    }
                }
            }
            Class<?> valueClass = value.getClass();
            if (Primitives.isWrapperType(valueClass)) {
                valueClass = Primitives.unwrap(valueClass); // unwrap
            }
            if (valueClass.isArray()) {
                Object[] array = (Object[]) value;
                SerializedValue[] values = new SerializedValue[array.length];
                for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
                    values[i] = serialize(array[i], currentField);
                }
                return new SerializedArray(values);
            } else if (value instanceof List) {
                List list = (List) value;
                List<SerializedValue> values = new ArrayList<SerializedValue>(list.size());
                for (Object aList : list) {
                    values.add(serialize(aList, currentField));
                }
                return new SerializedList(values);
            } else if (value instanceof Map) {
                // maps are tricky, in that we have to use a string-key format
                Map map = (Map) value;
                HashMap<String, SerializedValue> values = new HashMap<String, SerializedValue>(map.size());
                for (Object mapEntry : map.entrySet()) {
                    Map.Entry entry = (Map.Entry) mapEntry;
                    if (!(entry.getKey() instanceof String)) {
                        throw new IllegalArgumentException("Maps must contain string typed keys");
                    }
                    values.put((String) entry.getKey(), serialize(entry.getValue(), currentField));
                }
                return new SerializedJafig(values);
            } else if (Primitives.allPrimitiveTypes().contains(valueClass) || value instanceof String) { // it's a primitive, good to go!
                return new SerializedPrimitive(value);
            } else {
                // we have to go deeper! into the class it is
                HashMap<String, SerializedValue> serialized = new HashMap<String, SerializedValue>();
                boolean discarded = valueClass.getAnnotation(Discard.class) != null; // class is discarded
                for (Field field : valueClass.getFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()) || field.getAnnotation(Discard.class) != null || (discarded && field.getAnnotation(Accept.class) == null)) {
                        continue; // discarded field
                    }
                    String name = field.getName();
                    Options options = field.getAnnotation(Options.class);
                    if (options != null) {
                        if (!options.name().equals("")) {
                            name = options.name(); // they specified a name for this field
                        }
                    }
                    try {
                        Object object = field.get(value);
                        SerializedValue returned;
                        try {
                            returned = serialize(object, field);
                        } catch (StackOverflowError error) {
                            throw new Error("Field: " + field.getName() + " Class: " + valueClass.getName());
                        }
                        serialized.put(name, returned);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return new SerializedJafig(serialized);
            }
        }
    }

    public static Object deserialize(SerializedValue value, Class<?> clazz, Field currentField) {
        if (value == null) {
            throw new NullPointerException("Data must not be null");
        } else if (clazz == null) {
            throw new NullPointerException("Object must not be null");
        }
        if (serializers.containsKey(clazz)) {
            return serializers.get(clazz).deserialize(value, currentField);
        }
        if (value instanceof SerializedPrimitive) {
            Object returning = ((SerializedPrimitive) value).getValue();
            if (Primitives.unwrap(currentField.getType()) == float.class && returning instanceof Double) {
                Double d = (Double) returning;
                return d.floatValue();
            }
            return returning;
        } else if (value instanceof SerializedArray) {
            SerializedValue[] values = ((SerializedArray) value).getValues();
            Object[] objects = new Object[values.length];
            for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
                objects[i] = deserialize(values[i], clazz.getComponentType(), currentField);
            }
            return objects;
        } else if (value instanceof SerializedList) {
            if (currentField == null) {
                throw new IllegalArgumentException("Current field must be passed");
            }
            Options options = currentField.getAnnotation(Options.class);
            if (options == null || options.listType() == Options.class) {
                throw new IllegalArgumentException("Lists must be using the Options annotation with the listType set. Invalid: ");
            }
            List<SerializedValue> values = ((SerializedList) value).getValues();
            try {
                List list = (List) clazz.newInstance();
                for (SerializedValue serializedValue : values) {
                    list.add(deserialize(serializedValue, options.listType(), currentField));
                }
                return list;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to create new list!", e);
            }
        } else if (value instanceof SerializedJafig) {
            if (Primitives.allPrimitiveTypes().contains(Primitives.unwrap(clazz))) {
                throw new IllegalArgumentException("Primitive value cannot be object");
            }
            SerializedJafig jafig = (SerializedJafig) value;
            if (Map.class.isAssignableFrom(clazz)) {
                Options options = currentField.getAnnotation(Options.class);
                if (options == null || options.mapType() == Options.class) {
                    throw new IllegalArgumentException("Maps must be using the Options annotation with the mapType set. Invalid: ");
                }
                Map map = new HashMap(jafig.getChildren().size());
                for (Map.Entry<String, SerializedValue> entry : jafig.getChildren().entrySet()) {
                    map.put(entry.getKey(), deserialize(entry.getValue(), options.mapType(), currentField));
                }
                return map;
            }
            Object newObject = null;
            try {
                newObject = clazz.newInstance();
            } catch (InstantiationException e) {
                new InstantiationException("Failed to initialize field for Jafig type " + value.getClass() + "\n" + e.getMessage()).printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            for (Field field : clazz.getFields()) {
                String name = field.getName();
                Options options = field.getAnnotation(Options.class);
                if (options != null) {
                    if (!options.name().equals("")) {
                        name = options.name(); // they specified a name for this field
                    }
                }
                if (jafig.getChildren().containsKey(name)) {
                    SerializedValue serializedValue = jafig.getChildren().get(name);
                    try {
                        field.set(newObject, deserialize(serializedValue, field.getType(), field));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return newObject;
        } else {
            return null;
        }
    }

    public static void addSerializer(JafigSerializer serializer) {
        serializers.put(serializer.getTClass(), serializer);
    }

}
