package eva2.problems;

import eva2.util.annotation.Parameter;

/**
 *
 */
public abstract class AbstractProblemDoubleOffset extends AbstractProblemDouble implements Interface2DBorderProblem {

    protected double xOffset = 0.0; // TODO make them private, implement evaluate() and create abstract evalWithoutOffsets
    protected double yOffset = 0.0;

    public AbstractProblemDoubleOffset() {
        super();
        setDefaultRange(10);
    }

    public AbstractProblemDoubleOffset(AbstractProblemDoubleOffset b) {
        super();
        super.cloneObjects(b);
        this.problemDimension = b.problemDimension;
        this.xOffset = b.xOffset;
        this.yOffset = b.yOffset;
    }

    public AbstractProblemDoubleOffset(int dim) {
        this();
        setProblemDimension(dim);
    }

    public AbstractProblemDoubleOffset(int dim, double defRange) {
        this(dim);
        setDefaultRange(defRange);
    }


    /**
     * This method allows the CommonJavaObjectEditorPanel to read the
     * name to the current object.
     *
     * @return The name.
     */
    @Override
    public String getName() {
        return "Double-valued-Problem+Offsets";
    }

    /**
     * This method allows you to set/get an offset for decision variables.
     *
     * @param XOffset The offset for the decision variables.
     */
    @Parameter(description = "Choose an offset for the decision variables.")
    public void setXOffset(double XOffset) {
        this.xOffset = XOffset;
    }

    public double getXOffset() {
        return this.xOffset;
    }

    /**
     * This method allows you to set/get the offset for the
     * objective value.
     *
     * @param YOffset The offset for the objective value.
     */
    @Parameter(description = "Choose an offset for the objective value.")
    public void setYOffset(double YOffset) {
        this.yOffset = YOffset;
    }

    public double getYOffset() {
        return this.yOffset;
    }
}

