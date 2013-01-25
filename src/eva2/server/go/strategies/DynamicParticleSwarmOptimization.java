package eva2.server.go.strategies;

import eva2.gui.GenericObjectEditor;
import eva2.gui.Plot;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.SelectedTag;
import eva2.tools.math.Mathematics;
import eva2.tools.math.RNG;

/** 
 * This extends our particle swarm implementation to dynamic optimization problems.
 * 
 * User: marcekro
 * Date: 2007
 * Time: 11:23:21
 */
public class DynamicParticleSwarmOptimization extends ParticleSwarmOptimization {

    private boolean	envHasChanged	= false;
//    private boolean useCurrentFit	= true;
    /**
     * switch for the speed adaptation mechanism
     */
    protected boolean 	doSpeedAdaptation = false;
    private double 	phi0				= 0.005;
    private double	phi3				= 0.0;
    private double 	highEnergyRaise = 2.;
    private double 	highEnergyRatio = .2;
    private double	quantumRatio	= 0.1;
    private double	quantumCloudDia = 0.2;
    // the detectAnchor may be an Individual ID which will be ommitted by the update to check for env. changes.
    private int		detectAnchor = 0;
    private double[]	detectFit = null;
    /** 
     * the change detection strategy
     */
    protected SelectedTag		changeDetectStrategy;
    
	private double maxSpeedLimit = 0.1;
	private double minSpeedLimit = .003;
	
	private boolean plotBestOnly = false;
	private transient double[] lastBestPlot = null;
	
	/**
	 * constant indication quantum particles
	 */
	public static final int quantumType = 1;

	/**
	 * A standard constructor.
	 *
	 */
    public DynamicParticleSwarmOptimization() {
        super();
        this.changeDetectStrategy = new SelectedTag("Random Anchor", "Assume change", "Assume no change");
    }

    /**
     * The copy constructor.
     * 
     * @param a another DynPSO object
     */
    public DynamicParticleSwarmOptimization(DynamicParticleSwarmOptimization a) {
    	super(a);
    	envHasChanged		= a.envHasChanged;
    	doSpeedAdaptation 	= a.doSpeedAdaptation;
    	phi0				= a.phi0;
    	highEnergyRaise 	= a.highEnergyRaise;
    	highEnergyRatio 	= a.highEnergyRatio;
    	quantumRatio		= a.quantumRatio;
    	quantumCloudDia 	= a.quantumCloudDia;
    	detectAnchor 		= a.detectAnchor;
    	detectFit 			= a.detectFit;
    	maxSpeedLimit 		= a.maxSpeedLimit;
    	minSpeedLimit 		= a.minSpeedLimit;
    	changeDetectStrategy.setSelectedAs(a.changeDetectStrategy);
    }

    public Object clone() {
        return (Object) new DynamicParticleSwarmOptimization(this);
    }

    /**
     * Call all methods that may hide anything, cf. PSO
     */
    public void hideHideable() {
    	super.hideHideable();
    	setQuantumRatio(quantumRatio);
    	setDoSpeedAdaptation(doSpeedAdaptation);
    	setHighEnergyRatio(highEnergyRatio);
    }
    
    /**
     * Adapts the swarm speed limit to give room to the favourite tracking speed. This is currently
     * done by approaching a speed limit value at two times the EMA speed.
     *
     * @param range
     */
    public void adaptTrackingSpeed(double[][] range) {
		double incFact = 1.1;
		double decFact = .97;
		double upperChgLim = .6;
		double lowerChgLim = .4;
		double normEmaSpd = getRelativeEMASpeed(range);
		double spdLim = getSpeedLimit();
		
		if (normEmaSpd > (upperChgLim*spdLim)) setSpeedLimit(Math.min(maxSpeedLimit, incFact * spdLim));
		else if (normEmaSpd < (lowerChgLim*spdLim)) setSpeedLimit(Math.max(minSpeedLimit, decFact*spdLim));
    }
    
    /**
     * Returns the favourite speed-limit for the current average speed, which currently is
     * twice the EMA speed. Range is required to calculate the speed relative to the playground size. 
     *
     * @param range		range values for the optimization problem
     * @return	the favourite speed limit.
     */
    public double getFavTrackingSpeed(double[][] range) {
    	return 2*Mathematics.getRelativeLength(getEMASpeed(), range);
    }

    /** 
     * This method will update a quantum individual.
     * 
     * @param index      The individual to update.
     * @param pop       The current population.
     * @param best      The best individual found so far.
     */
    private void updateQuantumIndividual(int index, AbstractEAIndividual indy, Population pop) {
    	InterfaceDataTypeDouble endy = (InterfaceDataTypeDouble) indy;
        // search for the local best position
        
        double[]       localBestPosition;
        double[] position   = endy.getDoubleData();
        
        localBestPosition = findNeighbourhoodOptimum(index, pop);

        double[]	newPos = new double[position.length];
        double[][]	range = endy.getDoubleRange();
        
        System.arraycopy(localBestPosition, 0, newPos, 0, newPos.length);

        double[] rand = getNormalRandVect(position.length, range, quantumCloudDia);
        //double[] rand = getUniformRandVect(position.length, range);
            
            Mathematics.vvAdd(newPos, rand, newPos);
        	if (m_CheckRange) {
        		Mathematics.projectToRange(newPos, range);
        	}
        	
            if (indy instanceof InterfaceDataTypeDouble) ((InterfaceDataTypeDouble)indy).SetDoubleGenotype(newPos);
            else endy.SetDoubleGenotype(newPos); 
            
            resetFitness(indy);
            
            plotIndy(position, null, (Integer)(indy.getData(indexKey)));
//            if (this.m_Show) {
//                this.m_Plot.setUnconnectedPoint(position[0], position[1], (Integer)(indy.getData(indexKey)));
                //this.m_Plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index+1);
//        }
        //System.out.println("quantum particle " + index + " set to " + newPos[0] + "/" + newPos[1]);
    }
    
    private void plotBestIndy() {
    	if (m_Plot != null) {
    		double[] curPosition = ((InterfaceDataTypeDouble)m_Population.getBestEAIndividual()).getDoubleData();
    	
    		if (lastBestPlot != null) this.m_Plot.setConnectedPoint(lastBestPlot[0], lastBestPlot[1], 0);
    		this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], 0);
    		lastBestPlot = curPosition.clone();
    	}
    }
    
	protected void plotIndy(double[] curPosition, double[] curVelocity, int index) {
		if (this.m_Show) {
			if (plotBestOnly) {
				if (index != ((Integer)(m_Population.getBestEAIndividual().getData(indexKey)))) return;
				else {
//					if (lastBestPlot != null) this.m_Plot.setConnectedPoint(lastBestPlot[0], lastBestPlot[1], index);
//					this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index);
					this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
					lastBestPlot = curPosition.clone();
				}
			} else {
				if (curVelocity == null) {
			
					this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
				} else {
					this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index);
					this.m_Plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index);
				}
			}
		}
	}

    /**
     * Return a uniformly distributed position vector within a sphere of radius r in relation to the given range.
     *
     * @param vlen	vector length
     * @param range	range of the playground in any axis
     * @param r		radius of the sphere relative to the range
     * @return	a uniformly distributed vector within a sphere of radius r in relation to the given range
     */
	protected double[] getUniformRandVect(int vlen, double[][] range, double r) {
		double normfact = 0.;
		double[] rand = new double[vlen];
		for (int i=0; i<rand.length; i++) {
			rand[i] = RNG.gaussianDouble(1);
			normfact += (rand[i]*rand[i]);
		}
		normfact = Math.sqrt(normfact); // for normalization
		
		// normalize and scale with range 
		for (int i=0; i<rand.length; i++) {
			// leaving out the sqrt(rand) part would result in a point on the sphere (muller, marsaglia)
			rand[i] = rand[i] / normfact * Math.sqrt(RNG.randomDouble()) * r * (range[i][1]-range[i][0]);
		}
		return rand;
	}
	
    /**
     * Return a normally distributed position vector around zero with the given standard deviation in relation to the given range.
     *
     * @param vlen	vector length
     * @param range	range of the playground in any axis
     * @param stddev	standard deviation for the random vector
     * @return a normally distributed position vector around zero with the given standard deviation in relation to the given range.
     */
	protected double[] getNormalRandVect(int vlen, double[][] range, double stddev) {
		double[] rand = new double[vlen];
		for (int i=0; i<rand.length; i++) {
			rand[i] = RNG.gaussianDouble(stddev * (range[i][1]-range[i][0]));
		}
		return rand;
	}
	
    /** This method will update a given individual
     * according to the PSO method
     * @param index      The individual to update.
     * @param pop       The current population.
     * @param best      The best individual found so far.
     */
//    protected void updateIndividual(int index, Population pop, AbstractEAIndividual best) {
//        AbstractEAIndividual indy = (AbstractEAIndividual)pop.get(index);
//        if (indy instanceof InterfaceESIndividual) {
//            InterfaceESIndividual endy = (InterfaceESIndividual) indy;
//            
//        	if (isParticleType(indy, resetType)) {
//				resetIndividual(indy);
//        	} else {	            
//	            indy.SetData(partTypeKey, defaultType);	// reset in case it was quantum
//	            
//	            double[] personalBestPos   = (double[]) indy.getData(partBestPosKey);
//	            double[] velocity       = (double[]) indy.getData(partVelKey);
//	            double[] curPosition    = endy.getDGenotype();
//	            double[][] range = endy.getDoubleRange();
//	            
//	            // search for the local best position
//	            double[]    neighbourBestPos = findNeighbourhoodOptimum(index, pop, best);
//           
//	            // now update the velocity
//	            double[] curVelocity = updateVelocity(index, velocity, personalBestPos, curPosition, neighbourBestPos, range);
//	
//	            //System.out.println("localBestPos is " + localBestPosition[0] + "/" + localBestPosition[1]);
//	            
//	            // check the speed limit!
//	            if (checkSpeedLimit) enforceSpeedLimit(curVelocity, range, getSpeedLimit(index, pop.size()));
//	
//	            // enforce range constraints if necessary
//	            if (m_CheckConstraints) ensureConstraints(curPosition, curVelocity, range); 
//	            
//	            plotIndy(curPosition, curVelocity, (Integer)indy.getData(indexKey));
//	            // finally update the position
//	            updatePosition(indy, curVelocity, curPosition, range); 
//	            
//	            resetFitness(indy);
//        	}
//        } else {
//            System.err.println("Could not perform PSO update, because individual is not instance of InterfaceESIndividual!");
//        }
//    }

	@Override
	protected double[] updateVelocity(int index, double[] lastVelocity, double[] bestPosition, double[] curPosition, double[] localBestPos, double[][] range) {
		if (envHasChanged) {
			double chi;
			double[] curVelocity    = new double[lastVelocity.length];
			/* old ways
			    curVelocity[i]  = this.m_Inertness * velocity[i];

                curVelocity[i]  += (this.m_Phi1 * getSpeedLimit(index) * (range[i][1] - range[i][0]) * RNG.randomDouble(-1., 1.));
                // the component from the social model
                curVelocity[i]  += this.m_Phi2*RNG.randomDouble(0,1)*(localBestPos[i]-curPosition[i]);
 
			*/
	        for (int i = 0; i < lastVelocity.length; i++) {
	            // the component from the old velocity
	            curVelocity[i]  = this.m_InertnessOrChi * lastVelocity[i];
			    if (algType.getSelectedTag().getID()==1) chi=m_InertnessOrChi;
			    else chi = 1.;
			    // random perturbation
	            //curVelocity[i]  += (this.phi0 * chi * RNG.randomDouble(-1., 1.) * (range[i][1] - range[i][0]));
	            curVelocity[i]  += (this.phi0 * chi * getSpeedLimit(index) * RNG.randomDouble(-1., 1.) * (range[i][1] - range[i][0]));
	            // a problem specific attractor
            	curVelocity[i]  += getProblemSpecificAttraction(i, chi); 
	            // the component from the social model
	            curVelocity[i]  += (getIndySocialModifier(index, chi) * (localBestPos[i]-curPosition[i]));
	        }
			return curVelocity;
		} else return super.updateVelocity(index, lastVelocity, bestPosition, curPosition, localBestPos, range);
	}
	
	protected double getProblemSpecificAttraction(int i, double chi) {
//		if (m_Problem instanceof DynLocalizationProblem) {
//			// TODO test this!
//			//hier weiter
//			double[] att = ((DynLocalizationProblem)m_Problem).getProblemSpecificAttractor();
//			return (this.phi3 * chi * RNG.randomDouble(0, 1.))*att[i];
//		} else 
			return 0;
	}

	/**
	 * Get a modifier for the velocity update, social component. A random double in (0,1) is standard.
	 *
	 * @param index		index of the concerned individual
	 * @param chiVal	current chi value
	 * @return a double value
	 */
	protected double getIndySocialModifier(int index, double chiVal) {
		return (this.m_Phi2 * chiVal * RNG.randomDouble(0,1));
//		if (index < 50) return (this.m_Phi2 * chi * RNG.randomDouble(0,1));
//		else return (this.m_Phi2 * chi * (-1.) * RNG.randomDouble(0,1));
	}
	
    /**
     * Get the speed limit of an individual (respecting highEnergyRatio, so some individuals may be accelerated).
     *
     * @param index	the individuals index
     * @param popSize	the size of the population
     * @return the speed limit of the individual
     */
    protected double getSpeedLimit(int index) {
    	if (index >= ((double)(m_Population.size() * highEnergyRatio))) return m_SpeedLimit;
    	else {
    		if (highEnergyRaise == 0.) return maxSpeedLimit; 
    		else return m_SpeedLimit * highEnergyRaise; 
    	}
    }
    
    /////////////////////////////////////////// these are called from the optimize loop
    protected void startOptimize() {
    	super.startOptimize();
    	if (detectAnchor >= 0) {	// set the new detection anchor individual
    		detectAnchor = RNG.randomInt(0, m_Population.size()-1);
    		if (detectFit == null) detectFit = (m_Population.getIndividual(detectAnchor).getFitness()).clone();
    		else System.arraycopy(m_Population.getIndividual(detectAnchor).getFitness(), 0, detectFit, 0, detectFit.length);
    	}
    }
    
	/** This method will init the optimizer with a given population
	 * @param pop       The initial population
	 * @param reset     If true the population is reset.
	 */
	public void initByPopulation(Population pop, boolean reset) {
		super.initByPopulation(pop, reset);
		double quantumCount = 0.;
		// do what the usual function does plus announce quantum particles
		if (quantumRatio > 0.) {
			for (int i = 0; i < this.m_Population.size(); i++) {
				AbstractEAIndividual indy = (AbstractEAIndividual)m_Population.get(i);
		    	if (i>=quantumCount) {
		    		indy.putData(partTypeKey, quantumType);
		    		quantumCount += 1./quantumRatio;
		    	}
			}
		}
	}
    
	/** This method will update a given individual
	 * according to the PSO method
	 * @param index      The individual to update.
	 * @param pop       The current population.
	 * @param best      The best individual found so far.
	 */
	protected void updateIndividual(int index, AbstractEAIndividual indy, Population pop) {
		if (index != detectAnchor) { // only for non anchor individuals (its -1 if other detect strategy is used)
			if (indy instanceof InterfaceDataTypeDouble) {
				int type=(Integer)indy.getData(partTypeKey);
				switch (type) {
				case quantumType:
					updateQuantumIndividual(index, indy, pop);
					break;
				default:	// handles the standard way of updating a particle
					super.updateIndividual(index, indy, pop);
				break;
				}
			} else {
				throw new RuntimeException("Could not perform PSO update, because individual is not instance of InterfaceESIndividual!");
			}
		}
		//if (AbstractEAIndividual.getDoublePosition(indy)[0]<500000) {
		//	System.err.println(indy.getStringRepresentation());
		//}
	}

    /** This method will evaluate the current population using the
     * given problem.
     * @param population The population that is to be evaluated
     */
	protected void evaluatePopulation(Population population) {
    	envHasChanged = false;
        super.evaluatePopulation(population);
	    if (m_Show && plotBestOnly) plotBestIndy();
	    
	    envHasChanged = detectChange(m_Population);
	    
//	    if (envHasChanged) {
//	    	System.out.println("environmental change detected!");
//	    }

	    if (doSpeedAdaptation) {
    		adaptTrackingSpeed(((InterfaceDataTypeDouble)population.get(0)).getDoubleRange());
    	}
//        if (m_Problem instanceof DynLocalizationProblem) {
//        	((DynLocalizationProblem)m_Problem).adaptPSOByPopulation(population, this);
//        }
    }
    
	protected boolean isIndividualToUpdate(AbstractEAIndividual indy) {
		return (envHasChanged || super.isIndividualToUpdate(indy));
	}

	protected void logBestIndividual() { 
	    // log the best individual of the population
	    if (envHasChanged || (this.m_Population.getBestEAIndividual().isDominatingDebConstraints(this.m_BestIndividual))) {
	        this.m_BestIndividual = (AbstractEAIndividual)this.m_Population.getBestEAIndividual().clone();
	        this.m_BestIndividual.putData(partBestFitKey, this.m_BestIndividual.getFitness());
	        this.m_BestIndividual.putData(partBestPosKey, ((InterfaceDataTypeDouble)this.m_BestIndividual).getDoubleData());
	        //System.out.println("-- best ind set to " + ((InterfaceDataTypeDouble)this.m_BestIndividual).getDoubleData()[0] + "/" + ((InterfaceDataTypeDouble)this.m_BestIndividual).getDoubleData()[1]);
	    }
    }
    
    /////////////////////////////// end optimize loop

    /**
     * Checks for env change depending on the detection strategy.
     * For anchor detection, if detectAnchor is a valid ID, it returns true if the anchor individuals fitness has changed.
     * For assume change, true is returned, for assume no change, false is returned.
     * 
     * @param population	the population to check for a change
     * @return true if the population has changed as to the detect strategy, else false
     */
    protected boolean detectChange(Population population) {
    	switch (changeDetectStrategy.getSelectedTag().getID()) {
    	case 0:
        	if (detectAnchor >= 0) return !(java.util.Arrays.equals(detectFit, m_Population.getIndividual(detectAnchor).getFitness()));
        	else {
        		System.err.println("warning, inconsistency in detectChange");
        	}
        	break;
    	case 1:
    		return true;
    	case 2:
    		return false;
    	}
    	System.err.println("warning, inconsistency in detectChange");
    	return false;
	}

    @Override
	public void setPopulation(Population pop) {
		super.setPopulation(pop);
		if (detectAnchor>=pop.size()) detectAnchor=0;
	}

	public void init() {
    	super.init();
    	setEmaPeriods(15);
    	if (doSpeedAdaptation) setSpeedLimit(2*getInitialVelocity());
    }

	
    public void setProblem (InterfaceOptimizationProblem problem) {
    	super.setProblem(problem);
    	if (problem instanceof AbstractOptimizationProblem) {
    		((AbstractOptimizationProblem)problem).informAboutOptimizer(this);
    	}
    }
    
    /** This method will return a string describing all properties of the optimizer
     * and the applied methods.
     * @return A descriptive string
     */
    public String getStringRepresentation() {
    	StringBuilder strB = new StringBuilder(200);
    	strB.append("Dynamic Particle Swarm Optimization:\nOptimization Problem: ");
        strB.append(this.m_Problem.getStringRepresentationForProblem(this));
        strB.append("\n");
        strB.append(this.m_Population.getStringRepresentation());
        return strB.toString();
    }


/**********************************************************************************************************************
 * These are for GUI
 */
    /** This method returns a global info string
     * @return description
     */
    public static String globalInfo() {
        return "Particle Swarm Optimization tuned for tracking a dynamic target";
    }
    /** This method will return a naming String
     * @return The name of the algorithm
     */
    public String getName() {
        return "DynPSO";
    }

	/**
	 * @return the quantumCloudDia
	 **/
	public double getQuantumCloudDia() {
		return quantumCloudDia;
	}

	/**
	 * @param quantumCloudDia the quantumCloudDia to set
	 **/
	public void setQuantumCloudDia(double quantumCloudDia) {
		this.quantumCloudDia = quantumCloudDia;
	}

	/**
	 * @return the quantumRatio
	 **/
	public double getQuantumRatio() {
		return quantumRatio;
	}

	/**
	 * @param quantumRatio the quantumRatio to set
	 **/
	public void setQuantumRatio(double quantumRatio) {
		this.quantumRatio = quantumRatio;
		if (quantumRatio == 0.) GenericObjectEditor.setHideProperty(this.getClass(), "quantumCloudDia", true);
		else GenericObjectEditor.setHideProperty(this.getClass(), "quantumCloudDia", false);
	}

	/**
	 * @return the highEnergyRaise
	 **/
	public double getHighEnergyRaise() {
		return highEnergyRaise;
	}

	/**
	 * @param highEnergyRaise the highEnergyRaise to set
	 **/
	public void setHighEnergyRaise(double highEnergyRaise) {
		this.highEnergyRaise = highEnergyRaise;
	}

	/**
	 * @return the highEnergyRatio
	 **/
	public double getHighEnergyRatio() {
		return highEnergyRatio;
	}

	/**
	 * @param highEnergyRatio the highEnergyRatio to set
	 **/
	public void setHighEnergyRatio(double highEnergyRatio) {
		this.highEnergyRatio = highEnergyRatio;
		if (highEnergyRatio == 0.) GenericObjectEditor.setHideProperty(this.getClass(), "highEnergyRaise", true);
		else GenericObjectEditor.setHideProperty(this.getClass(), "highEnergyRaise", false);
	}

	/**
	 * @return the maxSpeedLimit
	 **/
	public double getMaxSpeedLimit() {
		return maxSpeedLimit;
	}

	/**
	 * @param maxSpeedLimit the maxSpeedLimit to set
	 **/
	public void setMaxSpeedLimit(double maxSpeedLimit) {
		this.maxSpeedLimit = maxSpeedLimit;
	}

	/**
	 * @return the minSpeedLimit
	 **/
	public double getMinSpeedLimit() {
		return minSpeedLimit;
	}

	/**
	 * @param minSpeedLimit the minSpeedLimit to set
	 **/
	public void setMinSpeedLimit(double minSpeedLimit) {
		this.minSpeedLimit = minSpeedLimit;
	}

	/**
	 * @return the doSpeedAdaptation
	 **/
	public boolean isDoSpeedAdaptation() {
		return doSpeedAdaptation;
	}

	/**
	 * @param doSpeedAdaptation the doSpeedAdaptation to set
	 **/
	public void setDoSpeedAdaptation(boolean doSpeedAdaptation) {
		this.doSpeedAdaptation = doSpeedAdaptation;
		if (doSpeedAdaptation && getEmaPeriods()<1) {
			int newEmaP=15;
			System.err.println("warning: EMA periods at " + getEmaPeriods() + " and speed adaption set to true... setting it to "+ newEmaP);
			setEmaPeriods(newEmaP);
		}
		GenericObjectEditor.setHideProperty(getClass(), "minSpeedLimit", !doSpeedAdaptation);	
		GenericObjectEditor.setHideProperty(getClass(), "maxSpeedLimit", !doSpeedAdaptation);	
	}

	/**
	 * @return the changeDetectStrategy
	 **/
	public SelectedTag getChangeDetectStrategy() {
		return changeDetectStrategy;
	}

	/**
	 * @param changeDetectStrategy the changeDetectStrategy to set
	 **/
	public void setChangeDetectStrategy(SelectedTag changeDetectStrategy) {
		this.changeDetectStrategy = changeDetectStrategy;
		if (changeDetectStrategy.getSelectedTag().getID() == 0) { // random anchor
			detectAnchor = 0; // this will be set to a random individual
		} else detectAnchor = -1;
	}

	public String phi0TipText() {
		return "the random perturbation factor in relation to the problem range"; 
	}
	
	/**
	 * @return the phi0 value
	 **/
	public double getPhi0() {
		return phi0;
	}

	/**
	 * @param phi0 the phi0 to set
	 **/
	public void setPhi0(double phi0) {
		this.phi0 = phi0;
	}

	/**
	 * @return the phi3
	 */
	public double getPhi3() {
		return phi3;
	}

	/**
	 * @param phi3 the phi3 to set
	 */
	public void setPhi3(double phi3) {
		this.phi3 = phi3;
	}
	
	public String phi3TipText() {
		return "Acceleration of the problem specific attractor";
	}
	
	public Plot getPlot() {
		return m_Plot;
	}

	/**
	 * @return the plotBestOnly
	 */
	public boolean isPlotBestOnly() {
		return plotBestOnly;
	}

	/**
	 * @param plotBestOnly the plotBestOnly to set
	 */
	public void setPlotBestOnly(boolean plotBestOnly) {
		this.plotBestOnly = plotBestOnly;
	}
}