package eva2.optimization.tools;

import eva2.gui.BeanInspector;
import eva2.tools.BasicResourceLoader;
import eva2.tools.StringTools;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 */
public class FileTools {

    /**
     * This method will load a file and will try to do this using the
     * BasicResourceLoader and the standart technique.
     *
     * @param file The full filename and path
     * @return A string[] containing the lines in the file
     */
    static public String[] loadStringsFromFile(String file) {
        String[] result = null;
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte bytes[] = loader.getBytesFromResourceLocation(file, false);
        if (bytes != null) {
            String data = new String(bytes);
            result = data.split("\n");
        } else {
            System.err.println("BasicResourceLoader failed to read " + file);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Could not find " + file);
                return result;
            }
            String currentLine;
            try {
                ArrayList tmpA = new ArrayList();
                while ((currentLine = reader.readLine()) != null && currentLine.length() != 0) {
                    currentLine = currentLine.trim();
                    tmpA.add(currentLine);
                }
                result = new String[tmpA.size()];
                for (int i = 0; i < tmpA.size(); i++) {
                    result[i] = (String) tmpA.get(i);
                }
                reader.close();
            } catch (java.io.IOException e) {
                System.err.println("Java.io.IOExeption: " + e.getMessage());
            }
        }
        return result;
    }


    /**
     * Write a given string to a file. Uses {@link #createNewFile(String)} to
     * create a new file.
     *
     * @param filename target file name
     * @param str      String to write
     * @return the File instance or null on a failure
     */
    public static File writeString(String filename, String str) {
        File f = null;
        try {
            f = createNewFile(filename);
            PrintWriter Out = new PrintWriter(new FileOutputStream(f));
            Out.println(str);
            Out.flush();
            Out.close();
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
            return null;
        }
        return f;
    }

    /**
     * Try to create a new file. If the filename exists, numbers are appended (with dot)
     * until a non-existing file name is found and the new file can be created.
     *
     * @param filename
     * @return
     */
    public static File createNewFile(String filename) {
        File f = new File(filename);
        try {
            if (!f.createNewFile()) {
                int i = 1;
                do {
                    f = new File(filename + "." + i);
                } while (!f.createNewFile());
                return f;
            } else {
                return f;
            }
        } catch (IOException e) {
            System.err.println("IOException when trying to create new file!");
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Saves the current object to a file selected by the user. Strings are written using
     * a PrintWriter while other objects are written using an ObjectOutputStream.
     *
     * @param parentComponent the parent component
     * @param object          The object to save.
     */
    public static boolean saveObjectWithFileChooser(Component parentComponent, Object object, FileFilter filter) {
        int returnVal;
        File sFile;
        boolean finished = false;
        do {
            JFileChooser fc = createFileChooser();
            fc.setFileFilter(filter);
            returnVal = fc.showSaveDialog(parentComponent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                sFile = fc.getSelectedFile();
                if (sFile.exists()) {
                    int opt = JOptionPane.showConfirmDialog(parentComponent, "File " + sFile.getName() + " exists! Overwrite?", "Confirm to overwrite file", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (opt == JOptionPane.OK_OPTION) {
                        finished = true;
                    }
                    if (opt == JOptionPane.CANCEL_OPTION) {
                        return false;
                    }
                } else {
                    finished = true;
                }
            } else {
                return false;
            } // user break
        } while (!finished); // wait until user selected valid file

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                if (object instanceof String) {
                    PrintWriter Out = new PrintWriter(new FileOutputStream(sFile));
                    Out.println((String) object);
                    Out.flush();
                    Out.close();
                } else {
                    ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(sFile)));
                    oo.writeObject(object);
                    oo.close();
                }
                return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Couldn't write to file: "
                                + sFile.getName()
                                + "\n" + ex.getMessage(),
                        "Save object",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Opens an object from a file selected by the user.
     *
     * @return the loaded object, or null if the operation was cancelled
     */
    public static Object openObject(Component parentComponent, Class clazz, FileFilter filter) {
        Object obj = null;
        JFileChooser fc = createFileChooser();
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(parentComponent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            try {
                obj = new Yaml().load(new FileInputStream(selected.getAbsolutePath()));
                if (!clazz.isAssignableFrom(obj.getClass())) {
                    throw new Exception("Object not of type: " + clazz.getName());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Couldn't read object: "
                                + selected.getName()
                                + "\n" + ex.getMessage(),
                        "Open object file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        if (fc != null) {
            fc = null;
        }
        return obj;
    }

    public static JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser(new File("resources"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return fileChooser;
    }

    public static boolean saveObjectToFolder(Object object, File folder, boolean forceOverwrite, Component parentComponent) {
        String predefName;
        try {
            predefName = (String) BeanInspector.callIfAvailable(object, "getName", null);
            predefName = StringTools.simplifySymbols(predefName) + ".yml";
        } catch (Exception e) {
            predefName = object.getClass().getName();
            predefName = StringTools.simplifySymbols(predefName) + ".yml";
        }

        if (!folder.exists()) {
            System.err.println("Error, folder " + folder + " does not exist, aborting saveObjectToFolder.");
            return false;
        } else {
            File outFile = new File(folder, predefName);
            if (outFile.exists() && !forceOverwrite) {
                int opt = JOptionPane.showConfirmDialog(parentComponent, "File " + outFile.getName() + " exists! Overwrite?", "Confirm to overwrite file", JOptionPane.YES_NO_CANCEL_OPTION);
                if (opt != JOptionPane.OK_OPTION) {
                    return false;
                }
            }
            // we may save the file
            String msg;
            if ((msg = saveObject(object, outFile)) != null) {
                JOptionPane.showMessageDialog(parentComponent,
                        "Couldn't save object: "
                                + object
                                + "\n" + msg,
                        "Save object to folder",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Store the given object directly without any testing. Returns null on success,
     * otherwise the error message of an exception that occurred.
     *
     * @param object
     * @param target
     * @return
     */
    public static String saveObject(Object object, File target) {
        try {
            if (object instanceof String) {
                PrintWriter Out = new PrintWriter(new FileOutputStream(target));
                Out.println((String) object);
                Out.flush();
                Out.close();
            } else {
                ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
                oo.writeObject(object);
                oo.close();
            }
            return null;
        } catch (Exception ex) {
            System.err.println("Unable to write to file " + target.getAbsolutePath());
            return ex.getMessage();
        }
    }
}
