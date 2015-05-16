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
import java.util.Map;

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
