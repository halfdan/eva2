package eva2.tools;

import eva2.gui.BeanInspector;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of miscellaneous static helper methods.
 */
public final class ToolBox {
    /**
     * Private constructor to prevent instances of module class.
     */
    private ToolBox() {
    }

    /**
     * Convert all items of an enum to a String array and append the given String array at the end.
     *
     * @param e The enum to convert
     * @param additionalValues Additional string values
     * @return
     */
    public static String[] appendEnumAndArray(Enum<?> e, String[] additionalValues) {
        Enum<?>[] fields = e.getClass().getEnumConstants();
        int enumLen = fields.length; //values().length;
        int len = enumLen + additionalValues.length;
        String[] ret = new String[len];
        for (int i = 0; i < enumLen; i++) {
            ret[i] = fields[i].toString();
        }

        System.arraycopy(additionalValues, enumLen - enumLen, ret, enumLen, ret.length - enumLen);
        return ret;
    }

    /**
     * Append two String arrays. If both are null, null is returned.
     *
     * @param strArr1 First array
     * @param strArr2 Second array
     * @return A single array containing the merged set of values
     */
    public static String[] appendArrays(String[] strArr1, String[] strArr2) {
        if (strArr1 == null) {
            return strArr2;
        }
        if (strArr2 == null) {
            return strArr1;
        }
        String[] ret = new String[strArr1.length + strArr2.length];
        System.arraycopy(strArr1, 0, ret, 0, strArr1.length);
        System.arraycopy(strArr2, 0, ret, strArr1.length, strArr2.length);
        return ret;
    }

    public static String[] appendArrays(String[] strArr1, String str) {
        String[] ret = new String[strArr1.length + 1];
        System.arraycopy(strArr1, 0, ret, 0, strArr1.length);
        ret[ret.length - 1] = str;
        return ret;
    }

    public static Object[] appendArrays(Object[] objArr1, Object o) {
        Object[] ret = new Object[objArr1.length + 1];
        System.arraycopy(objArr1, 0, ret, 0, objArr1.length);
        ret[ret.length - 1] = o;
        return ret;
    }

    public static Object[] appendArrays(Object[] objArr1, Object[] objArr2) {
        Object[] ret = new Object[objArr1.length + objArr2.length];
        System.arraycopy(objArr1, 0, ret, 0, objArr1.length);
        System.arraycopy(objArr2, 0, ret, objArr1.length, objArr2.length);
        return ret;
    }

    /**
     * For a list of objects, generate an array of Double which contains thee.getClass().getEnumConstants()
     * converted double arrays whenever this is directly possible, or null otherwise.
     * The length of the array will correspond to the length of the given list.
     *
     * @param l A list of Objects
     * @return A double array containing the converted object values
     */
    public static Double[] parseDoubles(List<Object> l) {
        ArrayList<Double> values = new ArrayList<>();
        for (Object o : l) {
            values.add(toDouble(o)); // null if unsuccessful
        }
        return values.toArray(new Double[values.size()]);
    }

    /**
     * Try to convert a Double from a given Object. Return null
     * if conversion fails (e.g. because the Object is a complex data type
     * which has no straight-forward numeric representation, e.g. an array).
     *
     * @param o
     * @return
     */
    public static Double toDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            try {
                return Double.parseDouble(BeanInspector.toString(o));
            } catch (Exception e) {
                // Here be dragons!
            }
        }
        return null;
    }

    /**
     * For an array of objects, generate an array of Double which contains the
     * converted double arrays whenever this is directly possible, or null otherwise.
     *
     * @param os
     * @return Double array containing the converted object values.
     */
    public static Double[] parseDoubles(Object[] os) {
        Double[] vals = new Double[os.length];
        for (int i = 0; i < os.length; i++) {
            vals[i] = toDouble(os[i]);
        }
        return vals;
    }

    /**
     * Return an array containing only those lines which have values within
     * lower and upper bound (included) in the indexed column.
     *
     * @param dat   a 2D double array
     * @param i     index of the column to look at
     * @param lower lower bound of values to filter rows for
     * @param upper upper bound of values to filter rows for
     * @return a filtered 2D double array where value[*][i] in [lower,upper]
     */
    public static double[][] filterBy(double[][] dat, int i, double lower, double upper) {
        if (dat == null || dat.length == 0) {
            return dat;
        }
        if (i >= dat[0].length) {
            System.err.println("Error, invalid column index " + i + " for data array with " + dat[0].length + " columns!");
        }
        ArrayList<double[]> matching = new ArrayList<>(5);
        for (double[] row : dat) {
            if (row[i] <= upper && row[i] >= lower) {
                matching.add(row);
            }
        }

        return matching.toArray(new double[matching.size()][dat[0].length]);
    }

    /**
     * Retrieve a given number of columns from a double matrix. The given
     * data array must have valid matrix dimensions (equal number of columns per row).
     *
     * @param data a 2D double array
     * @param cols the indices of columns in data to return
     * @return a 2D double array containing the indexed columns from data
     */
    public static double[][] getCols(double[][] data, int... cols) {
        if (data == null || (data[0] == null)) {
            return null;
        }
        int nCols = cols.length;
        if (nCols > data[0].length) {
            System.err.println("Error, mismatching column count in Mathematics.getCols!");
        }
        double[][] ret = new double[data.length][cols.length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                ret[i][j] = data[i][cols[j]];
            }
        }
        return ret;
    }
}
