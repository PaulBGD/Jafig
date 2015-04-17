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

import net.burngames.jafig.annotations.Accept;
import net.burngames.jafig.annotations.Discard;
import net.burngames.jafig.annotations.Options;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal functions for representing data
 *
 * @author PaulBGD
 */
public class JafigSerializer {

    public static SerializedValue serialize(Object value) {
        if (value == null) {
            // nothing to do here
            return new SerializedPrimitive(null);
        } else if (value instanceof Class) {
            throw new IllegalArgumentException("Classes cannot be serialized");
        }
        Class<?> valueClass = value.getClass();
        if (Primitives.isWrapperType(valueClass)) {
            valueClass = Primitives.unwrap(valueClass); // unwrap
        }
        if (valueClass.isArray()) {
            Object[] array = (Object[]) value;
            SerializedValue[] values = new SerializedValue[array.length];
            for (int i = 0, arrayLength = array.length; i < arrayLength; i++) {
                values[i] = serialize(array[i]);
            }
            return new SerializedArray(values);
        } else if (value instanceof List) {
            List list = (List) value;
            List<SerializedValue> values = new ArrayList<SerializedValue>(list.size());
            for (Object aList : list) {
                values.add(serialize(aList));
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
                values.put((String) entry.getKey(), serialize(entry.getValue()));
            }
            return new SerializedJafig(values);
        } else if (Primitives.allPrimitiveTypes().contains(valueClass) || value instanceof String) { // it's a primitive, good to go!
            return new SerializedPrimitive(value);
        } else {
            // we have to go deeper! into the class it is
            HashMap<String, SerializedValue> serialized = new HashMap<String, SerializedValue>();
            boolean discarded = valueClass.getAnnotation(Discard.class) != null; // class is discarded
            for (Field field : valueClass.getFields()) {
                if (field.getAnnotation(Discard.class) != null || (discarded && field.getAnnotation(Accept.class) == null)) {
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
                    SerializedValue returned = serialize(object);
                    serialized.put(name, returned);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return new SerializedJafig(serialized);
        }
    }

    public static Object deserialize(SerializedValue value, Class<?> clazz, Field currentField) {
        if (value == null) {
            throw new NullPointerException("Data must not be null");
        } else if (clazz == null) {
            throw new NullPointerException("Object must not be null");
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
            SerializedJafig jafig = (SerializedJafig) value;
            Object newObject = null;
            try {
                newObject = clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
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

    public static abstract class SerializedValue {
        public abstract Object toBasic();
    }

    public static class SerializedPrimitive extends SerializedValue {
        private final Object value;

        public SerializedPrimitive(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public Object toBasic() {
            return getValue();
        }
    }

    public static class SerializedArray extends SerializedValue {
        private final SerializedValue[] values;

        public SerializedArray(SerializedValue[] values) {
            this.values = values;
        }

        public SerializedValue[] getValues() {
            return values;
        }

        @Override
        public Object toBasic() {
            Object[] objects = new Object[this.values.length];
            for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
                objects[i] = this.values[i].toBasic();
            }
            return objects;
        }
    }

    public static class SerializedList extends SerializedValue {
        private final List<SerializedValue> values;

        public SerializedList(List<SerializedValue> values) {
            this.values = values;
        }

        public List<SerializedValue> getValues() {
            return values;
        }

        @Override
        public Object toBasic() {
            List<Object> objects = new ArrayList<Object>(this.values.size());
            for (SerializedValue value : values) {
                objects.add(value.toBasic());
            }
            return objects;
        }
    }

    public static class SerializedJafig extends SerializedValue {
        private final HashMap<String, SerializedValue> child;

        public SerializedJafig(HashMap<String, SerializedValue> child) {
            this.child = child;
        }

        public HashMap<String, SerializedValue> getChildren() {
            return child;
        }

        @Override
        public Map<String, Object> toBasic() {
            HashMap<String, Object> objects = new HashMap<String, Object>(this.child.size());
            for (Map.Entry<String, SerializedValue> entry : child.entrySet()) {
                objects.put(entry.getKey(), entry.getValue().toBasic());
            }
            return objects;
        }
    }

}
