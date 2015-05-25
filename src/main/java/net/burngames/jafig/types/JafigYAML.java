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

package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.serialize.SerializeUtil;
import net.burngames.jafig.serialize.types.SerializedJafig;
import net.burngames.jafig.serialize.types.SerializedValue;
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
        return (T) SerializeUtil.deserialize(SerializeUtil.serialize(this.data), clazz, null);
    }

    @Override
    public void save(Object object) {
        SerializedValue value = SerializeUtil.serialize(object);
        if (!(value instanceof SerializedJafig)) {
            throw new IllegalArgumentException("Object must be a SerializedConfig");
        }
        SerializedJafig serialized = (SerializedJafig) value;
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
