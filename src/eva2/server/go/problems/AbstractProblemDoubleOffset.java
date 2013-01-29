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
    protected double                    m_XOffset           = 0.0; // TODO make them private, implement eval() and create abstract evalWithoutOffsets
    protected double                    m_YOffset           = 0.0;
//    protected boolean                   m_UseTestConstraint = false;

    public AbstractProblemDoubleOffset() {
    	super();
    	setDefaultRange(10);
    }
    
    public AbstractProblemDoubleOffset(AbstractProblemDoubleOffset b) {
    	super();
    	super.cloneObjects(b);
    	this.m_ProblemDimension = b.m_ProblemDimension;
    	this.m_XOffset          = b.m_XOffset;
    	this.m_YOffset          = b.m_YOffset;
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
    @Override
    public String getName() {
        return "Double-valued-Problem+Offsets";
    }

    /** This method allows you to set/get an offset for decision variables.
     * @param XOffset     The offset for the decision variables.
     */
    public void setXOffset(double XOffset) {
        this.m_XOffset = XOffset;
    }
    public double getXOffset() {
        return this.m_XOffset;
    }
    public String XOffsetTipText() {
        return "Choose an offset for the decision variables.";
    }

    /** This method allows you to set/get the offset for the
     * objective value.
     * @param YOffset     The offset for the objective value.
     */
    public void setYOffset(double YOffset) {
        this.m_YOffset = YOffset;
    }
    public double getYOffset() {
        return this.m_YOffset;
    }
    public String YOffsetTipText() {
        return "Choose an offset for the objective value.";
    }
    /** Length of the x vector at is to be optimized
     * @param t Length of the x vector at is to be optimized
     */
    public void setProblemDimension(int t) {
        this.m_ProblemDimension = t;
    }
    @Override
    public int getProblemDimension() {
        return this.m_ProblemDimension;
    }
    public String problemDimensionTipText() {
        return "Length of the x vector to be optimized.";
    }
}

