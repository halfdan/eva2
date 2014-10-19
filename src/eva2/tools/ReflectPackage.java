package eva2.tools;

import eva2.gui.BeanInspector;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


/**
 * Allow for java to list Classes that exist in one package and can be instantiated from
 * the classpath, either directly or through a jar on the classpath.
 * So far, jars which are located <i>within</i> another jar will not be searched.
 *
 * @author mkron
 */
public class ReflectPackage {
    static int missedJarsOnClassPath = 0;
    static boolean useFilteredClassPath = true;
    static String[] dynCP = null;

    static class ClassComparator<T> implements Comparator<T> {
        @Override
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
                dir = path + "/" + pckgname.replace(".", "/");

                directory = new File(dir);

            } catch (NullPointerException x) {
                return 0;
            }
            if (directory.exists()) {
                // Get the list of the files contained in the package
                return getClassesFromDirFltr(set, directory, pckgname, includeSubs, reqSuperCls);
            } else {
                return 0;
            }
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public static int getClassesFromDirFltr(HashSet<Class> set, File directory, String pckgname, boolean includeSubs, Class<?> reqSuperCls) {
        int cntAdded = 0;
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (String file : files) {
                // we are only interested in .class files
                if (file.endsWith(".class")) {
                    // removes the .class extension
                    try {
                        Class<?> cls = Class.forName(pckgname + '.' + file.substring(0, file.length() - 6));
                        if (reqSuperCls != null) {
                            if (reqSuperCls.isAssignableFrom(cls)) {
                                cntAdded += addClass(set, cls);
                            }
                        } else {
                            cntAdded += addClass(set, cls);
                        }
                    } catch (Exception e) {
                        System.err.println("ReflectPackage: Couldnt get Class from jar for " + pckgname + '.' + file + ": " + e.getMessage());
                    } catch (Error e) {
                        System.err.println("ReflectPackage: Couldnt get Class from jar for " + pckgname + '.' + file + ": " + e.getMessage());
                    }
                } else if (includeSubs) {
                    // do a recursive search over subdirs
                    File subDir = new File(directory.getAbsolutePath() + File.separatorChar + file);
                    if (subDir.exists() && subDir.isDirectory()) {
                        cntAdded += getClassesFromDirFltr(set, subDir, pckgname + "." + file, includeSubs, reqSuperCls);
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
        if (set.contains(cls)) {
            System.err.println("warning, Class " + cls.getName() + " not added twice!");
            return 0;
        } else {
            set.add(cls);
            return 1;
        }
    }

    public static ArrayList<Class> filterAssignableClasses(ArrayList<Class> classes, Class<?> reqSuperCls) {
        ArrayList<Class> assClasses = new ArrayList<>();
        for (Class aClass : classes) {
            if (reqSuperCls.isAssignableFrom(aClass)) {
                assClasses.add(aClass);
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
    public static int getClassesFromJarFltr(HashSet<Class> set, String jarName, String packageName, boolean includeSubs, Class<?> reqSuperCls) {
        boolean isInSubPackage = true;
        int cntAdded = 0;

        packageName = packageName.replaceAll("\\.", "/");
        try {
            JarInputStream jarFile = new JarInputStream
                    (new FileInputStream(jarName));
            JarEntry jarEntry;

            while ((jarEntry = jarFile.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                if ((jarEntryName.startsWith(packageName)) &&
                        (jarEntryName.endsWith(".class"))) {
//					subpackages are hit here as well!
                    if (!includeSubs) { // check if the class belongs to a subpackage
                        int lastDash = jarEntryName.lastIndexOf('/');
                        isInSubPackage = lastDash > packageName.length() + 1;
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
                            } else {
                                cntAdded += addClass(set, cls);
                            }
                        } catch (Exception | Error e) {
                            System.err.println("ReflectPackage: Couldn't get Class from jar for " + clsName + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            missedJarsOnClassPath++;
            if (missedJarsOnClassPath == 0) {
                System.err.println("Couldn't open jar from class path: " + e.getMessage());
                System.err.println("Dirty class path?");
            } else if (missedJarsOnClassPath == 2) {
                System.err.println("Couldn't open jar from class path more than once...");
            }
        }
        return cntAdded;
    }

    /**
     * Collect all classes from a given package on the classpath. If includeSubs is true,
     * the sub-packages are listed as well.
     *
     * @param pkg           Package name
     * @param includeSubs   Whether to include sub packages
     * @param bSort         sort alphanumerically by class name
     * @return An ArrayList of Class objects contained in the package which may be empty if an error occurs.
     */
    public static Class[] getAllClassesInPackage(String pkg, boolean includeSubs, boolean bSort) {
        return getClassesInPackageFltr(new HashSet<Class>(), pkg, includeSubs, bSort, null);
    }

    /**
     * Collect classes from a given package on the classpath which have the given Class
     * as superclass or superinterface. If includeSubs is true,
     * the sub-packages are listed as well.
     *
     * @param pkg
     * @return
     */
    public static Class[] getClassesInPackageFltr(HashSet<Class> set, String pkg, boolean includeSubs, boolean bSort, Class reqSuperCls) {
        if (!useFilteredClassPath || (dynCP == null)) {
            if (useFilteredClassPath) {
                try {
                    dynCP = getValidCPArray();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else {
                dynCP = getClassPathElements();
            }
        }

        for (String aDynCP : dynCP) {
            if (aDynCP.endsWith(".jar")) {
                getClassesFromJarFltr(set, aDynCP, pkg, includeSubs, reqSuperCls);
            } else {
                getClassesFromFilesFltr(set, aDynCP, pkg, includeSubs, reqSuperCls);
            }
        }
        Object[] clsArr = set.toArray();
        if (bSort) {
            Arrays.sort(clsArr, new ClassComparator());
        }

        List list;
        list = Arrays.asList(clsArr);
        return (Class[]) list.toArray(new Class[list.size()]);
    }

    /**
     * Request a valid absolute resource path from a relative resource path
     * by searching the classpath entries. Returns null if the resource is not found.
     *
     * @param res
     */
    public static String getResourcePathFromCP(String res) {
        String[] cpEntries = getClassPathElements();
        URL url = ClassLoader.getSystemResource(res);
        if (url != null) {
            File f;
            try {
                f = new File(url.toURI());
            } catch (URISyntaxException e) {
                f = new File(url.getPath());
            }
            return f.getPath();
        }

        File f;
        String fNameSep;
        if (res.startsWith(System.getProperty("file.separator"))) {
            fNameSep = res;
        } else {
            fNameSep = System.getProperty("file.separator") + res;
        }

        for (String cpEntry : cpEntries) {
            if (!cpEntry.endsWith(".jar")) { // its a fs directory (hopefully)
                f = new File(cpEntry + fNameSep);
                if (f.exists()) {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

    /**
     * Request a valid absolute resource path from a relative resource path
     * by searching the classpath entries. Returns null if the resource is not found.
     *
     * @param res
     */
    public static InputStream getResourceStreamFromCP(String res) {

        InputStream in = BasicResourceLoader.instance().getStreamFromResourceLocation(res);
        if (in != null) {
            return in;
        }

        String[] cpEntries = getClassPathElements();

        File f;
        String fNameSep;
        if (res.startsWith(System.getProperty("file.separator"))) {
            fNameSep = res;
        } else {
            fNameSep = System.getProperty("file.separator") + res;
        }

        for (String cpEntry : cpEntries) {
            if (!cpEntry.endsWith(".jar")) { // its a fs directory (hopefully)
                f = new File(cpEntry + fNameSep);
                if (f.exists()) {
                    try {
                        return new FileInputStream(f);
                    } catch (Exception e) {
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
        ArrayList<String> valids = new ArrayList<>(pathElements.length);
        for (String pathElement : pathElements) {
            f = new File(pathElement);
            if (f.exists() && f.canRead()) {
                valids.add(pathElement);
            }
        }
        return valids;
    }

    public static String[] getValidCPArray() {
        ArrayList<String> valids = getValidCPEntries();
        String[] vp = new String[valids.size()];
        for (int i = 0; i < valids.size(); i++) {
            vp[i] = valids.get(i);
        }
        return vp;
    }

    /**
     * Retrieve assignable classes of the given package from classpath given by package name and Class instance.
     *
     * @param pckg        String denoting the package
     * @param reqSuperCls
     * @return
     */
    public static Class<?>[] getAssignableClassesInPackage(String pckg, Class reqSuperCls, boolean includeSubs, boolean bSort) {
        return getClassesInPackageFltr(new HashSet<Class>(), pckg, includeSubs, bSort, reqSuperCls);
    }

    /**
     * Retrieve assignable classes of the given package from classpath given by full class and package String,
     * such as eva2.problems.AbstractOptimizationProblem.
     *
     * @param pckgClassName        String denoting the package
     * @param includeSubs
     * @param bSort
     * @return
     */
    public static Class<?>[] getAssignableClasses(String pckgClassName, boolean includeSubs, boolean bSort) {
        int dotIndex = pckgClassName.lastIndexOf('.');
        if (dotIndex <= 0) {
            System.err.println("warning: " + pckgClassName + " is not a package!");
            return null;
        } else {
            String pckg = pckgClassName.substring(0, pckgClassName.lastIndexOf('.'));
            Class<?>[] clsArr;
            try {
                clsArr = ReflectPackage.getAssignableClassesInPackage(pckg, Class.forName(pckgClassName), includeSubs, bSort);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                clsArr = null;
            }
            return clsArr;
        }
    }

    public static void main(String[] args) {
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        System.out.println("1: " + cld.getResource("/eva2/optimization"));
        System.out.println("2: " + cld.getResource("eva2/optimization"));

        HashSet<String> h = new HashSet<>(20);

        for (int i = 0; i < 20; i++) {
            h.add("String " + (i % 10));
        }
        for (String string : h) {
            System.out.println("+ " + string);
        }
    }

    public static String[] getClassPathElements() {
        String classPath = System.getProperty("java.class.path", ".");
        return classPath.split(File.pathSeparator);
    }

    public static Object getHotspotMBean() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Object bean = null;
        try {
            bean = server.getObjectInstance(new ObjectName("com.sun.management:type=HotSpotDiagnostic"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bean;
    }

    public static Object dumpHeap(String file, boolean live) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Object ret = null;
        try {
            server.invoke(new ObjectName("com.sun.management:type=HotSpotDiagnostic"), "dumpHeap", new Object[]{file, live}, new String[]{"java.lang.String", "boolean"});
            //bean = server.queryMBeans(null, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Instantiate a class given by full name (with package) and try to set the member values given
     * in the pair-value list. Returns null if the instance could not be created or any name-value
     * pair could not be set. Otherwise the created object is returned.
     *
     * @param clsName
     * @param paramValuePairs
     * @return
     */
    public static Object instantiateWithParams(String clsName, List<Pair<String, Object>> paramValuePairs) {
        return instantiateWithParams(clsName, new Object[]{}, paramValuePairs);
    }

    /**
     * Instantiate a class given by full name (with package) and try to set the member values given
     * in the pair-value list. Returns null if the instance could not be created or any name-value
     * pair could not be set. Otherwise the created object is returned.
     *
     * @param clsName         name of the target class with full package path
     * @param args            constructor arguments
     * @param paramValuePairs pairs of values to set using generic setter methods
     * @return
     */
    public static Object instantiateWithParams(String clsName, Object[] args, List<Pair<String, Object>> paramValuePairs) {
        Object o = getInstance(clsName, args);
        if (o != null) {
            if (paramValuePairs != null) {
                for (Pair<String, Object> nameVal : paramValuePairs) {
                    boolean succ = BeanInspector.setMem(o, nameVal.head, nameVal.tail);
                    if (!succ) {
                        System.err.println("Error, unable to set " + nameVal.head + " to " + nameVal.tail + " in object " + o);
                        return null;
                    }
                }
            }
            return o;
        } else {
            System.err.println("Error in instantiateWithParams!");
            return null;
        }
    }

    /**
     * Retrieve an instance of a generic object with arbitrary arguments. Note that the
     * full package path must be given and the argument array must match a signature of
     * an existing constructor.
     * Returns null on a failure and the constructed object otherwise.
     *
     * @param clsName
     * @param args
     * @return
     */
    public static Object getInstance(String clsName, Object[] args) {
        Object o;
        try {
            Class<?> clz = Class.forName(clsName);
            Class<?>[] argClz = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argClz[i] = args[i].getClass();
            }
            Constructor<?> ct;
            try {
                ct = clz.getConstructor(argClz);
                o = ct.newInstance(args);
            } catch (Exception e) {
                System.err.println("Unable to retrieve constructor of " + clsName + ", arguments " + BeanInspector.toString(args) + "\n" + e.getClass());
                System.err.println(e.getMessage());
                e.printStackTrace();
                o = null;
            }
        } catch (Exception e) {
            System.err.println("Unable to create instance of " + clsName + ", arguments " + BeanInspector.toString(args) + "\n" + e.getMessage());
            e.printStackTrace(System.err);
            o = null;
        }
        return o;
    }
}