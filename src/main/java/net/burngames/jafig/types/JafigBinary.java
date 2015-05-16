package net.burngames.jafig.types;

import net.burngames.jafig.Jafig;
import net.burngames.jafig.utils.JafigSerializer;
import net.burngames.jafig.utils.OutputInputStream;
import net.burngames.jafig.utils.Primitives;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author PaulBGD
 */
public class JafigBinary extends Jafig {

    private final OutputInputStream stream;

    public JafigBinary(Object[] parameters) {
        super(parameters);

        if (parameters.length == 0) {
            throw new InvalidParameterException("File must be provided");
        }
        OutputInputStream stream = null;
        try {
            if (parameters[0] instanceof String) {
                File file = new File((String) parameters[0]);
                stream = new OutputInputStream(new FileOutputStream(file), new FileInputStream(file));
            } else if (parameters[0] instanceof File) {
                File file = (File) parameters[0];
                stream = new OutputInputStream(new FileOutputStream(file), new FileInputStream(file));
            } else if (parameters.length == 2) {
                if (parameters[0] instanceof InputStream && parameters[1] instanceof OutputStream) {
                    stream = new OutputInputStream((OutputStream) parameters[1], (InputStream) parameters[0]);
                } else if (parameters[0] instanceof OutputStream && parameters[1] instanceof InputStream) {
                    stream = new OutputInputStream((OutputStream) parameters[0], (InputStream) parameters[1]);
                } else {
                    throw new InvalidParameterException("File must be provided as a String, File, Input Stream, or Output Stream");
                }
            } else if (parameters[0] instanceof InputStream) {
                stream = new OutputInputStream(null, (InputStream) parameters[0]);
            } else if (parameters[0] instanceof OutputStream) {
                stream = new OutputInputStream((OutputStream) parameters[0], null);
            } else {
                throw new InvalidParameterException("File must be provided as a String, File, Input Stream, or Output Stream");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.stream = stream;
    }

    @Override
    public <T> T load(Class<T> clazz) {
        JafigSerializer.SerializedJafig data;
        try {
            data = (JafigSerializer.SerializedJafig) readValue(this.stream.read(), new AtomicInteger());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to read binary file", e);
            data = new JafigSerializer.SerializedJafig(new HashMap<String, JafigSerializer.SerializedValue>());
        }
        return (T) JafigSerializer.deserialize(data, clazz, null);
    }

    @Override
    public void save(Object object) {
        JafigSerializer.SerializedValue value = JafigSerializer.serialize(object);
        if (!(value instanceof JafigSerializer.SerializedJafig)) {
            throw new IllegalArgumentException("Object must be an object");
        }
        JafigSerializer.SerializedJafig serialized = (JafigSerializer.SerializedJafig) value;
        try {
            writeValue(serialized);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read binary file", e);
        }
    }

    private JafigSerializer.SerializedValue readValue(byte[] bytes, AtomicInteger offset) throws Exception {
        JafigSerializer.SerializedValue value;
        byte b = bytes[offset.getAndAdd(1)];
        Class<?> type = byteToClass(b);
        if (type == String.class) {
            value = new JafigSerializer.SerializedPrimitive(readString(bytes, offset));
        } else if (type == int.class) {
            value = new JafigSerializer.SerializedPrimitive(readInt(bytes, offset));
        } else if (type == boolean.class) {
            value = new JafigSerializer.SerializedPrimitive(readBoolean(bytes, offset));
        } else if (type == char.class) {
            value = new JafigSerializer.SerializedPrimitive(readChar(bytes, offset));
        } else if (type == long.class) {
            value = new JafigSerializer.SerializedPrimitive(readLong(bytes, offset));
        } else if (type == double.class) {
            value = new JafigSerializer.SerializedPrimitive(readDouble(bytes, offset));
        } else if (type == float.class) {
            value = new JafigSerializer.SerializedPrimitive(Float.intBitsToFloat(readInt(bytes, offset)));
        } else if (type == short.class) {
            value = new JafigSerializer.SerializedPrimitive(readShort(bytes, offset));
        } else if (type == Array.class) {
            int length = readInt(bytes, offset);
            JafigSerializer.SerializedValue[] array = new JafigSerializer.SerializedValue[length];
            while (length-- != 0) {
                array[length] = readValue(bytes, offset);
            }
            value = new JafigSerializer.SerializedArray(array);
        } else if (type == List.class) {
            int length = readInt(bytes, offset);
            List<JafigSerializer.SerializedValue> list = new ArrayList<>(length);
            while (length-- != 0) {
                list.add(readValue(bytes, offset));
            }
            value = new JafigSerializer.SerializedList(list);
        } else if (type == Object.class) {
            HashMap<String, JafigSerializer.SerializedValue> map = new HashMap<>();
            int length = readInt(bytes, offset);
            while (length-- != 0) {
                String header = readString(bytes, offset);
                JafigSerializer.SerializedValue objectValue = readValue(bytes, offset);
                map.put(header, objectValue);
            }
            value = new JafigSerializer.SerializedJafig(map);
        } else {
            throw new IllegalArgumentException("Invalid type found: " + (int) b + " (" + type + ") o: " + offset.get());
        }
        return value;
    }

    private void writeValue(JafigSerializer.SerializedValue object) throws IOException {
        if (object instanceof JafigSerializer.SerializedPrimitive) {
            JafigSerializer.SerializedPrimitive primitive = (JafigSerializer.SerializedPrimitive) object;
            Object value = primitive.getValue();
            if (value instanceof String) {
                this.stream.write(new byte[] {classToByte(value.getClass())});
                writeString((String) value);
            } else if (value instanceof Integer) {
                this.stream.write(new byte[] {classToByte(value.getClass())});
                writeInt((Integer) value);
            } else if (value instanceof Boolean) {
                this.stream.write(new byte[] {classToByte(value.getClass())});
                writeBoolean((Boolean) value);
            } else if (value instanceof Character) {
                this.stream.write(new byte[] {classToByte(value.getClass())});
                writeChar((Character) value);
            } else if (value instanceof Long) {
                this.stream.write(new byte[] {classToByte(value.getClass())});
                writeLong((Long) value);
            } else if (value instanceof Float) {
                this.stream.write(new byte[]{classToByte(value.getClass())});
                writeInt(Float.floatToRawIntBits((Float) value));
            } else if (value instanceof Double) {
                this.stream.write(new byte[]{classToByte(value.getClass())});
                writeDouble((Double) value);
            } else if (value instanceof Short) {
                this.stream.write(new byte[]{classToByte(value.getClass())});
                writeShort((Short) value);
            } else {
                throw new IllegalArgumentException("Invalid object '" + value + "' " + (value != null ? value.getClass() : ""));
            }
        } else if (object instanceof JafigSerializer.SerializedArray) {
            JafigSerializer.SerializedArray array = (JafigSerializer.SerializedArray) object;
            JafigSerializer.SerializedValue[] values = array.getValues();
            this.stream.write(new byte[] {classToByte(array.getClass())});
            writeInt(values.length);
            for (JafigSerializer.SerializedValue value : values) {
                writeValue(value);
            }
        } else if (object instanceof JafigSerializer.SerializedList) {
            JafigSerializer.SerializedList list = (JafigSerializer.SerializedList) object;
            List<JafigSerializer.SerializedValue> values = list.getValues();
            this.stream.write(new byte[] {classToByte(values.getClass())});
            writeInt(values.size());
            for (JafigSerializer.SerializedValue value : values) {
                writeValue(value);
            }
        } else if (object instanceof JafigSerializer.SerializedJafig) {
            JafigSerializer.SerializedJafig jafig = (JafigSerializer.SerializedJafig) object;
            HashMap<String, JafigSerializer.SerializedValue> children = jafig.getChildren();
            this.stream.write(new byte[] {classToByte(children.getClass())});
            writeInt(children.size());
            for (Map.Entry<String, JafigSerializer.SerializedValue> entry : children.entrySet()) {
                writeString(entry.getKey());
                writeValue(entry.getValue());
            }
        } else {
            throw new IllegalArgumentException("Invalid object '" + object + "' " + (object != null ? object.getClass() : ""));
        }
    }

    private String readString(byte[] bytes, AtomicInteger offset) throws UnsupportedEncodingException {
        int length = readInt(bytes, offset);
        byte[] stringBytes = Arrays.copyOfRange(bytes, offset.get(), offset.addAndGet(length));
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    private void writeString(String string) throws IOException {
        writeInt(string.length());
        this.stream.write(string.getBytes(StandardCharsets.UTF_8));
    }

    private int readInt(byte[] bytes, AtomicInteger offset) {
        return bytes[offset.getAndAdd(1)] << 24
                | (bytes[offset.getAndAdd(1)] & 0xFF) << 16
                | (bytes[offset.getAndAdd(1)] & 0xFF) << 8
                | (bytes[offset.getAndAdd(1)] & 0xFF);
    }

    private void writeInt(int integer) throws IOException {
        this.stream.write(new byte[]{
                (byte) (integer >>> 24),
                (byte) (integer >>> 16),
                (byte) (integer >>> 8),
                (byte) integer
        });
    }

    private boolean readBoolean(byte[] bytes, AtomicInteger offset) {
        return bytes[offset.getAndAdd(1)] == 0x1;
    }

    private void writeBoolean(boolean b) throws IOException {
        this.stream.write(new byte[] {(byte) (b ? 0x1 : 0x0)});
    }

    private char readChar(byte[] bytes, AtomicInteger offset) {
        return Character.forDigit(readInt(bytes, offset), 10);
    }

    private void writeChar(char c) throws IOException {
        writeInt(Character.digit(c, 10));
    }

    private long readLong(byte[] bytes, AtomicInteger offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (bytes[i + offset.getAndAdd(1)] & 0xFF);
        }
        return result;
    }

    private void writeLong(long l) throws IOException {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        this.stream.write(result);
    }

    private double readDouble(byte[] bytes, AtomicInteger offset) {
        return ByteBuffer.wrap(bytes).getDouble(offset.getAndAdd(8));
    }

    private void writeDouble(double d) throws IOException {
        this.stream.write(ByteBuffer.allocate(8).putDouble(d).array());
    }

    private short readShort(byte[] bytes, AtomicInteger offset) {
        return (short) ((bytes[offset.getAndAdd(1)]) << 8 | (bytes[offset.getAndAdd(1)] & 0xFF));
    }

    private void writeShort(short s) throws IOException {
        this.stream.write(new byte[]{(byte) (s & 0xff), (byte) ((s >> 8) & 0xff)});
    }

    private Class<?> byteToClass(byte b) {
        if (b == 0x0) {
            return String.class;
        } else if (b == 0x1) {
            return int.class;
        } else if (b == 0x2) {
            return boolean.class;
        } else if (b == 0x3) {
            return char.class;
        } else if (b == 0x4) {
            return long.class;
        } else if (b == 0x5) {
            return double.class;
        } else if (b == 0x6) {
            return float.class;
        } else if (b == 0x7) {
            return short.class;
        } else if (b == 0x8) {
            return Array.class;
        } else if (b == 0x9) {
            return List.class;
        } else if (b == 0x10) {
            return Object.class;
        }
        return null;
    }

    private byte classToByte(Class<?> clazz) {
        clazz = Primitives.unwrap(clazz);
        if (clazz == String.class) {
            return 0x0;
        } else if (clazz == int.class) {
            return 0x1;
        } else if (clazz == boolean.class) {
            return 0x2;
        } else if (clazz == char.class) {
            return 0x3;
        } else if (clazz == long.class) {
            return 0x4;
        } else if (clazz == double.class) {
            return 0x5;
        } else if (clazz == float.class) {
            return 0x6;
        } else if (clazz == short.class) {
            return 0x7;
        } else if (clazz == Array.class) {
            return 0x8;
        } else if (clazz == List.class) {
            return 0x9;
        } else if (clazz != null && Map.class.isAssignableFrom(clazz)) {
            return 0x10;
        }
        return 0xffffffff;
    }


}
