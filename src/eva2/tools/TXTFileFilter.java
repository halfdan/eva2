/*
 * TXTFileFilter.java
 *
 * Created on 8. Oktober 2002, 14:20
 */

package eva2.tools;

import javax.swing.filechooser.FileFilter;

/** TXTFileFilter
 * Description:     A simple File Filter for *.txt files.
 * Copyright:       Copyright (c) 2001
 * Company:         University of Tuebingen, Computer Architecture
 * @author          Felix Streichert
 * @version:        $Revision: 10 $
 *                  $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *                  $Author: streiche $
 * @since           JDK 1.3.0_02
 */

public class TXTFileFilter extends FileFilter {

    /** Creates a new instance of TXTFileFilter */
    public TXTFileFilter() {
    }

    @Override
    public boolean accept(java.io.File file) {
        if (file.isDirectory()) {
            return true;
        }
        String fileName = file.getName();
        if ((fileName.substring(fileName.lastIndexOf('.')+1, fileName.length()).equals("TXT")) ||
            (fileName.substring(fileName.lastIndexOf('.')+1, fileName.length()).equals("txt"))) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "*.TXT; *.txt";
    }

}
