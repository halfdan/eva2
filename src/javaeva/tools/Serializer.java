package javaeva.tools;
/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 319 $
 *            $Date: 2007-12-05 11:29:32 +0100 (Wed, 05 Dec 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 * This class defines utility routines that use Java serialization.
 **/
public class Serializer {
  /**
  * Serialize the object o (and any Serializable objects it refers to) and
  * store its serialized state in File f.
  **/
  static public void store(Serializable o, File f) throws IOException {
    FileOutputStream file = new FileOutputStream(f);
    ObjectOutputStream out = new ObjectOutputStream(file);
    out.writeObject(o);
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
  * Deserialize the contents of File f and return the resulting object
  **/
  static public Object load(File f) throws IOException, ClassNotFoundException {
    FileInputStream file = new FileInputStream(f);
    ObjectInputStream in = new ObjectInputStream(file);
    Object ret = in.readObject();
    in.close();
    file.close();
    return ret;
  }


  /**
    * Use object serialization to make a "deep clone" of the object o.
    * This method serializes o and all objects it refers to, and then
    * deserializes that graph of objects, which means that everything is
    * copied.  This differs from the clone() method of an object which is
    * usually implemented to produce a "shallow" clone that copies references
    * to other objects, instead of copying all referenced objects.
  **/
  static public Object deepclone(final Serializable o) throws IOException, ClassNotFoundException {
    // Create a connected pair of "piped" streams.
    // We'll write bytes to one, and them from the other one.
    final PipedOutputStream pipeout = new PipedOutputStream();
    PipedInputStream pipein = new PipedInputStream(pipeout);
    // Now define an independent thread to serialize the object and write
    // its bytes to the PipedOutputStream
    Thread writer = new Thread() {
      public void run() {
	ObjectOutputStream out = null;
	try {
	  out = new ObjectOutputStream(pipeout);
	  out.writeObject(o); }
	catch(IOException e) {
	  System.out.println("ERROR in Serialization1"+ e.getMessage());
	}
	finally {
	  try { out.close(); } catch (Exception e) {
	    System.out.println("ERROR in Serialization2"+ e.getMessage());
	  }
	}
      }
    };
    writer.start();
    // Meanwhile, in this thread, read and deserialize from the piped
    // input stream.  The resulting object is a deep clone of the original.
    ObjectInputStream in = new ObjectInputStream(pipein);
    return in.readObject();
  }
  /**
     * This is a simple serializable data structure that we use below for
     * testing the methods above
     **/
  public static class DataStructure implements Serializable {
    String message;
    int[] data;
    DataStructure other;
    public String toString() {
      String s = message;
      for(int i = 0; i < data.length; i++)
	s += " " + data[i];
      if (other != null) s += "\n\t" + other.toString();
	return s;
    }
  }

  /** This class defines a main() method for testing */
  public static class Test {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
      // Create a simple object graph
      DataStructure ds = new DataStructure();
      ds.message = "hello world";
      ds.data = new int[] { 1, 2, 3, 4 };
      ds.other = new DataStructure();
      ds.other.message = "nested structure";
      ds.other.data = new int[] { 9, 8, 7 };
      // Display the original object graph
      System.out.println("Original data structure: " + ds);
      // Output it to a file
      File f = new File("datastructure.ser");
      System.out.println("Storing to a file...");
      Serializer.store(ds, f);
      // Read it back from the file, and display it again
      ds = (DataStructure) Serializer.load(f);
      System.out.println("Read from the file: " + ds);
      // Create a deep clone and display that.  After making the copy
      // modify the original to prove that the clone is "deep".
      DataStructure ds2 = (DataStructure) Serializer.deepclone(ds);
      ds.other.message = null; ds.other.data = null; // Change original
      System.out.println("Deep clone: " + ds2);
    }
  }
  /**
  * Serialize the string s and
  * store its serialized state in File with name Filename.
  **/
  public static void storeString (String Filename,String s) {
    try {
      store(s, new File(Filename));
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
      store(s, ret);
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
	  Object s = null;

	  File f = new File(Filename);
	  if (f.exists()) {    
		  try {
			  s=(Object)load(new File(Filename));
		  } catch (Exception e) {
			  System.err.println("WARNING: loading object File "+Filename+ " not possible, this may happen on source code changes.");
			  System.err.println(e.getMessage());
			  //e.printStackTrace();
			  return null;
		  }
		  return s;
	  } else return null;
  }
}
