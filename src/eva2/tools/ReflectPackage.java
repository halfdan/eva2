package eva2.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import eva2.tools.tool.BasicResourceLoader;

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
	static int missedJarsOnClassPath = 0;
	static boolean useFilteredClassPath = true;
//	static boolean usePathMap = true;
	static String[] dynCP = null;
//	static HashMap<String, ArrayList<String>> pathMap = new HashMap<String, ArrayList<String>>();
	
	static class ClassComparator<T> implements Comparator<T> {
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
	public static int getClassesFromFilesFltr(HashSet<Class> set, String path, String pckgname, boolean includeSubs, Class reqSuperCls) {
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
				return 0;
			}
			if (directory.exists()) {
				// Get the list of the files contained in the package
				return getClassesFromDirFltr(set, directory, pckgname, includeSubs, reqSuperCls);
			} else {
				if (TRACE) System.err.println(directory.getPath() + " doesnt exist in " + path + ", dir was " + dir);
				return 0;
			}
		} catch(ClassNotFoundException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}
	
//	public static ArrayList<Class> getClassesFromDir(File directory, String pckgname, boolean includeSubs) {
//		return getClassesFromDirFltr(directory, pckgname, includeSubs, null);
//	}
	
	public static int getClassesFromDirFltr(HashSet<Class> set, File directory, String pckgname, boolean includeSubs, Class<?> reqSuperCls) {
		int cntAdded = 0;
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
								cntAdded += addClass(set, cls);
							}
						} else {
							cntAdded += addClass(set, cls);
						}
					} catch (Exception e) {
						System.err.println("ReflectPackage: Couldnt get Class from jar for "+pckgname+'.'+files[i]+": "+e.getMessage());
					} catch (Error e) {
						System.err.println("ReflectPackage: Couldnt get Class from jar for "+pckgname+'.'+files[i]+": "+e.getMessage());
					}
				} else if (includeSubs) {
					// do a recursive search over subdirs
					File subDir = new File(directory.getAbsolutePath()+File.separatorChar+files[i]);
					if (subDir.exists() && subDir.isDirectory()) {
						cntAdded += getClassesFromDirFltr(set, subDir, pckgname+"."+files[i], includeSubs, reqSuperCls);
					}
				}
			}
		}
		return cntAdded;
	}

	/**
	 * If valid classpath entries are stored but you want to reset them, use this method. The classpath
	 * will then be rescanned on the next request.
	 */
	public static void resetDynCP() {
		dynCP = null;
	}
	
	private static int addClass(HashSet<Class> set, Class cls) {
		if (TRACE) System.out.println("adding class " + cls.getName());
		if (set.contains(cls)) {
			System.err.println("warning, Class " + cls.getName() + " not added twice!");
			return 0;
		} else {
			set.add(cls);
			return 1;
		}
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
	public static int getClassesFromJarFltr(HashSet<Class> set, String jarName, String packageName, boolean includeSubs, Class<?> reqSuperCls){
		boolean isInSubPackage = true;
		int cntAdded = 0;
		
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
									cntAdded += addClass(set, cls);
								}
							} else cntAdded += addClass(set, cls);
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
			missedJarsOnClassPath++;
			if (missedJarsOnClassPath == 0) {
				System.err.println("Couldnt open jar from class path: " + e.getMessage());
				System.err.println("Dirty class path?");
			} else if (missedJarsOnClassPath == 2) System.err.println("Couldnt open jar from class path more than once...");
			//e.printStackTrace();
		}
		return cntAdded;
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
		String classPath = null;
		if (!useFilteredClassPath || (dynCP==null)) {
			classPath = System.getProperty("java.class.path",".");
			if (useFilteredClassPath) {
				try {
					dynCP = getValidCPArray();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			} else dynCP = getClassPathElements();
		}

		if (TRACE) System.out.println("classpath is " + classPath);
		for (int i=0; i<dynCP.length; i++) {
			if (TRACE) System.out.println("reading element "+dynCP[i]);
			if (dynCP[i].endsWith(".jar")) {
				getClassesFromJarFltr(set, dynCP[i], pckg, includeSubs, reqSuperCls);
			} else {
				if (TRACE) System.out.println("reading from files: "+dynCP[i]+" "+pckg);
				getClassesFromFilesFltr(set, dynCP[i], pckg, includeSubs, reqSuperCls);
			}
		}
		Object[] clsArr = set.toArray();
		if (bSort) {
			Arrays.sort(clsArr, new ClassComparator());
		}

		List list;
		list = Arrays.asList(clsArr);
		return (Class[])list.toArray(new Class[list.size()]);
	}

	/**
	 * Request a valid absolute resource path from a relative resource path
	 * by searching the classpath entries. Returns null if the resource is not found.
	 * @param res
	 */
	public static String getResourcePathFromCP(String res) {
		String[] cpEntries = getClassPathElements();
		URL url = ClassLoader.getSystemResource(res);
		if (TRACE) System.out.println(res + ((url == null) ? " not" : " was") + " found by classloader.");
		if (url != null) {
			File f;
			try {
			  f = new File(url.toURI());
			} catch(URISyntaxException e) {
			  f = new File(url.getPath());
			}
			return f.getPath();
		}
		
		File f;
		String fNameSep;
		if (res.startsWith(System.getProperty("file.separator"))) fNameSep = res;
		else fNameSep = System.getProperty("file.separator")+res;

		for (int i=0; i<cpEntries.length; i++) {
			if (!cpEntries[i].endsWith(".jar")) { // its a fs directory (hopefully)
				if (TRACE) System.out.println("reading element "+cpEntries[i]);
				f = new File(cpEntries[i]+fNameSep);
				if (TRACE) System.out.println(res + ((!f.exists()) ? " not" : " was") + " found in " + cpEntries[i]);
				if (f.exists()) return f.getAbsolutePath();
			}
		}
		return null;
	}
	
	/**
	 * Request a valid absolute resource path from a relative resource path
	 * by searching the classpath entries. Returns null if the resource is not found.
	 * @param res
	 */
	public static InputStream getResourceStreamFromCP(String res) {
		
		InputStream in = BasicResourceLoader.instance().getStreamFromResourceLocation(res);
		if (TRACE) System.out.println(res + ((in == null) ? " not" : " was") + " found by classloader.");
		if (in != null) return in;
		
		String[] cpEntries = getClassPathElements();
		
		File f;
		String fNameSep;
		if (res.startsWith(System.getProperty("file.separator"))) fNameSep = res;
		else fNameSep = System.getProperty("file.separator")+res;

		for (int i=0; i<cpEntries.length; i++) {
			if (!cpEntries[i].endsWith(".jar")) { // its a fs directory (hopefully)
				if (TRACE) System.out.println("reading element "+cpEntries[i]);
				f = new File(cpEntries[i]+fNameSep);
				if (TRACE) System.out.println(res + ((!f.exists()) ? " not" : " was") + " found in " + cpEntries[i]);
				if (f.exists()) {
					try {
						return new FileInputStream(f);
					} catch(Exception e) {
						System.err.println("error reading file " + f.getAbsolutePath() + ": " + e.getMessage());
					}
				}
			}
		}
		return null;
	}
	
	public static ArrayList<String> getValidCPEntries() {
		String[] pathElements = getClassPathElements();
		File f;
		ArrayList<String> valids = new ArrayList<String>(pathElements.length);
		for (int i=0; i<pathElements.length; i++) {
//						System.err.println(pathElements[i]);
			f = new File(pathElements[i]);
//						if (f.canRead()) {valids.add(pathElements[i]);}
			if (f.exists() && f.canRead()) {
				valids.add(pathElements[i]);
			}
		}
		return valids;
	}
	
	public static String[] getValidCPArray() {
		ArrayList<String> valids = getValidCPEntries();
//		vp = valids.toArray(dynCP); // this causes Matlab to crash meanly.
		String[] vp = new String[valids.size()];
		for (int i=0; i<valids.size(); i++) vp[i] = valids.get(i);
		return vp;
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
		System.out.println("1: " + cld.getResource("/eva2/server"));
		System.out.println("2: " + cld.getResource("eva2/server"));
//		BasicResourceLoader rld = BasicResourceLoader.instance();
//		byte[] b = rld.getBytesFromResourceLocation("resources/images/Sub24.gif");
//		System.out.println((b == null) ? "null" : b.toString());
//		b = rld.getBytesFromResourceLocation("src/eva2/client/EvAClient.java");
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
//				ArrayList al = getClassesFromJarFltr(pathElements[i], "eva2.server.model", false, null);
//				for (int k=0; k<al.size(); k++) {
//					//if (Class.forName("eva2.server.modules.ModuleAdapter").isAssignableFrom((Class)al.get(i))) 
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
	
    public static Object getHotspotMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Object bean=null;
        try {
        	bean = server.getObjectInstance(new ObjectName("com.sun.management:type=HotSpotDiagnostic"));
			//bean = server.queryMBeans(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
    }
    
    public static Object dumpHeap(String file, boolean live) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Object ret=null;
        try {
//        	void setVMOption(String name, String value)
        	//-Xrunhprof:heap=dump,format=b
//        	ret = server.invoke(new ObjectName("com.sun.management:type=HotSpotDiagnostic"), 
//        			"setVMOption", new Object[]{"agentlib", "hprof=heap=dump,format=a"}, 
//        			new String[] {"java.lang.String","java.lang.String"});
//        	ret = server.invoke(new ObjectName("com.sun.management:type=HotSpotDiagnostic"), "getDiagnosticOptions", new Object[] {}, new String[] {});
        	server.invoke(new ObjectName("com.sun.management:type=HotSpotDiagnostic"), "dumpHeap", new Object[]{file, live}, new String[] {"java.lang.String", "boolean"});
			//bean = server.queryMBeans(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
    }
}