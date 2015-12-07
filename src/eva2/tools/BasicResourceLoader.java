///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile: BasicResourceLoader.java,v $
//  Purpose:  Atom representation.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg Kurt Wegner, Gerd Mueller
//  Version:  $Revision: 1.3 $
//            $Date: 2005/02/17 16:48:44 $
//            $Author: wegner $
//
// Copyright OELIB:          OpenEye Scientific Software, Santa Fe,
//                           U.S.A., 1999,2000,2001
// Copyright JOELIB/JOELib2: Dept. Computer Architecture, University of
//                           Tuebingen, Germany, 2001,2002,2003,2004,2005
// Copyright JOELIB/JOELib2: ALTANA PHARMA AG, Konstanz, Germany,
//                           2003,2004,2005
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation version 2 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
///////////////////////////////////////////////////////////////////////////////
package eva2.tools;

import eva2.EvAInfo;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Loads resource file from directory OR jar file. Now it is easier possible to
 * access resource files in a directory structure or a .jar/.zip file.
 *
 * @author Marcel Kronfeld
 * @author wegnerj
 * @author Robin Friedman, rfriedman@TriadTherapeutics.com
 * @author Gerd Mueller
 * @license GPL
 */
public class BasicResourceLoader implements ResourceLoader {
    /**
     * Obtain a suitable logger.
     */
    private static Logger logger = Logger.getLogger(BasicResourceLoader.class.getName());
    private static BasicResourceLoader resourceLoader;

    /**
     * Constructor for the ResourceLoader object
     */
    private BasicResourceLoader() {
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static synchronized BasicResourceLoader getInstance() {
        if (resourceLoader == null) {
            resourceLoader = new BasicResourceLoader();
        }

        return resourceLoader;
    }

    /**
     * Description of the Method
     *
     * @param resourceFile Source file to read
     * @return Description of the Return Value
     */
    public static List<String> readLines(String resourceFile) {
        return readLines(resourceFile, false);
    }

    /**
     * Description of the Method
     *
     * @param resourceFile   Source file to read
     * @param ignoreCommentedLines Whether to ignore commented lines
     * @return Description of the Return Value
     */
    public static List<String> readLines(String resourceFile,
                                         boolean ignoreCommentedLines) {
        return readLines(resourceFile, new String[]{"#"}, 0, -1);
    }

    /**
     * Read String lines from a file into a list.
     *
     * @param resourceFile File to read
     * @param ignorePrefix array of prefixes which mark a line to be ignored
     * @param lOffset      offset of the first line to read
     * @param lCnt         number of lines to read, if less than 0, all lines are read
     * @return List of lines which were read or null on an error
     */
    public static List<String> readLines(String resourceFile,
                                         String[] ignorePrefix, int lOffset, int lCnt) {
        if (resourceFile == null) {
            return null;
        }

        byte[] bytes = BasicResourceLoader.getInstance()
                .getBytesFromResourceLocation(
                        resourceFile, false);

        if (bytes == null) {
            return null;
        }

        ByteArrayInputStream sReader = new ByteArrayInputStream(bytes);
        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(
                sReader));

        String line;
        ArrayList<String> lineData = new ArrayList<>(100);

        int lineCnt = 0;
        try {
            while ((line = lnr.readLine()) != null) {
                line = line.trim();
                if (strStartsWithPrefix(line, ignorePrefix) < 0) {
                    if (lineCnt >= lOffset) {
                        lineData.add(line);
                    }
                    lineCnt++;
                    if ((lCnt > 0) && (lineData.size() == lCnt)) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return lineData;
    }

    /**
     * Parse columns of a data array containing double data. Columns may be selected by giving their
     * indices in an int array. If selectedCols is null, all columns are selected. All selected columns
     * are expected to contain double data and to be of same length. If rawData is null, null is returned.
     *
     * @param rawData      Strings containing an array with double data columns
     * @param colSplit     String regexp for the splitting of a line
     * @param selectedCols indices of the columns to retrieve, null for all.
     * @return
     * @see java.util.regex.Pattern
     */
    public static double[][] parseDoubleArray(ArrayList<String> rawData, String colSplit, int[] selectedCols) {
        String[] entries;
        double dat[][] = null;
        if (rawData != null) {
            try {
                for (int i = 0; i < rawData.size(); i++) {
                    entries = rawData.get(i).split(colSplit);
                    if (i == 0) {    // at the first pass
                        dat = new double[rawData.size()][(selectedCols == null) ? entries.length : selectedCols.length];
                    }
                    trimAll(entries);
                    fillLine(dat, i, entries, selectedCols);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
        return dat;
    }

    private static void trimAll(String[] entries) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null) {
                entries[i] = entries[i].trim();
            }
        }
    }

    /**
     * Walk through a 2-d-array and retrieve the first bunch of lines for which the given column data lies
     * within start and end limits, both inclusively. The original array is not altered.
     *
     * @param data  data array to search
     * @param col   column to inspect
     * @param start first value to start retrieving from
     * @param end   last value to retrieve
     * @return
     */
    public static double[][] getLinesByCol(double[][] data, int col, double start, double end) {
        int cnt = 0;
        int startIndex = 0;
        for (int i = 0; i < data.length; i++) {
            if ((data[i][col] >= start) && (data[i][col] <= end)) {
                if (cnt == 0) {
                    startIndex = i;
                }
                cnt++;
            } else if (cnt > 0) {
                break;
            }
        }

        double[][] selData = new double[cnt][data[0].length];
        System.arraycopy(data, startIndex, selData, 0, cnt);
        return selData;
    }

    /**
     * Load double data from a text file. An ignore list of prefixes may be specified. The start line and number of lines
     * to read may be specified, if lCnt is -1, as many lines as possible are read. The cols array may contain an integer
     * list of columns to be read. If null, as many columns as possible are read.
     * The data file is expected to be uniform, meaning that all lines which are not ignored, contain double data values
     * in all columns.
     *
     * @param fname        file name to read
     * @param ignorePrefix lines starting with any of these Strings will be ignored
     * @param colSplit     String regexp for the splitting of a line
     * @param lOffset      start at a certain line (0 for top)
     * @param lCnt         read as many lines, -1 or 0 for all (from offset). Ignored lines are not counted!
     * @param selectedCols indices of the columns to retrieve, null for all.
     * @return
     */
    public static double[][] loadDoubleData(String fname, String[] ignorePrefix, String colSplit, int lOffset, int lCnt, int[] selectedCols) {
        return parseDoubleArray((ArrayList<String>) readLines(fname, ignorePrefix, lOffset, lCnt), colSplit, selectedCols);
    }

    /**
     * Fill a line of an array with double values parsed from a String array. A subset of
     * Columns may be selected by giving their indices in an integer array cols. If cols
     * is null, all are converted.
     * If data is missing from the string array, the double array is filled with NaN.
     * If more data is delivered, an error message will be printed and the superfluous data disregarded.
     *
     * @param dest
     * @param lineCnt
     * @param entries
     * @param cols
     */
    public static void fillLine(double[][] dest, int lineCnt, String[] entries, int[] cols) {
        if (((cols == null) && (dest[lineCnt].length < entries.length)) || (cols != null && (dest[lineCnt].length != cols.length))) {
            System.err.println("error, array dimensions dont match! (BasicResourceLoader)");
        }
        if (cols == null) {
            for (int i = 0; i < dest[lineCnt].length; i++) {
                try {
                    if ((i >= entries.length) || (entries[i] == null || (entries[i].length() == 0))) {
                        dest[lineCnt][i] = Double.NaN;
                    } else {
                        dest[lineCnt][i] = Double.valueOf(entries[i]);
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid Double format in line " + lineCnt + ", data was " + entries[i]);
                    dest[lineCnt][i] = Double.NaN;
                }
            }
        } else {
            for (int i = 0; i < cols.length; i++) {
                try {
                    if ((cols[i] < 0) || ((cols[i] >= entries.length) || (entries[cols[i]] == null || (entries[cols[i]].length() == 0)))) {
                        dest[lineCnt][i] = Double.NaN;
                    } else {
                        dest[lineCnt][i] = Double.valueOf(entries[cols[i]]);
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid Double format in line " + lineCnt + ", data was " + entries[cols[i]]);
                    dest[lineCnt][i] = Double.NaN;
                }
            }
        }
    }

    /**
     * Test a string for prefixes. For the first matching prefix, the index
     * of the prefix within the prefix array is returned. If there is no match -1 is returned.
     *
     * @param str
     * @param pref
     * @return
     */
    public static int strStartsWithPrefix(String str, String[] pref) {
        int i = 0;
        if (pref != null) {
            for (String prefix : pref) {
                if (str.startsWith(prefix)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    /**
     * Gets the byte data from a file at the given resource location.
     *
     * @param rawResrcLoc Description of the Parameter
     * @return the byte array of file.
     */
    public InputStream getStreamFromResourceLocation(String rawResrcLoc) {
        String resourceLocation = rawResrcLoc.replace('\\', '/');

        //System.out.println("Try to get: "+resourceLocation);
        if (resourceLocation == null) {
            return null;
        }

        // to avoid hours of debugging non-found-files under linux with
        // some f... special characters at the end which will not be shown
        // at the console output !!!
        resourceLocation = resourceLocation.trim();
        InputStream in = null;

        // is a relative path defined ?
        // this can only be possible, if this is a file resource location
        if (resourceLocation.startsWith("..") ||
                resourceLocation.startsWith("/") ||
                resourceLocation.startsWith("\\") ||
                ((resourceLocation.length() > 1) &&
                        (resourceLocation.charAt(1) == ':'))) {
            in = getStreamFromFile(resourceLocation);
        }
//        InputStream inTest = getStreamFromFile(resourceLocation);

        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resourceLocation);
        }

        if (in == null) {
            // try again for web start applications
            in = this.getClass().getClassLoader().getResourceAsStream(
                    resourceLocation);
        }

        if (in == null) {
            // try to search other classpaths...? not really necessary.
//        	in = getStreamFromClassPath(resourceLocation);
        }

        if (in == null) {
            logger.log(Level.FINER, "Unable to open stream for " + resourceLocation);
        } else {
            logger.log(Level.FINER, "Stream opened for " + resourceLocation);
        }
        return in;
    }

    /**
     * Gets the byte data from a file at the given resource location.
     *
     * @param rawResrcLoc Description of the Parameter
     * @return the byte array of file.
     */
    @Override
    public byte[] getBytesFromResourceLocation(String rawResrcLoc) {
        return getBytesFromResourceLocation(rawResrcLoc, false);
    }

    /**
     * Gets the byte data from a file at the given resource location.
     *
     * @param rawResrcLoc         Location of the resource
     * @param exceptionOnNotFound if true, a RuntimeException is thrown if the resource was not found.
     * @return the byte array of file.
     */
    public byte[] getBytesFromResourceLocation(String rawResrcLoc, boolean exceptionOnNotFound) {
        InputStream in = getStreamFromResourceLocation(rawResrcLoc);
        byte[] arr = null;
        if (in != null) {
            arr = getBytesFromStream(in);
        }
        if (exceptionOnNotFound && (arr == null)) {
            throw new RuntimeException(EvAInfo.resourceNotFoundErrorMessage(rawResrcLoc));
        }
        return arr;
    }

    /**
     * Gets the byte data from a file contained in a JAR or ZIP file.
     *
     * @param urlToZipArchive     Description of the Parameter
     * @param internalArchivePath Description of the Parameter
     * @return the byte array of the file.
     */
    private byte[] getBytesFromArchive(String urlToZipArchive,
                                       String internalArchivePath) {
        URL url = null;
        int size = -1;
        byte[] b = null;

        try {
            url = new URL(urlToZipArchive);

            // extracts just sizes only.
            ZipFile zf = new ZipFile(url.getFile());
            Enumeration e = zf.entries();

            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();

                if (ze.getName().equals(internalArchivePath)) {
                    if (ze.isDirectory()) {
                        return null;
                    }

                    // only files with <65536 bytes are allowed
                    if (ze.getSize() > 65536) {
                        System.out.println("Resource files should be smaller than 65536 bytes...");
                    }

                    size = (int) ze.getSize();
                }
            }

            zf.close();

            FileInputStream fis = new FileInputStream(url.getFile());
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze = null;

            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals(internalArchivePath)) {
                    b = new byte[size];

                    int rb = 0;
                    int chunk = 0;

                    while ((size - rb) > 0) {
                        chunk = zis.read(b, rb, size - rb);

                        if (chunk == -1) {
                            break;
                        }

                        rb += chunk;
                    }
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }

        return b;
    }

    /**
     * Gets the byte data from a file.
     *
     * @param fileName Description of the Parameter
     * @return the byte array of the file.
     */
    private FileInputStream getStreamFromFile(String fileName) {
        if (fileName.startsWith("/cygdrive/")) {
            int length = "/cygdrive/".length();
            fileName = fileName.substring(length, length + 1) + ":" +
                    fileName.substring(length + 1);
        }

        logger.log(Level.FINER, "Trying to get file from " + fileName);

        File file = new File(fileName);
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            return fis;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }

    }

    /**
     * Gets the byte data from a file.
     *
     * @param fileName Description of the Parameter
     * @return the byte array of the file.
     */
    private byte[] getBytesFromFile(String fileName) {
        FileInputStream fis = getStreamFromFile(fileName);
        if (fis == null) {
            System.err.println("couldnt get file input stream!");
            return null;
        }
        BufferedInputStream bis = new BufferedInputStream(fis);

        // only files with <65536 bytes are allowed
        //if( file.length() > 65536 ) System.out.println("Resource files should be smaller than 65536 bytes...");
        int size = (int) new File(fileName).length();
        byte[] b = new byte[size];
        int rb = 0;
        int chunk = 0;

        try {
            while ((size - rb) > 0) {
                chunk = bis.read(b, rb, size - rb);

                if (chunk == -1) {
                    break;
                }

                rb += chunk;
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);

            return null;
        }

        return b;
    }

    /**
     * Gets the byte data from a file.
     *
     * @param stream Stream to read from
     * @return the byte array of the file.
     */
    private byte[] getBytesFromStream(InputStream stream) {
        if (stream == null) {
            return null;
        }

        BufferedInputStream bis = new BufferedInputStream(stream);

        try {
            int size = bis.available();
            byte[] b = new byte[size];
            int rb = 0;
            int chunk = 0;

            while ((size - rb) > 0) {
                chunk = bis.read(b, rb, size - rb);

                if (chunk == -1) {
                    break;
                }

                rb += chunk;
            }

            return b;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     *
     */
    public static Properties readProperties(String resourceName) throws Exception {
        Properties prop = new Properties();
        BasicResourceLoader loader = BasicResourceLoader.getInstance();

        byte bytes[] = loader.getBytesFromResourceLocation(resourceName, false);
        if (bytes != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            prop.load(bais);
            if (prop != null) {
                return prop;
            }
        }
        /////////////

        int slInd = resourceName.lastIndexOf('/');
        String planBResName;
        if (slInd != -1) {
            planBResName = resourceName.substring(slInd + 1);
        } else {
            planBResName = resourceName;
        }
        Properties userProps = new Properties();
        File propFile = new File(File.separatorChar + planBResName);
        if (propFile.exists()) {
            try {
                userProps.load(new FileInputStream(propFile));
            } catch (Exception ex) {
                System.err.println("Problem reading user properties: " + propFile);
            }
        } else {
            System.err.println("Warning in readProperties: neither " + resourceName + " nor " + planBResName + " could be read.");
        }
        return userProps;
    }
}
