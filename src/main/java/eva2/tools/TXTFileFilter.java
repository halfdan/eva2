/*
 * TXTFileFilter.java
 *
 * Created on 8. Oktober 2002, 14:20
 */

package eva2.tools;

import javax.swing.filechooser.FileFilter;

/**A simple File Filter for *.txt files.
 */

public class TXTFileFilter extends FileFilter {

    /**
     * Creates a new instance of TXTFileFilter
     */
    public TXTFileFilter() {}

    @Override
    public boolean accept(java.io.File file) {
        if (file.isDirectory()) {
            return true;
        }
        String fileName = file.getName();
        return (fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).equals("TXT")) ||
                (fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).equals("txt"));
    }

    @Override
    public String getDescription() {
        return "*.TXT; *.txt";
    }

}
