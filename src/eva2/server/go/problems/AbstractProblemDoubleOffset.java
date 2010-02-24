package eva2.server.go.problems;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 24.03.2003
 * Time: 17:58:55
 * To change this template use Options | File Templates.
 */
public abstract class AbstractProblemDoubleOffset extends AbstractProblemDouble implements Interface2DBorderProblem {

//	protected AbstractEAIndividual      m_OverallBest       = null;
    protected int                       m_ProblemDimension  = 10;
    protected double                    m_XOffSet           = 0.0;
    protected double                    m_YOffSet           = 0.0;
//    protected boolean                   m_UseTestConstraint = false;

    public AbstractProblemDoubleOffset() {
    	super();
    	setDefaultRange(10);
    }
    
    public AbstractProblemDoubleOffset(AbstractProblemDoubleOffset b) {
    	super();
    	super.cloneObjects(b);
    	this.m_ProblemDimension = b.m_ProblemDimension;
    	this.m_XOffSet          = b.m_XOffSet;
    	this.m_YOffSet          = b.m_YOffSet;
//    	this.m_UseTestConstraint = b.m_UseTestConstraint;
    }
    
    public AbstractProblemDoubleOffset(int dim) {
    	this();
    	setProblemDimension(dim);
    }
    
    public AbstractProblemDoubleOffset(int dim, double defRange) {
    	this(dim);
    	setDefaultRange(defRange);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     * @return The name.
     */
    public String getName() {
        return "Double-valued-Problem+Offsets";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param XOffSet     The offset for the decision variables.
     */
    public void setXOffSet(double XOffSet) {
        this.m_XOffSet = XOffSet;
    }
    public double getXOffSet() {
        return this.m_XOffSet;
    }
    public String xOffSetTipText() {
        return "Choose an offset for the decision variable.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param YOffSet     The offset for the objective value.
     */
    public void setYOffSet(double YOffSet) {
        this.m_YOffSet = YOffSet;
    }
    public double getYOffSet() {
        return this.m_YOffSet;
    }
    public String yOffSetTipText() {
        return "Choose an offset for the objective value.";
    }
    /** Length of the x vector at is to be optimized
     * @param t Length of the x vector at is to be optimized
     */
    public void setProblemDimension(int t) {
        this.m_ProblemDimension = t;
    }
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the x vector to be optimized.";
    }
}

