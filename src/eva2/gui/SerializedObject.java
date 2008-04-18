package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.*;
import java.util.zip.*;
/**
 * This class stores an object serialized in memory. It allows compression,
 * to be used to conserve memory (for example, when storing large strings
 * in memory), or can be used as a mechanism for deep copying objects.
 *
 */
public class SerializedObject implements Serializable {
  /** Stores the serialized object */
  private byte [] m_Serialized;
  /** True if the object has been compressed during storage */
  private boolean m_Compressed;
  /**
   * Serializes the supplied object into a byte array without compression.
   *
   * @param obj the Object to serialize.
   * @exception Exception if the object is not Serializable.
   */
  public SerializedObject(Object obj) throws Exception {
    this(obj, false);
  }
  /**
   * Serializes the supplied object into a byte array.
   *
   * @param obj the Object to serialize.
   * @param compress true if the object should be stored compressed.
   * @exception Exception if the object is not Serializable.
   */
  public SerializedObject(Object obj, boolean compress) throws Exception {
    //System.err.print("."); System.err.flush();
    m_Compressed = compress;
    m_Serialized = toByteArray(obj, m_Compressed);
  }
  /**
   * Serializes the supplied object to a byte array.
   *
   * @param obj the Object to serialize
   * @param compress true if the object should be compressed.
   * @return the byte array containing the serialized object.
   * @exception Exception if the object is not Serializable.
   */
  protected static byte [] toByteArray(Object obj, boolean compress) throws Exception {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    OutputStream os = bo;
    if (compress)
      os = new GZIPOutputStream(os);
    os = new BufferedOutputStream(os);
    ObjectOutputStream oo = new ObjectOutputStream(os);
    oo.writeObject(obj);
    oo.close();
    return bo.toByteArray();
  }
  /**
   * Gets the object stored in this SerializedObject. The object returned
   * will be a deep copy of the original stored object.
   *
   * @return the deserialized Object.
   */
  public Object getObject() {
    try {
      InputStream is = new ByteArrayInputStream(m_Serialized);
      if (m_Compressed) {
        is = new GZIPInputStream(is);
      }
      is = new BufferedInputStream(is);
      ObjectInputStream oi = new ObjectInputStream(is);
      Object result = oi.readObject();
      oi.close();
      return result;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Compares this object with another for equality.
   *
   * @param other the other Object.
   * @return true if the objects are equal.
   */
  public final boolean equals(Object other) {

    // Check class type
    if ((other == null) || !(other.getClass().equals(this.getClass()))) {
      return false;
    }
    // Check serialized length
    byte [] os = ((SerializedObject)other).m_Serialized;
    if (os.length != m_Serialized.length) {
      return false;
    }
    // Check serialized contents
    for (int i = 0; i < m_Serialized.length; i++) {
      if (m_Serialized[i] != os[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a hashcode for this object.
   *
   * @return the hashcode for this object.
   */
  public final int hashCode() {
    return m_Serialized.length;
  }

  /**
   * Returns a text representation of the state of this object.
   *
   * @return a String representing this object.
   */
  public String toString() {

    return (m_Compressed ? "Compressed object: " : "Uncompressed object: ")
      + m_Serialized.length + " bytes";
  }
}
