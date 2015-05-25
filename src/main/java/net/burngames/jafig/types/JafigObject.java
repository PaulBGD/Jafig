package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.serialize.SerializeUtil;
import net.burngames.jafig.serialize.types.SerializedJafig;
import net.burngames.jafig.serialize.types.SerializedValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author PaulBGD
 */
public class JafigObject extends Jafig {

    private Map<String, Object> data = new HashMap<>();

    public JafigObject(Object[] parameters) {
        super(parameters);
    }

    @Override
    public <T> T load(Class<T> clazz) {
        throw new IllegalArgumentException("Load cannot be called when using JafigObject");
    }

    @Override
    public void save(Object object) {
        SerializedValue value = SerializeUtil.serialize(object);
        if (!(value instanceof SerializedJafig)) {
            throw new IllegalArgumentException("Object must be a SerializedJafig");
        }
        SerializedJafig serialized = (SerializedJafig) value;
        this.data = serialized.toBasic();
    }

    public Map<String, Object> getData() {
        return data;
    }
}
