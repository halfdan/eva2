package eva2.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a
 * Zip or Jar file. Additionally, it provides a method to extract one
 * as a blob.
 */
public final class JarResources {

    // external debug flag
    public boolean debugOn = false;

    // jar resource mapping tables
    private Hashtable<String,Integer> htSizes = new Hashtable<>();
    private Hashtable<String,byte[]> htJarContents = new Hashtable<>();

    // a jar file
    private String jarFileName;

    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal HashTable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     */
    public JarResources(String jarFileName) {
        this.jarFileName = jarFileName;
        init();
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
     */
    public byte[] getResource(String name) {
        return htJarContents.get(name);
    }

    /**
     * initializes internal hash tables with Jar file resources.
     */
    private void init() {
        try {
            // extracts just sizes only.
            ZipFile zf = new ZipFile(jarFileName);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (debugOn) {
                    System.out.println(dumpZipEntry(ze));
                }
                htSizes.put(ze.getName(), (int) ze.getSize());
            }
            zf.close();

            // extract resources and put them into the HashTable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (debugOn) {
                    System.out.println(
                            "ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize()
                    );
                }
                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1) {
                    size = htSizes.get(ze.getName());
                }
                byte[] b = new byte[size];
                int rb = 0;
                int chunk;
                while ((size - rb) > 0) {
                    chunk = zis.read(b, rb, size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }
                // add to internal resource HashTable
                htJarContents.put(ze.getName(), b);
                if (debugOn) {
                    System.out.println(
                            ze.getName() + "  rb=" + rb +
                                    ",size=" + size +
                                    ",csize=" + ze.getCompressedSize()
                    );
                }
            }
        } catch (NullPointerException e) {
            System.out.println("done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Dumps a zip entry into a string.
     *
     * @param ze a ZipEntry
     */
    private String dumpZipEntry(ZipEntry ze) {
        StringBuilder sb = new StringBuilder();
        if (ze.isDirectory()) {
            sb.append("d ");
        } else {
            sb.append("f ");
        }
        if (ze.getMethod() == ZipEntry.STORED) {
            sb.append("stored   ");
        } else {
            sb.append("defalted ");
        }
        sb.append(ze.getName());
        sb.append("\t");
        sb.append("").append(ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED) {
            sb.append("/").append(ze.getCompressedSize());
        }
        return (sb.toString());
    }

    /**
     * Is a test driver. Given a jar file and a resource name, it tries to
     * extract the resource and then tells us whether it could or not.
     * <p>
     * <strong>Example</strong>
     * Let's say you have a JAR file which jarred up a bunch of gif image
     * files. Now, by using JarResources, you could extract, create, and display
     * those images on-the-fly.
     * <pre>
     *     ...
     *     JarResources JR=new JarResources("GifBundle.jar");
     *     Image image=Toolkit.createImage(JR.getResource("logo.gif");
     *     Image logo=Toolkit.getDefaultToolkit().createImage(
     *                   JR.getResources("logo.gif")
     *                   );
     *     ...
     * </pre>
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println(
                    "usage: java JarResources <jar file name> <resource name>"
            );
            System.exit(1);
        }
        JarResources jr = new JarResources(args[0]);
        byte[] buff = jr.getResource(args[1]);
        if (buff == null) {
            System.out.println("Could not find " + args[1] + ".");
        } else {
            System.out.println("Found " + args[1] + " (length=" + buff.length + ").");
        }
    }

}
