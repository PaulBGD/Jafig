package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.utils.JSONTidier;
import net.burngames.jafig.utils.JafigSerializer;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Handles the Jafig interface for JSON files.
 *
 * @author PaulBGD
 */
public class JafigJSON extends Jafig {

    static {
        JSONValue.COMPRESSION = JSONStyle.NO_COMPRESS;
    }

    private final File file;
    private JSONObject data = new JSONObject();

    public JafigJSON(Object[] parameters) {
        super(parameters);
        if (parameters.length != 1) {
            throw new InvalidParameterException("File must be provided");
        }
        File file;
        if (parameters[0] instanceof String) {
            file = new File((String) parameters[0]);
        } else if (parameters[0] instanceof File) {
            file = (File) parameters[0];
        } else {
            throw new InvalidParameterException("File must be provided as a String, or File");
        }
        this.file = file;
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                this.data = (JSONObject) JSONValue.parse(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> T load(Class<T> clazz) {
        return (T) JafigSerializer.deserialize(JafigSerializer.serialize(this.data), clazz, null);
    }

    @Override
    public void save(Object object) {
        JafigSerializer.SerializedValue value = JafigSerializer.serialize(object);
        if (!(value instanceof JafigSerializer.SerializedJafig)) {
            throw new IllegalArgumentException("Object must be an object");
        }
        JafigSerializer.SerializedJafig serialized = (JafigSerializer.SerializedJafig) value;
        this.data.putAll(serialized.toBasic());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.file);
            fileOutputStream.write(JSONTidier.tidyJSON(this.data.toJSONString()).getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
