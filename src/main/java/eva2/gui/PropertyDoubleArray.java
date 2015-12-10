package eva2.gui;

/**
 * A property for a double array. May be of dimensions m times n.
 */
public class PropertyDoubleArray implements java.io.Serializable {
    private double[][] doubleArray;
    private int numCols = 1;

    public PropertyDoubleArray(double[] d) {
        setDoubleArray(d);
    }

    public PropertyDoubleArray(double[][] d) {
        setDoubleArray(d);
    }

    public PropertyDoubleArray(PropertyDoubleArray d) {
        this.doubleArray = d.doubleArray.clone();
        this.numCols = d.numCols;
//        System.arraycopy(d.doubleArray, 0, this.doubleArray, 0, this.doubleArray.length);
    }

    /**
     * Constructor that creates a double matrix with given dimensions and fills
     * it cyclically with values given.
     *
     * @param rows
     * @param cols
     * @param d
     */
    public PropertyDoubleArray(int rows, int cols, double... d) {
        if (rows > 0 && cols > 0) {
            this.doubleArray = new double[rows][cols];
        } else {
            this.doubleArray = null;
        }
        this.numCols = cols;
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                doubleArray[i][j] = d[index];
                index++;
                if (index >= d.length) {
                    index = 0;
                }
            }
        }
    }

    @Override
    public Object clone() {
        return new PropertyDoubleArray(this);
    }

    /**
     * This method will allow you to set the value of the double array
     *
     * @param d The double[]
     */
    public void setDoubleArray(double[] d) {
        this.doubleArray = new double[d.length][1];
        for (int i = 0; i < d.length; i++) {
            doubleArray[i][0] = d[i];
        }
        numCols = 1;
    }

    /**
     * This method will allow you to set the value of the double array
     *
     * @param d The double[]
     */
    public void setDoubleArray(double[][] d) {
        this.doubleArray = d;
        if (d.length > 0) {
            numCols = d[0].length;
        } else {
            numCols = 1;
        }
    }

    /**
     * @return the double array itself (no clone)
     */
    public double[][] getDoubleArrayShallow() {
        return this.doubleArray;
    }

    /**
     * Return a column as a vector (in copy)
     *
     * @return a column as a vector (in copy)
     */
    public double[] getDoubleColumnAsVector(int col) {
        if (col >= numCols) {
            throw new IllegalArgumentException("Error, invalid column selected, " + col + " of " + numCols);
        }
        double[] ret = new double[doubleArray.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubleArray[i][col];
        }
        return ret;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumRows() {
        return doubleArray.length;
    }

    public double getValue(int i, int j) {
        if (i < 0 || j < 0 || (i >= getNumRows()) || (j >= getNumCols())) {
            throw new IllegalArgumentException("Error, invalid access to double array: " + i + "," + j + " within " + getNumRows() + "," + getNumCols());
        }
        return doubleArray[i][j];
    }

    public void adaptRowCount(int k) {
        if (k != doubleArray.length) {
            double[][] newDD = new double[k][numCols];
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < numCols; j++) {
                    if (i < doubleArray.length) {
                        newDD[i][j] = doubleArray[i][j];
                    } else {
                        newDD[i][j] = doubleArray[doubleArray.length - 1][j];
                    }
                }
            }
            setDoubleArray(newDD);
        }
    }

    public void deleteRow(int k) {
        if (k < 0 || k >= getNumRows()) {
            throw new IllegalArgumentException("Invalid index to deleteRow: " + k + " is not a valid row.");
        }
        double[][] newDD = new double[getNumRows() - 1][getNumCols()];
        int inc = 0;
        for (int i = 0; i < newDD.length; i++) {
            if (i == k) {
                inc = 1;
            }
            System.arraycopy(doubleArray[i + inc], 0, newDD[i], 0, getNumCols());
        }
        setDoubleArray(newDD);
    }

    /**
     * Add a copy of an indexed row at the end. If the given index
     * is invalid, the last row is copied.
     *
     * @param k
     */
    public void addRowCopy(int k) {
        if (k < 0 || k >= getNumRows()) {
            k = getNumRows() - 1;
        }
        double[][] newDD = new double[getNumRows() + 1][getNumCols()];

        for (int i = 0; i < getNumRows(); i++) {
            System.arraycopy(doubleArray[i], 0, newDD[i], 0, getNumCols());
        }
        if (k >= 0) {
            System.arraycopy(newDD[k], 0, newDD[newDD.length - 1], 0, getNumCols());
        } else {
            for (int j = 0; j < getNumCols(); j++) {
                newDD[newDD.length - 1][j] = 1.;
            }
        } // if the array was empty
        setDoubleArray(newDD);
    }

    /**
     * Normalize all columns of the array by dividing through the sum.
     */
    public void normalizeColumns() {
        double colSum = 0;
        for (int j = 0; j < getNumCols(); j++) {
            colSum = 0;
            for (int i = 0; i < getNumRows(); i++) {
                colSum += doubleArray[i][j];
            }
            if (colSum != 0) {
                for (int i = 0; i < getNumRows(); i++) {
                    doubleArray[i][j] /= colSum;
                }
            }
        }
    }

    /**
     * Check if k is a valid row index (within 0 and numRows-1).
     *
     * @param k
     * @return
     */
    public boolean isValidRow(int k) {
        return (k >= 0) && (k < getNumRows());
    }

    @Override
    public String toString() {
        return BeanInspector.toString(doubleArray);
    }

//    /** This method will allow you to set the value of the double array
//     * @param d     The double[]
//     */
//    public void setDoubleArray(double[] d) {
//        this.doubleArray = new double[d.length][1];
//        for (int i=0; i<d.length; i++) doubleArray[i][0] = d[i];
//    }
//
//    /** This method will return the complete name of the file
//     * which filepath
//     * @return The complete filename with path.
//     */
//    public double[] getDoubleArray() {
//        return this.doubleArray;
//    }
}