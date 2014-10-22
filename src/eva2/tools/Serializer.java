package eva2.tools;

import com.google.gson.Gson;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines utility routines that use Java serialization. Any
 * serializable object can be stored to a file, loaded, and cloned (returning a
 * deep copy).
 *
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher, Marcel Kronfeld
 */
public class Serializer {

    /**
     * The logging instance for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Serializer.class.getName());

    /**
     * Private constructor to prevent instantiating module class.
     */
    private Serializer() {
    }

    /**
     * Serialize the object o (and any Serializable objects it refers to) and
     * store its serialized state in File f. If serializeInMem is true, the
     * object is wrapped in a SerializedObject first, which seems to be more
     * efficient than writing a nested object directly to a file.
     *
     * @param o              the object to write
     * @param outStream      The stream to write to
     * @param serializeInMem flag whether to wrap the object in a
     *                       SerializedObject
     * @throws IOException
     */
    private static void store(Serializable o, OutputStream outStream, boolean serializeInMem) throws IOException {
        ObjectOutputStream objectStream = new ObjectOutputStream(outStream);
        try {
            Object objToStore = o;
            if (serializeInMem) {
                objToStore = new SerializedObject(o);
            }
            objectStream.writeObject(objToStore);
            objectStream.flush();
            objectStream.close();
        } catch (java.io.NotSerializableException ex) {
            LOGGER.log(Level.SEVERE, "Object is not serializable!", ex);
        }
    }

    /**
     * Deserialize the contents of File f and return the resulting object. A
     * SerializedObject is unwrapped once.
     *
     * @param inputStream The Input stream to read from
     * @return The deserialized Object from the file
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Object load(final InputStream inputStream) throws IOException, ClassNotFoundException {
        ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
        Object ret = objInputStream.readObject();
        if (ret instanceof SerializedObject) {
            ret = ((SerializedObject) ret).getObject();
        }
        objInputStream.close();
        return ret;
    }

    /**
     * Use object serialization to make a "deep clone" of the object o. This
     * method serializes o and all of its member objects, and then deserializes
     * that graph of objects, which means that everything is copied. This
     * differs from the clone() method of an object which is usually implemented
     * to produce a "shallow" clone that copies references to other objects,
     * instead of copying all referenced objects.
     */
    public static Object deepClone(Object o) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream inputStream = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = inputStream.readObject();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while cloning object.", ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Object class was not found.", ex);
        }
        return obj;
    }

    /**
     * Serialize the string data and write it to the OutputStream.
     *
     * @param outStream The output stream
     * @param data      The string data
     */
    public static void storeString(final OutputStream outStream, final String data) {
        try {
            outStream.write(data.getBytes());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not write string to stream", ex);
        }
    }

    /**
     * Deserialize the contents of the InputStream containing a string and
     * return the resulting string.
     *
     * @param inputStream The input stream to read from
     * @return The deserialized data from the stream
     */
    public static String loadString(final InputStream inputStream) {
        StringBuilder sBuilder = new StringBuilder();
        try {
            int data = inputStream.read();
            while (data != -1) {
                sBuilder.append((char) data);
                data = inputStream.read();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not load string from stream!", ex);
        }
        return sBuilder.toString();
    }

    /**
     * Serialize the string s and store its serialized state in File with name
     * Filename.
     */
    public static void storeObject(OutputStream outStream, Serializable s) {
        try {
            store(s, outStream, true);
        } catch (Exception ex) {
            LOGGER.log(Level.FINER, "Could not write object to stream.", ex);
        }
    }

    /**
     * Deserialize the contents of File with given name containing a string and
     * return the resulting string. If the indicated file doesn't exist or an
     * error occurs, null is returned.
     */
    public static Object loadObject(InputStream inputStream) {
        return loadObject(inputStream, true);
    }

    /**
     * Deserialize the contents of File with given name containing a string and
     * return the resulting string. If the indicated file doesn't exist or an
     * error occurs, null is returned. If casually is false, an error message is
     * printed and an exception is raised if the file was not found or an error
     * occured on loading.
     */
    public static Object loadObject(InputStream inputStream, boolean casually) {
        Object serializedObject = null;

        try {
            serializedObject = load(inputStream);
        } catch (InvalidClassException ex) {
            LOGGER.log(Level.WARNING, "Could not load object file.", ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.WARNING, "Matching object class could not be found.", ex);
        } catch (Exception ex) {
            if (!casually) {
                throw new RuntimeException("WARNING: loading object is not possible! (" + ex.getMessage() + ")");
            } else {
                return null;
            }
        }
        return serializedObject;

    }
}
