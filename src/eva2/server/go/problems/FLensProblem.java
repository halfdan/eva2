package eva2.server.go.problems;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import eva2.server.go.GOStandaloneVersion;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.ESIndividualDoubleData;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.strategies.InterfaceOptimizer;
import eva2.server.modules.GOParameters;
import eva2.tools.math.RNG;

class MyLensViewer extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7945150208043416139L;
	private double[]                    m_BestVariables;
    private double                      m_BestFitness;
    private int                         m_Height, m_Width;
    FLensProblem                        m_LensProblem;

    public MyLensViewer (FLensProblem f) {
        this.m_LensProblem      = f;
        Dimension d = new Dimension (280, 220);
        this.setPreferredSize(d);
        this.setMinimumSize(d);
        resetBest();
    }

    public void resetBest() {
    	this.m_BestFitness = Double.POSITIVE_INFINITY;
    	this.m_BestVariables = new double[10];
    }
    
    public void paint(Graphics g) {
        Shape               tmpShape;
        BufferedImage       bufferedImage;
        BasicStroke         ds = new BasicStroke();
        Stroke              dashStroke;
        int                 mag = 10;
        int                 centerLens, centerScreen, segment;

//        lineStroke  = ds;
//        pointStroke = new BasicStroke(ds.getLineWidth(), ds.getEndCap(), ds.getLineJoin(), ds.getMiterLimit() , new float[] {1, 4}, 0);
        dashStroke  = new BasicStroke(ds.getLineWidth(), ds.getEndCap(), ds.getLineJoin(), ds.getMiterLimit() , new float[] {8, 8}, 0);

        super.paint(g);
        if (g == null) {
            System.out.println(" G == null!?");
            return;
        }
        // Create a buffered image in which to draw
//        try {
//            this.m_Height   = (int)g.getClipBounds().getHeight();
//            this.m_Width    = (int)g.getClipBounds().getWidth();
//            this.m_CenterX  = (int)g.getClipBounds().getCenterX();
//            this.m_CenterY  = (int)g.getClipBounds().getCenterY();
//        } catch (java.lang.NullPointerException npe) {
//            //System.out.println("Try fail...");
//        }
        // This might cure the eternal display problems: just ignore clipping and leave it up to swing
        Dimension winDim = getSize();
        m_Height = winDim.height;
        m_Width = winDim.width;
//        m_CenterX = m_Width/2;
//        m_CenterY = m_Height/2;
        
//        if (this.m_Height == 0) this.m_Height = 250;
//        if (this.m_Width == 0) this.m_Width = 350;
//        System.out.println(" h w cx cy "  + m_Height + " " + m_Width + " " + m_CenterX + " " + m_CenterY );
        bufferedImage = new BufferedImage(this.m_Width, this.m_Height, BufferedImage.TYPE_INT_RGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2D = bufferedImage.createGraphics();
        g2D.setPaint(Color.white);
        tmpShape = new Rectangle(0, 0, this.m_Width, this.m_Height);
        g2D.fill(tmpShape);

        // now start to plot some interesting stuff
        //draw the mid line
        g2D.setPaint(Color.black);
        g2D.drawLine(0, this.m_Height/2, this.m_Width, this.m_Height/2);
        centerLens      = 5 + 50;
        centerScreen    = centerLens + (int)this.m_LensProblem.m_FocalLength*10;
        segment         = 10*(int)this.m_LensProblem.m_Radius*2/(this.m_LensProblem.m_ProblemDimension-1);
        g2D.setStroke(dashStroke);
        g2D.drawLine(centerLens, this.m_Height/2+(int)this.m_LensProblem.m_Radius*10, centerLens, this.m_Height/2 -(int)this.m_LensProblem.m_Radius*10);
        g2D.drawLine(centerScreen, this.m_Height/2+(int)this.m_LensProblem.m_Radius*10+10, centerScreen, this.m_Height/2 -(int)this.m_LensProblem.m_Radius*10-10);
        g2D.setStroke(ds);
        // top and bottom line
        g2D.drawLine(centerLens-(int)(this.m_BestVariables[0]*mag), this.m_Height/2-(int)this.m_LensProblem.m_Radius*10, centerLens+(int)(this.m_BestVariables[0]*mag), this.m_Height/2 -(int)this.m_LensProblem.m_Radius*10);
        g2D.drawLine(centerLens-(int)(this.m_BestVariables[this.m_LensProblem.m_ProblemDimension-1]*mag), this.m_Height/2+(int)this.m_LensProblem.m_Radius*10, centerLens+(int)(this.m_BestVariables[this.m_LensProblem.m_ProblemDimension-1]*mag), this.m_Height/2 +(int)this.m_LensProblem.m_Radius*10);

        // plot the fitness result
        g2D.drawString("Fitness : "+ this.m_BestFitness, 5, 15);

        int currentXPos = this.m_Height/2-(int)this.m_LensProblem.m_Radius*10;
        double[] dots = this.m_LensProblem.testLens(this.m_BestVariables);
        for (int i = 1; i < this.m_BestVariables.length; i++) {
            ///System.out.println("X"+i+": " +  this.m_BestVariables[i]);
            // draw the line from the least one to the current on and use 10 as magnifier
            g2D.setPaint(Color.black);
            g2D.drawLine(centerLens-(int)(this.m_BestVariables[i-1]*mag), currentXPos, centerLens-(int)(this.m_BestVariables[i]*mag), currentXPos+segment);
            g2D.drawLine(centerLens+(int)(this.m_BestVariables[i-1]*mag), currentXPos, centerLens+(int)(this.m_BestVariables[i]*mag), currentXPos+segment);

            // paint the light rays
            g2D.setPaint(Color.red);
            g2D.drawLine(0, currentXPos + segment/2, centerLens, currentXPos + segment/2);
            g2D.drawLine(centerLens, currentXPos + segment/2, centerScreen, this.m_Height/2 +(int)(dots[i-1]*mag));

            currentXPos += segment;
//            tmpShape = new Rectangle(currentPos-width/2, this.m_Height/2, width, (int)(this.m_BestVariables[i]*10));
//            g2D.setPaint(Color.red);
//            g2D.fill(tmpShape);
//            g2D.setPaint(Color.black);
//            g2D.draw(tmpShape);
//            g2D.drawLine(currentPos, this.m_Height/2+5, currentPos, this.m_Height/2-5);
        }
        // Now put everything on the screen
        g.drawImage(bufferedImage, 0, 0, this);
    }

    /** This method updates the painted stuff
     * @param pop   The population to use
     */
    public void update(Population pop) {
        InterfaceDataTypeDouble best = (InterfaceDataTypeDouble)pop.getBestIndividual();
        //this.m_BestFitness      = ((AbstractEAIndividual)best).getFitness(0);
        double curFit = ((AbstractEAIndividual)best).getFitness(0);
        if (m_BestFitness > curFit) {
            this.m_BestVariables    = best.getDoubleData();
        	this.m_BestFitness 		= curFit;
        	this.paint(this.getGraphics());
        }
    }

}
/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.07.2004
 * Time: 09:49:37
 * To change this template use File | Settings | File Templates.
 */
public class FLensProblem extends AbstractOptimizationProblem implements InterfaceOptimizationProblem, java.io.Serializable  {

	/**
	 *  
	 */
	private static final long serialVersionUID = 4694920294291719310L;
	protected AbstractEAIndividual      m_OverallBest       = null;
	protected int                       m_ProblemDimension  = 10;
	protected double                    m_Noise             = 0.0;
	protected double                    m_XOffSet           = 0.0;
	protected double                    m_YOffSet           = 0.0;
    transient protected boolean         m_Show              = false;
    //protected int						sleepTime			= 0;
    
    transient private JFrame            m_ProblemFrame;
    transient private MyLensViewer      m_Panel;
    public double                       m_Radius            = 5;
    public double                       m_FocalLength       = 20;
    public double                       m_Epsilon           = 1.5;
    private boolean                     m_UseMaterialConst  = false;

	public FLensProblem() {
        this.m_Template         = new ESIndividualDoubleData();
        if (this.m_Show) this.initProblemFrame();
	}
	public FLensProblem(FLensProblem b) {
        //AbstractOptimizationProblem
        if (b.m_Template != null)
            this.m_Template         = (AbstractEAIndividual)((AbstractEAIndividual)b.m_Template).clone();
        //FLensProblem
        if (b.m_OverallBest != null)
            this.m_OverallBest      = (AbstractEAIndividual)((AbstractEAIndividual)b.m_OverallBest).clone();
        this.m_ProblemDimension = b.m_ProblemDimension;
        this.m_Noise            = b.m_Noise;
        this.m_XOffSet          = b.m_XOffSet;
        this.m_YOffSet          = b.m_YOffSet;
        this.m_Radius           = b.m_Radius;
        this.m_FocalLength      = b.m_FocalLength;
        this.m_Epsilon          = b.m_Epsilon;
        this.m_UseMaterialConst = b.m_UseMaterialConst;
	}
    public Object clone() {
        return (Object) new FLensProblem(this);
    }

    /** This method inits a problem view frame
     *
     */
    public void initProblemFrame() {
        if (this.m_ProblemFrame == null) {
            this.m_ProblemFrame = new JFrame("Lens Problem Viewer");
            this.m_Panel        = new MyLensViewer(this);
            this.m_ProblemFrame.getContentPane().add(this.m_Panel);
            this.m_ProblemFrame.pack();
            this.m_ProblemFrame.setVisible(true);
            //this.m_ProblemFrame.show();
        } else this.m_Panel.resetBest();
    }

    /** This method gets rid of the problem view frame
     *
     */
    public void disposeProblemFrame() {
        if (this.m_ProblemFrame != null) this.m_ProblemFrame.dispose();
        this.m_ProblemFrame = null;
        this.m_Panel = null;
    }

    /** This method update the content in the current problem
     * view frame depending on the current population.
     * @param population    The current population.
     */
    public void updateProblemFrame(Population population) {
        if (this.m_Panel != null) this.m_Panel.update(population);
    }

	/** This method inits the Problem to log multiruns
	 */
	public void initProblem() {
		this.m_OverallBest = null;
        if (this.m_Show) this.initProblemFrame();
	}

	/** This method inits a given population
	 * @param population    The populations that is to be inited
	 */
	public void initPopulation(Population population) {
		this.m_OverallBest = null;
		((InterfaceDataTypeDouble)this.m_Template).setDoubleDataLength(this.m_ProblemDimension);
        // set the range
        double[][] range = new double[this.m_ProblemDimension][2];
        for (int i = 0; i < range.length; i++) {
            range[i][0] = 0.1;
            range[i][1] = 5.0;
        }
       ((InterfaceDataTypeDouble)this.m_Template).SetDoubleRange(range);

		AbstractOptimizationProblem.defaultInitPopulation(population, m_Template, this);
        if (this.m_Show) this.initProblemFrame();
	}

	public void evaluatePopulationEnd(Population pop) {
		if (this.m_Show) this.updateProblemFrame(pop);
	}

	/** This method evaluate a single individual and sets the fitness values
	 * @param individual    The individual that is to be evalutated
	 */
	public void evaluate(AbstractEAIndividual individual) {
		double[]            x;
		double[]            fitness;

		x = new double[((InterfaceDataTypeDouble) individual).getDoubleData().length];
		System.arraycopy(((InterfaceDataTypeDouble) individual).getDoubleData(), 0, x, 0, x.length);
		for (int i = 0; i < x.length; i++) x[i] = x[i] - this.m_XOffSet;
		fitness = this.doEvaluation(x);
		for (int i = 0; i < fitness.length; i++) {
			// add noise to the fitness
			fitness[i] += RNG.gaussianDouble(this.m_Noise);
			fitness[i] += this.m_YOffSet;
			// set the fitness of the individual
			individual.SetFitness(i, fitness[i]);
		}
		if ((this.m_OverallBest == null) || (this.m_OverallBest.getFitness(0) > individual.getFitness(0))) {
			this.m_OverallBest = (AbstractEAIndividual)individual.clone();
		}
	}

	/** Ths method allows you to evaluate a simple bit string to determine the fitness
	 * @param x     The n-dimensional input vector
	 * @return  The m-dimensional output vector.
	 */
	public double[] doEvaluation(double[] x) {
        double fitness          = 0;
        double[] ret            = new double[1];

        // set a minimum value for the thickness of the lens
        for (int i = 0; i < x.length; i++) if (x[i] < 0.1) x[i] = 0.1;

        double[] tmpFit = this.testLens(x);
        for (int i = 0; i < tmpFit.length; i++) fitness += Math.pow(tmpFit[i], 2);

//        // Computation of fitness. Uses an approximation for very thin lenses.
//        // The fitness is the sum over all segments of the deviation from the center
//        // of focus of a beam running through a segment.
//         for (int i = 1; i < x.length; i++)
//            fitness = fitness + Math.pow(m_Radius - m_SegmentHight / 2 - m_SegmentHight * (i - 1) -  m_FocalLength / m_SegmentHight * (m_Epsilon - 1) * (x[i] - x[i-1]),2);

        // Here the thickness of the middle segment of the lens	is added to the fitness
        // to permit the optimization to reduce the overall thickness of the lens
        if (this.m_UseMaterialConst) fitness = fitness + x[(int)(x.length/2)];

        ret[0] = fitness;
        return ret;
	}

    /** this method will return the deviations
     * @param x     The lens
     * @return double[]
     */
    public double[] testLens(double[] x) {
        double      m_SegmentHight      = 2 * m_Radius / (x.length - 1);
        double[]    result              = new double[x.length-1];
        // Computation of fitness. Uses an approximation for very thin lenses.
        // The fitness is the sum over all segments of the deviation from the center
        // of focus of a beam running through a segment.
        for (int i = 1; i < x.length; i++)
           result[i-1] = m_Radius - m_SegmentHight / 2 - m_SegmentHight * (i - 1) -  m_FocalLength / m_SegmentHight * (m_Epsilon - 1) * (x[i] - x[i-1]);
        return result;
    }

	/** This method allows you to output a string that describes a found solution
	 * in a way that is most suiteable for a given problem.
	 * @param individual    The individual that is to be shown.
	 * @return The description.
	 */
	public String getSolutionRepresentationFor(AbstractEAIndividual individual) {
		this.evaluate(individual);
		String result = "FX problem:\n";
		result += individual.getStringRepresentation() + "\n";
		result += "Y = " + individual.getFitness(0);
		return result;
	}

	/** This method returns the header for the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringHeader(Population pop) {
		return "Solution";
	}

	/** This method returns the additional data that is to be written into a file
	 * @param pop   The population that is to be refined.
	 * @return String
	 */
	public String getAdditionalFileStringValue(Population pop) {
		String result ="{";
		double[] data = ((InterfaceDataTypeDouble) pop.getBestEAIndividual()).getDoubleData();
		for (int i = 0; i < data.length; i++) result += data[i] +"; ";
		result += "}";
		return result;
	}

	/** This method allows you to output a string that describes a found solution
	 * in a way that is most suiteable for a given problem.
	 * @param optimizer        The individual that is to be shown.
	 * @return The description.
	 */
	public String getFinalReportOn(InterfaceOptimizer optimizer){
		String result = optimizer.getStringRepresentation() +"\n";
		result += this.getSolutionRepresentationFor(this.m_OverallBest);
		return result;
	}

    /** This method returns a string describing the optimization problem.
     * @param opt       The Optimizer that is used or had been used.
     * @return The description.
     */
    public String getStringRepresentationForProblem(InterfaceOptimizer opt) {
		String result = "";

		result += "FX Problem:\n";
		result += "Here the individual codes a vector of real number x and FX(x)= x is to be minimized.\n";
		result += "Parameters:\n";
		result += "Dimension   : " + this.m_ProblemDimension +"\n";
		result += "Noise level : " + this.m_Noise + "\n";
		result += "Solution representation:\n";
		//result += this.m_Template.getSolutionRepresentationFor();
		return result;
	}

	/** This method allows you to request a graphical representation for a given
	 * individual.
	 */
	public JComponent drawIndividual(AbstractEAIndividual indy) {
		JTextArea tindy = new JTextArea(indy.getStringRepresentation());
		JScrollPane pindy = new JScrollPane(tindy);
		tindy.setEditable(false);
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(tindy, BorderLayout.NORTH);
        result.add(pindy, BorderLayout.CENTER);
        return result;
	}

    public static void main(String[] args) {
        System.out.println("TEST");
        FLensProblem f = new FLensProblem();
        System.out.println("Working Dir " +System.getProperty("user.dir"));
		GOStandaloneVersion  program = new GOStandaloneVersion();
        GOParameters GO = program.getGOParameters();
        GO.setProblem(f);
		RNG.setRandomSeed(1);
		program.initFrame();
		program.setShow(true);
    }

/**********************************************************************************************************************
 * These are for GUI
 */
	/** This method allows the CommonJavaObjectEditorPanel to read the
	 * name to the current object.
	 * @return The name.
	 */
	public String getName() {
		return "Lens Problem";
	}

	/** This method returns a global info string
	 * @return description
	 */
	public String globalInfo() {
		return "Focussing of a lens is to be optimized.";
	}

	/** This method allows you to choose how much noise is to be added to the
	 * fitness. This can be used to make the optimization problem more difficult.
	 * @param noise     The sigma for a gaussian random number.
	 */
	public void setNoise(double noise) {
		if (noise < 0) noise = 0;
		this.m_Noise = noise;
	}
	public double getNoise() {
		return this.m_Noise;
	}
	public String noiseTipText() {
		return "Noise level on the fitness value.";
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
	/** This method allows you to set the number of mulitruns that are to be performed,
	 * necessary for stochastic optimizers to ensure reliable results.
	 * @param multiruns The number of multiruns that are to be performed
	 */
	public void setProblemDimension(int multiruns) {
		this.m_ProblemDimension = multiruns;
	}
	public int getProblemDimension() {
		return this.m_ProblemDimension;
	}
	public String problemDimensionTipText() {
		return "Length of the x vector at is to be optimized.";
	}
	/** This method allows you to toggel the solution representation.
	 * @param show  Whether to show the result or not
	 */
	public void setShow(boolean show) {
		this.m_Show = show;
        if (this.m_Show) this.initProblemFrame();
        else this.disposeProblemFrame();
	}
	public boolean getShow() {
		return this.m_Show;
	}
	public String showTipText() {
		return "Toggel the visualization of the solution.";
	}

	/** This method allows you to toggel the use of the material constraint.
	 * @param show  Whether to show the result or not
	 */
	public void setUseMaterialConst(boolean show) {
		this.m_UseMaterialConst = show;
	}
	public boolean getUseMaterialConst() {
		return this.m_UseMaterialConst;
	}
	public String useMaterialConstTipText() {
		return "Toggel the use of the material constraint.";
	}

    /** This method allows you to choose the EA individual
     * @param indy The EAIndividual type
     */
    public void setEAIndividual(InterfaceDataTypeDouble indy) {
        this.m_Template = (AbstractEAIndividual)indy;
    }
    public InterfaceDataTypeDouble getEAIndividual() {
        return (InterfaceDataTypeDouble)this.m_Template;
    }

	/** This method allows you to set the EA individual type
	 * Beware: Trap!
	 * @param indy  The new EA individual type.
	 */
	public void setEAIndividualTrap(AbstractEAIndividual indy) {
		if (indy instanceof InterfaceDataTypeDouble) this.setEAIndividual((InterfaceDataTypeDouble)indy);
	}
}