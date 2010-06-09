package eva2.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class defines utility routines that use Java serialization. Any
 * serializable object can be stored to a file, loaded, and cloned (returning
 * a deep copy).
 * 
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher, Marcel Kronfeld
 **/
public class Serializer {
	/**
	 * Serialize the object o (and any Serializable objects it refers to) and
	 * store its serialized state in File f. If serializeInMem is true, the object
	 * is wrapped in a SerializedObject first, which seems to be more efficient than
	 * writing a nested object directly to a file.
	 * 
	 * @param o the object to write
	 * @param f the file to write to
	 * @param serializeInMem flag whether to wrap the object in a SerializedObject
	 * @throws IOException
	 **/
	static public void store(Serializable o, File f, boolean serializeInMem) throws IOException {
		FileOutputStream file = new FileOutputStream(f);
		ObjectOutputStream out = new ObjectOutputStream(file);
		try {
			Object objToStore = o;
			if (serializeInMem) objToStore = new SerializedObject((Object)o);
			//    	System.out.println("Writing " + o.getClass());
			out.writeObject(objToStore);
		} catch (java.io.NotSerializableException e) {
			System.err.println("Error: Object " + o.getClass() + " is not serializable - run settings cannot be stored.");
			e.printStackTrace();
		}
		out.flush();
		out.close();
		file.close();
	}
	//   try {
	//      FileOutputStream OutStream = new FileOutputStream("ESPara.ser");
	//      ObjectOutputStream OutObjectStream = new ObjectOutputStream (OutStream);
	//      OutObjectStream.writeObject(this);
	//      OutObjectStream.flush();
	//      OutStream.close();
	//    } catch (Exception e) {
	//       System.out.println ("ERROR ESPara.ser"+e.getMessage());
	//    }

	/**
	 * Deserialize the contents of File f and return the resulting object.
	 * A SerializedObject is unwrapped once.
	 **/
	static public Object load(File f) throws IOException, ClassNotFoundException {
		FileInputStream file = new FileInputStream(f);
		ObjectInputStream in = new ObjectInputStream(file);
		Object ret = in.readObject();
		if (ret instanceof SerializedObject) {
			ret = ((SerializedObject)ret).getObject();
		}
		in.close();
		file.close();
		return ret;
	}

	/**
	 * Use object serialization to make a "deep clone" of the object o.
	 * This method serializes o and all of its member objects, and then
	 * deserializes that graph of objects, which means that everything is
	 * copied.  This differs from the clone() method of an object which is
	 * usually implemented to produce a "shallow" clone that copies references
	 * to other objects, instead of copying all referenced objects.
	 **/
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
			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bos.toByteArray()));
			obj = in.readObject();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		return obj;
	}

	/**
	 * This is a simple serializable data structure that we use below for
	 * testing the methods above
	 **/
	static class ExampleDataStruct implements Serializable {
		String message;
		int[] data;
		ExampleDataStruct other;
		public String toString() {
			String s = message;
			for(int i = 0; i < data.length; i++)
				s += " " + data[i];
			if (other != null) s += "\n\t" + other.toString();
			return s;
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// Create a simple object graph
		ExampleDataStruct ds = new ExampleDataStruct();
		ds.message = "hello world";
		ds.data = new int[] { 1, 2, 3, 4 };
		ds.other = new ExampleDataStruct();
		ds.other.message = "nested structure";
		ds.other.data = new int[] { 9, 8, 7 };
		// Display the original object graph
		System.out.println("Original data structure: " + ds);
		// Output it to a file
		File f = new File("datastructure.ser");
		System.out.println("Storing to a file...");
		Serializer.store(ds, f, true);
		// Read it back from the file, and display it again
		ds = (ExampleDataStruct) Serializer.load(f);
		System.out.println("Read from the file: " + ds);
		// Create a deep clone and display that.  After making the copy
		// modify the original to prove that the clone is "deep".
		ExampleDataStruct ds2 = (ExampleDataStruct) Serializer.deepClone(ds);
		ds.other.message = null; ds.other.data = null; // Change original
		System.out.println("Deep clone: " + ds2);
	}

	/**
	 * Serialize the string s and
	 * store its serialized state in File with name Filename.
	 **/
	public static void storeString (String Filename,String s) {
		try {
			store(s, new File(Filename), false);
		} catch (Exception e) {
			System.out.println("ERROR writing string File "+Filename+ " String "+s);
		}
	}
	/**
	 * Deserialize the contents of File f containing
	 * a string and return the resulting string.
	 **/
	public static String loadString (String Filename) {
		String s = null;
		try {
			s=(String)load(new File(Filename));
		} catch (Exception e) {
			// System.out.println("WARNING: Loading string File "+Filename+ " not possible !!");
		}
		return s;
	}
	/**
	 * Serialize the string s and
	 * store its serialized state in File with name Filename.
	 **/
	public static File storeObject (String Filename,Serializable s) {
		File ret = new File(Filename);
		try {
			store(s, ret, true);
		} catch (Exception e) {
			System.err.println("ERROR writing Object File "+Filename+ " String "+s);
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Deserialize the contents of File with given name containing
	 * a string and return the resulting string. If the indicated file
	 * doesnt exist or an error occurs, null is returned.
	 **/
	public static Object loadObject (String Filename) {
		return loadObject(Filename, true);
	}

	/**
	 * Deserialize the contents of File with given name containing
	 * a string and return the resulting string. If the indicated file
	 * doesnt exist or an error occurs, null is returned.
	 * If casually is false, an error message is printed and an exception
	 * is raised if the file was not found or an error occured on loading.
	 **/
	public static Object loadObject (String Filename, boolean casually) {
		Object s = null;

		File f = new File(Filename);
		if (f.exists()) {    
			try {
				s=(Object)load(f);
			} catch (InvalidClassException e) {
				System.err.println("WARNING: loading object File "+Filename+ " not possible, this may happen on source code changes.");
				System.err.println(e.getMessage());
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException on loading object File " + Filename + ". This may happen on refactorings.");
				System.err.println(e.getMessage());
			} catch (Exception e) {
				if (!casually) throw new RuntimeException("WARNING: loading object File "+Filename+ " not possible! ("+e.getMessage()+")");
				else return null;
			}
			return s;
		} else {
			if (!casually) System.err.println("Error in Serializer: file " + Filename + " not found!");
			return null;
		}
	}
}
