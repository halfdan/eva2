package javaeva.server.go.tools;

import wsi.ra.tool.BasicResourceLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 09.06.2005
 * Time: 13:01:18
 * To change this template use File | Settings | File Templates.
 */
public class FileLoader {

    /** This method will load a file and will try to do this using the
     * BasicResourceLoader and the standart technique.
     * @param file      The full filename and path
     * @return A string[] containing the lines in the file
     */
    static public String[] loadStringsFromFile(String file) {
        String[] result = null;
        BasicResourceLoader  loader = BasicResourceLoader.instance();
        byte            bytes[] = loader.getBytesFromResourceLocation(file);
        if (bytes != null) {
            String      data = new String(bytes);
            result      = data.split("\n");
        } else {
            System.out.println("BasicResourceLoader failed to read "+file);
            BufferedReader reader= null;
            try {
                reader = new BufferedReader(new FileReader(file));
            } catch (java.io.FileNotFoundException e) {
                System.out.println("Could not find " + file);
                return result;
            }
            String      currentLine;
            try {
                ArrayList tmpA = new ArrayList();
                while((currentLine=reader.readLine()) != null && currentLine.length()!=0) {
                    currentLine = currentLine.trim();
                    tmpA.add(currentLine);
                }
                result = new String[tmpA.size()];
                for (int i = 0; i < tmpA.size(); i++) {
                    result[i] = (String)tmpA.get(i);
                }
                reader.close();
            } catch (java.io.IOException e) {
                System.out.println("Java.io.IOExeption: " + e.getMessage());
            }
        }
        return result;
    }
}
