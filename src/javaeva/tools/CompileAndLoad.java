package  javaeva.tools;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 235 $
 *            $Date: 2007-11-08 13:53:51 +0100 (Thu, 08 Nov 2007) $
 *            $Author: mkron $
 *
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import  java.net.URL;
import  java.net.URLClassLoader;
import  java.net.MalformedURLException;
import  java.lang.reflect.Constructor;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class CompileAndLoad {
  /**
   * muss weg . wird ersetzt. hannes
   */
  static public Object getInstance (String path, String fullclassname, Object[] paraforconstructor) {
    System.out.println("CompileAndLoad getInstancee");
    Object Instance = null;
    try {
      String base = System.getProperty("user.dir");
      //      System.out.println("getInstance base = " + base + "path=" + path);
      //      System.out.println("base="+base);
      //      System.out.println("fullclassname = " + fullclassname);
      String classname = EVAHELP.cutClassName(fullclassname);
      //JavaCompiler.compile(base + "\\" + path + "\\" + classname + ".java");
      //JavacWrapper jwc = new JavacWrapper();
      JavacWrapper.compile(base + "\\" + path + "\\" + classname + ".java");
      System.out.println("Error: " + JavacWrapper.m_error);
      URL[] serverURLs = null;
      try {
        //System.out.println("!!!!!!!!!!!getInstance base =" + base + " path =" +path);
        System.gc();
        System.runFinalization();
        String s = "file:"+base+"/"+JavacWrapper.m_userdefclasses+"/";
        serverURLs = new URL[] {
        //          new URL("file:Z:/work/JOptStudent/userdefinedclasses/")
        new URL(s)
      };
    } catch (MalformedURLException e) {
        System.out.println("Invalid URL:" + e.getMessage());
        return  null;
    }
    ClassLoader loader = new URLClassLoader(serverURLs);
    Class[] classes = new Class[paraforconstructor.length];
    for (int i = 0; i < classes.length; i++) {
      if (paraforconstructor[i].getClass() == Double.class)
        classes[i] = double.class;
      else if (paraforconstructor[i].getClass() == Integer.class)
        classes[i] = int.class;
      else if (paraforconstructor[i].getClass() == Boolean.class)
        classes[i] = boolean.class;
      else
        classes[i] = paraforconstructor[i].getClass();
    }
      System.out.println("fullclassname: " + fullclassname);
      Constructor[] constructors = loader.loadClass(fullclassname).getConstructors();
      Constructor constructor = loader.loadClass(fullclassname).getConstructor(classes);
      Instance = constructor.newInstance(paraforconstructor);
    } catch (Exception e) {
      System.out.println("CompileAndLoad.getInstance() : " + e + " " + e.getMessage());
      e.printStackTrace();
    }
    return  Instance;
  }

  static public Object getInstanceFull (String path, String fullclassname, Object[] paraforconstructor) {
//    System.out.println("CompileAndLoad getInstancee");
    Object Instance = null;
    try {
      String base = System.getProperty("user.dir");
      //      System.out.println("getInstance base = " + base + "path=" + path);
      //      System.out.println("base="+base);
      //      System.out.println("fullclassname = " + fullclassname);
      String classname = EVAHELP.cutClassName(fullclassname);
      //JavaCompiler.compile(base + "\\" + path + "\\" + classname + ".java");
      //JavacWrapper jwc = new JavacWrapper();
      JavacWrapper.compile(base + "\\" + path);
  //    System.out.println("Error (CompileAndLoad): " + JavacWrapper.m_error);
      URL[] serverURLs = null;
      try {
        //System.out.println("!!!!!!!!!!!getInstance base =" + base + " path =" +path);
        System.gc();
        System.runFinalization();
        String s = "file:"+base+"/"+JavacWrapper.m_userdefclasses+"/";
    //    System.out.println("LADE: " + s );
        serverURLs = new URL[] {
        //          new URL("file:Z:/work/JOptStudent/userdefinedclasses/")
        new URL(s)
      };
    } catch (MalformedURLException e) {
        System.out.println("Invalid URL:" + e.getMessage());
        return  null;
    }
    URLClassLoader loader = new URLClassLoader(serverURLs);

    Class[] classes = new Class[paraforconstructor.length];
    for (int i = 0; i < classes.length; i++) {
      if (paraforconstructor[i].getClass() == Double.class)
        classes[i] = double.class;
      else if (paraforconstructor[i].getClass() == Integer.class)
        classes[i] = int.class;
      else if (paraforconstructor[i].getClass() == Boolean.class)
        classes[i] = boolean.class;
      else
        classes[i] = paraforconstructor[i].getClass();
    }
      //System.out.println("fullclassname: " + fullclassname);
      Constructor[] constructors = loader.loadClass(fullclassname).getConstructors();
      Constructor constructor = loader.loadClass(fullclassname).getConstructor(classes);
      Instance = constructor.newInstance(paraforconstructor);
      /*URL[] testu = loader.getURLs();
      for (int i = 0; i < testu.length; i++) {
              System.out.println("URL:" + testu[i]);
      }*/

//      System.out.println("OK INSTANCE CREATED");
    } catch (Exception e) {
      System.out.println("CompileAndLoad.getInstance() : " + e + " " + e.getMessage());
      e.printStackTrace();
    }
    return  Instance;
  }

  /**
   *
   */
  static public Class getClass (String path, String fullclassname, Object[] paraforconstructor) {
    System.out.println("CompileAndLoad getClass");
    Class ret = null;
    try {
      String base = System.getProperty("user.dir");
      System.out.println("getInstance base = " + base + "path=" + path);
      String classname = EVAHELP.cutClassName(fullclassname);
      JavacWrapper.compile(base + "\\" + path + "\\" + classname + ".java");
      URL[] serverURLs = null;
      try {
        System.out.println("URLs=" + "file:");                  //+classname+".class");
        System.out.println("!!!!!!!!!!!getInstance base =" + base + "path =" + path);
        serverURLs = new URL[] {
          //          new URL("file:Z:/work/JOptStudent/userdefinedclasses/")
            new URL("file:"+base+"/userdefinedclasses/")
        };
      } catch (MalformedURLException e) {
        System.out.println("Invalid URL:" + e.getMessage());
        return  null;
      }
      ClassLoader loader = new URLClassLoader(serverURLs);
    } catch (Exception e) {
      System.out.println("CompileAndLoad:G   : " + e + " " + e.getMessage());
      e.printStackTrace();
    }
    return  ret;
  }

//  public static void main(String[] args) {
//    Object[] para = new Object[] {};
//    AbstractESProblem first = (AbstractESProblem) CompileAndLoad.getInstanceFull("/usersrc/src1/javaeva/server/problems/bench/UserProblem.java","javaeva.server.problems.bench.UserProblem",para);
//    //first = (AbstractESProblem) CompileAndLoad.getInstanceFull("/usersrc/src2/javaeva/server/problems/bench/UserProblem.java","javaeva.server.problems.bench.UserProblem",para);
//    first = (AbstractESProblem) CompileAndLoad.getInstanceFull("/usersrc/src2/javaeva/server/problems/bench/UserProblem.java","javaeva.server.problems.bench.UserProblem",para);
//
//  }


}



