package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.utils.JafigSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Jafig interface for YAML files.
 *
 * @author PaulBGD
 */
public class JafigYAML extends Jafig {

    private static final Yaml yaml;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    private final File file;
    private Map data = new HashMap();

    public JafigYAML(Object[] parameters) {
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
                this.data = (HashMap) yaml.load(inputStream);
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
        String data = yaml.dump(this.data);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(this.file);
            fileOutputStream.write(data.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
