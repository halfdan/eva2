package javaeva.tools;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileOutputStream;
import javaeva.tools.JavacWrapper;
/**
 *
 */
public class Source implements Serializable {
  private String m_String = "";
  private String m_Path = "";
  private String m_Filename = "";
  /**
   *
   */
  public Source(String path) {
    System.out.println("Constructor Source "+path);
    m_Path = path;
    m_Filename = System.getProperty("user.dir")+path;
    System.out.println("user.dir " +System.getProperty("user.dir")+ " m_Filename "+m_Filename);
    try {
      BufferedReader reader = new BufferedReader(new FileReader(m_Filename));
      while(true) {
        String s = reader.readLine();
        if (s==null) break;
        //System.out.println("s="+s);
        m_String=m_String+"\n"+s;
      }
      reader.close();
    }  catch (Exception e) {
      e.printStackTrace();
      System.out.println("e="+e+" "+e.getMessage());
    }
  }
  /**
   *
   */
  public String getPath() {
    return m_Path;
  }
  /**
   *
   */
  public String getFilename() {
    return m_Filename;
  }
  /**
   *
   */
  public void setString (String s ) {
    if (s.equals(m_String)==false) {
      m_String = s;
      try {
        File f = new File (m_Filename);
        f.createNewFile();
        PrintWriter writer = new PrintWriter(new FileOutputStream(f));
        writer.println(m_String);
        writer.close();
      } catch (Exception e) {
      System.out.println("set Source="+e+" "+e.getMessage());}

    }
  }
  /**
   *
   */
  public String getString ( ) {
    return m_String;
  }
  /**
   *
   */
  public void append(String s) {
    m_String = m_String + "\n"+s;
  }
  /**
   *
   */
  public void compile() {
    String base = System.getProperty("user.dir");
      System.out.println("user.dir " + base);
    JavacWrapper.compile(base + m_Path);
  }
}