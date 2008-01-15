package javaeva.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

//import wsi.ra.tool.BasicResourceLoader;

/**
 * Allow for java to list Classes that exist in one package and can be instantiated from
 * the classpath, either directly or through a jar on the classpath.
 * So far, jars which are located <i>within</i> another jar will not be searched.
 * 
 * @author mkron
 *
 */
public class ReflectPackage {
	
	final static boolean TRACE = false;
	
	static class ClassComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			return (o1.toString().compareTo(o2.toString()));
		}
		
	}
	
	/**
 	 * Collect classes of a given package from the file system.
	 * 
	 * @param pckgname
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static HashSet<Class> getClassesFromFilesFltr(HashSet<Class> set, String path, String pckgname, boolean includeSubs, Class reqSuperCls) {
		try {
			// Get a File object for the package
			File directory = null;
			String dir = null;
			try {
				ClassLoader cld = ClassLoader.getSystemClassLoader();
				if (cld == null) {
					throw new ClassNotFoundException("Can't get class loader.");
				}
				dir = path + "/" + pckgname.replace(".","/");

				if (TRACE) System.out.println(".. opening " + path);

				directory = new File(dir);

			} catch (NullPointerException x) {
				if (TRACE) {
					System.err.println(directory.getPath()+ " not found in " + path);
					System.err.println("directory " + (directory.exists() ? "exists" : "doesnt exist"));
				}
				return set;
			}
			if (directory.exists()) {
				// Get the list of the files contained in the package
				getClassesFromDirFltr(set, directory, pckgname, includeSubs, reqSuperCls);
			} else {
				if (TRACE) System.err.println(directory.getPath() + " doesnt exist in " + path + ", dir was " + dir);
			}
		} catch(ClassNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return set;
		}
		return set;
	}
	
//	public static ArrayList<Class> getClassesFromDir(File directory, String pckgname, boolean includeSubs) {
//		return getClassesFromDirFltr(directory, pckgname, includeSubs, null);
//	}
	
	public static HashSet<Class> getClassesFromDirFltr(HashSet<Class> set, File directory, String pckgname, boolean includeSubs, Class<?> reqSuperCls) {
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					try {
						Class<?> cls = Class.forName(pckgname + '.' + files[i].substring(0, files[i].length() - 6));
						if (reqSuperCls != null) {
							if (reqSuperCls.isAssignableFrom(cls)) {
								addClass(set, cls);
							}
						} else {
							addClass(set, cls);
						}
					} catch (Exception e) {
						System.err.println("ReflectPackage: Couldnt get Class from jar for "+pckgname+files[i]+": "+e.getMessage());
					} catch (Error e) {
						System.err.println("ReflectPackage: Couldnt get Class from jar for "+pckgname+files[i]+": "+e.getMessage());
					}
				} else if (includeSubs) {
					// do a recursive search over subdirs
					File subDir = new File(directory.getAbsolutePath()+File.separatorChar+files[i]);
					if (subDir.exists() && subDir.isDirectory()) {
						getClassesFromDirFltr(set, subDir, pckgname+"."+files[i], includeSubs, reqSuperCls);
					}
				}
			}
		}
		return set;
	}

	private static void addClass(HashSet<Class> set, Class cls) {
		if (TRACE) System.out.println("adding class " + cls.getName());
		if (set.contains(cls)) {
			System.err.println("warning, Class " + cls.getName() + " not added twice!");
		} else set.add(cls);
	}
	
	public static ArrayList<Class> filterAssignableClasses(ArrayList<Class> classes, Class<?> reqSuperCls) {
		ArrayList<Class> assClasses = new ArrayList<Class>();
		for (int i=0; i<classes.size(); i++) {
			if (reqSuperCls.isAssignableFrom(classes.get(i))) {
				if (TRACE) System.out.println(" taking over "+classes.get(i));
				assClasses.add(classes.get(i));
			}
		}
		return assClasses;
	}

	/**
	 * Collect classes of a given package from a jar file.
	 * 
	 * @param jarName
	 * @param packageName
	 * @return
	 */
	public static HashSet<Class> getClassesFromJarFltr(HashSet<Class> set, String jarName, String packageName, boolean includeSubs, Class<?> reqSuperCls){
		boolean isInSubPackage = true;

		packageName = packageName.replaceAll("\\." , "/");
		if (TRACE) System.out.println("Jar " + jarName + " looking for " + packageName);
		try{
			JarInputStream jarFile = new JarInputStream
			(new FileInputStream (jarName));
			JarEntry jarEntry;

			while((jarEntry = jarFile.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();
//				if (TRACE) System.out.println("- " + jarEntry.getName());
				if((jarEntryName.startsWith(packageName)) &&
						(jarEntryName.endsWith (".class")) ) {
//					subpackages are hit here as well!
					if (!includeSubs) { // check if the class belongs to a subpackage
						int lastDash = jarEntryName.lastIndexOf('/');
						if (lastDash > packageName.length()+1) isInSubPackage = true;
						else isInSubPackage = false;
					}
					if (includeSubs || !isInSubPackage) { // take the right ones
						String clsName = jarEntryName.replace("/", ".");
						try {
							// removes the .class extension							
							Class cls = Class.forName(clsName.substring(0, jarEntryName.length() - 6));
							if (reqSuperCls != null) {
								if (reqSuperCls.isAssignableFrom(cls)) {
									addClass(set, cls);
								}
							} else addClass(set, cls);
						} catch(Exception e) {
							System.err.println("ReflectPackage: Couldnt get Class from jar for "+clsName+": "+e.getMessage());
						} catch(Error e) {
							System.err.println("ReflectPackage: Couldnt get Class from jar for "+clsName+": "+e.getMessage());
						}
					}
					
//					classes.add (jarEntry.getName().replaceAll("/", "\\."));
				}
			}
		} catch(IOException e) {
			System.err.println("coulnt read jar: " + e.getMessage());
			e.printStackTrace();
		}
		return set;
	}
	
	/**
	 * Collect all classes from a given package on the classpath. If includeSubs is true,
	 * the sub-packages are listed as well.
	 * 
	 * @param pckg
	 * @param includeSubs
	 * @param bSort	sort alphanumerically by class name 
	 * @return An ArrayList of Class objects contained in the package which may be empty if an error occurs.
	 */
	public static Class[] getAllClassesInPackage(String pckg, boolean includeSubs, boolean bSort) {
		return getClassesInPackageFltr(new HashSet<Class>(), pckg, includeSubs, bSort, null);
	}
	
	/**
	 * Collect classes from a given package on the classpath which have the given Class
	 * as superclass or superinterface. If includeSubs is true,
	 * the sub-packages are listed as well.
	 * 
	 * @see Class.assignableFromClass(Class cls)
	 * @param pckg
	 * @return
	 */
	public static Class[] getClassesInPackageFltr(HashSet<Class> set, String pckg, boolean includeSubs, boolean bSort, Class reqSuperCls) {
		String classPath = System.getProperty("java.class.path",".");
		if (TRACE) System.out.println("classpath is " + classPath);
		String[] pathElements = classPath.split(File.pathSeparator);

		for (int i=0; i<pathElements.length; i++) {
			if (TRACE) System.out.println("reading element "+pathElements[i]);
			if (pathElements[i].endsWith(".jar")) {
				getClassesFromJarFltr(set, pathElements[i], pckg, includeSubs, reqSuperCls);
			} else {
				getClassesFromFilesFltr(set, pathElements[i], pckg, includeSubs, reqSuperCls);
			}
		}
		Object[] clsArr = set.toArray();
		if (bSort) {
			Arrays.sort(clsArr, new ClassComparator());
		}
		
		List list;
		 
//		#1
		list = Arrays.asList(clsArr);
		return (Class[])list.toArray(new Class[list.size()]);
	}
	
	/**
	 * Retrieve assignable classes of the given package from classpath.
	 * 
	 * @param pckg	String denoting the package
	 * @param reqSuperCls
	 * @return
	 */
	public static Class[] getAssignableClassesInPackage(String pckg, Class reqSuperCls, boolean includeSubs, boolean bSort) {
		if (TRACE) System.out.println("requesting classes assignable from " + reqSuperCls.getName());
		return getClassesInPackageFltr(new HashSet<Class>(), pckg, includeSubs, bSort, reqSuperCls);
	}
	
	public static void main(String[] args) {
		ClassLoader cld =  Thread.currentThread().getContextClassLoader();
		System.out.println("1: " + cld.getResource("/javaeva/server"));
		System.out.println("2: " + cld.getResource("javaeva/server"));
//		BasicResourceLoader rld = BasicResourceLoader.instance();
//		byte[] b = rld.getBytesFromResourceLocation("resources/images/Sub24.gif");
//		System.out.println((b == null) ? "null" : b.toString());
//		b = rld.getBytesFromResourceLocation("src/javaeva/client/EvAClient.java");
//		System.out.println((b == null) ? "null" : b.toString());

		HashSet<String> h = new HashSet<String> (20);
	
		for (int i=0; i<20; i++) {
			h.add("String "+ (i%10));
		}
		for (String string : h) {
			System.out.println("+ "+string);
		}
		
//		String[] pathElements = getClassPathElements();
//		for (int i=0; i<pathElements.length; i++) {
//			System.out.print(i+" " + pathElements[i]);
//			System.out.println(pathElements[i].endsWith(".jar") ? " is a jar" : " is a path");
//			if (pathElements[i].endsWith(".jar")) {
//				ArrayList al = getClassesFromJarFltr(pathElements[i], "javaeva.server.model", false, null);
//				for (int k=0; k<al.size(); k++) {
//					//if (Class.forName("javaeva.server.modules.ModuleAdapter").isAssignableFrom((Class)al.get(i))) 
//						System.out.println((Class)al.get(k));
//				}
//			} else {
//				
//			}
//		}

	}
	
	public static String[] getClassPathElements() {
		String classPath = System.getProperty("java.class.path",".");
//		System.out.println("classpath: " + classPath);
		return classPath.split(File.pathSeparator);
	}
}