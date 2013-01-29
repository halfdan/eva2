package eva2.gui;

/**
 * A property for a double array. May be of dimensions m times n.
 */
public class PropertyDoubleArray implements java.io.Serializable {
    private double[][] m_DoubleArray;
    private int m_numCols = 1;

    public PropertyDoubleArray(double[] d) {
    	setDoubleArray(d);
    }
    
    public PropertyDoubleArray(double[][] d) {
    	setDoubleArray(d);
    }
    
    public PropertyDoubleArray(PropertyDoubleArray d) {
        this.m_DoubleArray = d.m_DoubleArray.clone();
        this.m_numCols = d.m_numCols;
//        System.arraycopy(d.m_DoubleArray, 0, this.m_DoubleArray, 0, this.m_DoubleArray.length);
    }

    /**
     * Constructor that creates a double matrix with given dimensions and fills
     * it cyclically with values given.
     * @param rows
     * @param cols
     * @param d
     */
    public PropertyDoubleArray(int rows, int cols, double ... d) {
    	if (rows>0 && cols>0) {
            this.m_DoubleArray = new double[rows][cols];
        }
    	else {
            this.m_DoubleArray=null;
        }
    	this.m_numCols=cols;
    	int index=0;
    	for (int i=0; i<rows; i++) {
    		for (int j=0; j<cols; j++) {
    			m_DoubleArray[i][j]=d[index];
    			index++;
    			if (index>=d.length) {
                        index=0;
                    }
    		}
    	}
	}

    @Override
	public Object clone() {
        return (Object) new PropertyDoubleArray(this);
    }

    /** This method will allow you to set the value of the double array
     * @param d     The double[]
     */
    public void setDoubleArray(double[] d) {
    	this.m_DoubleArray = new double[d.length][1];
    	for (int i=0; i<d.length; i++) {
            m_DoubleArray[i][0] = d[i];
        }
    	m_numCols=1;
    }
    
    /** This method will allow you to set the value of the double array
     * @param d     The double[]
     */
    public void setDoubleArray(double[][] d) {
    	this.m_DoubleArray = d;
    	if (d.length>0) {
            m_numCols=d[0].length;
        }
    	else {
            m_numCols=1;
        }
    }
    
    /** 
     * @return the double array itself (no clone)
     */
    public double[][] getDoubleArrayShallow() {
    	return this.m_DoubleArray;
    }
    
    /**
     * Return a column as a vector (in copy)
     * @return a column as a vector (in copy)
     */
    public double[] getDoubleColumnAsVector(int col) {
    	if (col>=m_numCols) {
    		throw new IllegalArgumentException("Error, invalid column selected, " + col + " of " + m_numCols);
    	}
    	double[] ret = new double[m_DoubleArray.length];
    	for (int i=0; i<ret.length; i++) {
            ret[i]=m_DoubleArray[i][col];
        }
    	return ret;
    }
    
    public int getNumCols() {
    	return m_numCols;
    }
    
    public int getNumRows() {
    	return m_DoubleArray.length;
    }
    
    public double getValue(int i, int j) {
    	if (i<0 || j<0 || (i>=getNumRows()) || (j>=getNumCols())) {
    		throw new IllegalArgumentException("Error, invalid access to double array: " + i + "," + j + " within " + getNumRows() + ","+getNumCols());
    	}
    	return m_DoubleArray[i][j];
    }

	public void adaptRowCount(int k) {
		if (k!=m_DoubleArray.length) {
			double[][] newDD = new double[k][m_numCols];
			for (int i=0; i<k; i++) {
				for (int j=0; j<m_numCols; j++) {
					if (i<m_DoubleArray.length) {
                                        newDD[i][j]=m_DoubleArray[i][j];
                                    }
					else {
                                        newDD[i][j]=m_DoubleArray[m_DoubleArray.length-1][j];
                                    }
				}
			}
			setDoubleArray(newDD);
		}
	}
	
	public void deleteRow(int k) {
		if (k<0 || k>=getNumRows()) {
                throw new IllegalArgumentException("Invalid index to deleteRow: " + k + " is not a valid row.");
            }
		double[][] newDD = new double[getNumRows()-1][getNumCols()];
		int inc=0;
		for (int i = 0; i < newDD.length; i++) {
			if (i==k) {
                        inc=1;
                    }
			for (int j=0; j<getNumCols(); j++) {
                        newDD[i][j] = m_DoubleArray[i+inc][j];
                    }
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
		if (k<0 || k>= getNumRows()) {
                k=getNumRows()-1;
            }
		double[][] newDD = new double[getNumRows()+1][getNumCols()];

		for (int i = 0; i < getNumRows(); i++) {
                for (int j = 0; j<getNumCols(); j++) {
                    newDD[i][j] = m_DoubleArray[i][j];
                }
            }
		if (k>=0) {
                for (int j=0; j<getNumCols(); j++) {
      newDD[newDD.length-1][j] = newDD[k][j];
  }         }
		else {
                for (int j=0; j<getNumCols(); j++) {
           newDD[newDD.length-1][j] = 1.;
       }    } // if the array was empty
		setDoubleArray(newDD);
	}
	
	/**
	 * Normalize all columns of the array by dividing through the sum. 
	 */
	public void normalizeColumns() {
		double colSum=0;
		for (int j=0; j<getNumCols(); j++) {
			colSum=0;
			for (int i = 0; i < getNumRows(); i++) {
				colSum += m_DoubleArray[i][j];
			}
			if (colSum!=0) {
                        for (int i = 0; i < getNumRows(); i++) {
                 m_DoubleArray[i][j]/=colSum;
         }          }
		}
	}

	/**
	 * Check if k is a valid row index (within 0 and numRows-1).
	 * @param k
	 * @return
	 */
	public boolean isValidRow(int k) {
		return (k>=0) && (k<getNumRows());
	}
	
    @Override
	public String toString() {
		return BeanInspector.toString(m_DoubleArray);
	}
	
//    /** This method will allow you to set the value of the double array
//     * @param d     The double[]
//     */
//    public void setDoubleArray(double[] d) {
//        this.m_DoubleArray = new double[d.length][1];
//        for (int i=0; i<d.length; i++) m_DoubleArray[i][0] = d[i];
//    }
//
//    /** This method will return the complete name of the file
//     * which filepath
//     * @return The complete filename with path.
//     */
//    public double[] getDoubleArray() {
//        return this.m_DoubleArray;
//    }
}