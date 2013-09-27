package eva2.optimization.strategies;

import eva2.optimization.go.IndividualInterface;
import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.individuals.InterfaceDataTypeDouble;
import eva2.optimization.operator.distancemetric.EuclideanMetric;
import eva2.optimization.population.Population;
import eva2.optimization.problems.AbstractOptimizationProblem;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.tools.math.Mathematics;

import java.util.Vector;


/**
 * This implements a particle swarm optimizer which is used by the NichePSO and ANPSO
 * to represent and train the mainswarm or any subswarm.
 * This is done by extending the standard particle swarm optimizer so that
 * - additional member variables (characterizing and parameterizing the subswarm) are available
 * - additional information is added to the particles during an optimization loop
 * - additional functions to add, remove and reinitialize particles are available
 */
public class ParticleSubSwarmOptimization extends ParticleSwarmOptimizationGCPSO {

    protected double maxAllowedSwarmRadiusNormal; // maximal allowed swarmradius (relative to the search space)
    public static boolean hideFromGOE = true;   // dont want it to be available in the GUI
    protected boolean active;  // marks the swarm active or inactive

    protected double maxPosDist; // maximal possible distance in the search space, depends on problem -> initMaxPosDist()
    private int particleIndexCounter; // used to give each particle a unique index (for debbugging and plotting)
    private int fitnessArchiveSize = 15; // maximal number of fitnessvalues remembered from former iterations

    //ParameterupdateStrategies
//	InterfaceParameterAging inertnessAging = new NoParameterAging();

/**********************************************************************************************************************
 * ctors, clone
 */
    /**
     * @tested ps
     * ctor
     */
    public ParticleSubSwarmOptimization() {
        updateMaxPosDist();
        this.maxAllowedSwarmRadiusNormal = 0.1; // set similar to "particle swarms for multimodal optimization" by Oezcan and Yilmaz
        this.active = true;
        particleIndexCounter = getPopulation().size(); // newly added particles will start at this index
//		setInitialVelocity(1.);
    }

    /**
     * @tested cpyctor
     */
    public ParticleSubSwarmOptimization(ParticleSubSwarmOptimization a) {
        super(a);
        if (a.m_BestIndividual != null) {
            m_BestIndividual = (AbstractEAIndividual) a.m_BestIndividual.clone();
        }

        maxAllowedSwarmRadiusNormal = a.maxAllowedSwarmRadiusNormal;
        active = a.active;
        maxPosDist = a.maxPosDist;
        particleIndexCounter = a.particleIndexCounter;
//		inertnessAging = (InterfaceParameterAging)a.inertnessAging.clone();
        fitnessArchiveSize = a.fitnessArchiveSize;
    }

    /**
     * @tested (non-Javadoc) @see javaeva.server.oa.go.Strategies.ParticleSwarmOptimization#clone()
     */
    @Override
    public Object clone() {
        return (Object) new ParticleSubSwarmOptimization(this);
    }


/**********************************************************************************************************************
 * inits
 */
    /**
     * @tested ps
     * (non-Javadoc) @see javaeva.server.oa.go.Strategies.ParticleSwarmOptimization#init()
     */
    @Override
    public void init() {
        super.init();

        initIndividuals();

        updateMBestIndividual();
        updateMaxPosDist();
        particleIndexCounter = getPopulation().size();
//		setInertnessOrChi(inertnessAging.getStartValue());
    }

    /**
     * @tested ps
     * (non-Javadoc) @see javaeva.server.oa.go.Strategies.ParticleSwarmOptimization#initByPopulation(javaeva.server.oa.go.Populations.Population, boolean)
     */
    @Override
    public void initByPopulation(Population pop, boolean reset) {
        super.initByPopulation(pop, reset);
        initIndividuals();

        updateMBestIndividual();
        updateMaxPosDist();
        particleIndexCounter = getPopulation().size();
    }

    /**
     * @tested junit&
     * adds a vector for fitnessvalues to all individuals in the swarm
     * and sets the current fitness as the first value
     */
    protected void initIndividuals() {
        for (int i = 0; i < m_Population.size(); ++i) {
            AbstractEAIndividual indy = (AbstractEAIndividual) (m_Population.getEAIndividual(i));
            initSubSwarmDefaultsOf(indy);
        }
    }

    public static void initSubSwarmDefaultsOf(AbstractEAIndividual indy) {
        initFitnessArchiveOf(indy);
        initFitnessStdDevOf(indy);
        initPersonalBestOf(indy);
        initPBestImprInARowOf(indy);
    }

    public static void initFitnessArchiveOf(AbstractEAIndividual indy) {
        Vector<Double> vec = new Vector<Double>();
        double scalarFitness = sum(indy.getFitness()); // if multiobjective, use the sum of all fitnessvalues (dont use the norm because fitnessvalues may be negative)
        vec.add(new Double(scalarFitness));
        indy.putData(NichePSO.fitArchiveKey, vec);
    }

    /**
     * @tested emp
     * adds a std deviation value to an individual
     * and initially sets this value to infinity.
     */
    public static void initFitnessStdDevOf(AbstractEAIndividual indy) {
        // init stddev to inf, dont want immediate convergence...
        indy.putData(NichePSO.stdDevKey, new Double(Double.POSITIVE_INFINITY));
    }

    public static void initPBestImprInARowOf(AbstractEAIndividual indy) {
        indy.putData("PBestImprovementsInARow", new Integer(0));
    }

    /**
     * @tested junit
     * adds a representation of the personal best to an individual
     * and initially sets the current individual as its personal best.
     */
    public static void initPersonalBestOf(AbstractEAIndividual indy) {
        AbstractEAIndividual newpbest = (AbstractEAIndividual) indy.clone();
        newpbest.putData("PersonalBestKey", null);  // dont want to have a chain of pbests
        indy.putData("PersonalBestKey", newpbest);
    }

/**********************************************************************************************************************
 * Optimization
 */
    /**
     * @tested ps
     * (non-Javadoc) @see javaeva.server.oa.go.Strategies.ParticleSwarmOptimization#optimize()
     */
    @Override
    public void optimize() {
        super.optimize();
        updateFitnessArchives();
        updateFitnessStdDev();
        updatePersonalBest();
        updateMBestIndividual();
//		updateParameters();
    }

    public void reinitIndividuals(Vector<int[]> indicesToReinit) {
        for (int[] indices : indicesToReinit) {
            addNewParticlesToPopulation(indices);
        }
    }

    /**
     * Get the next set of indices increasing the internal particle counter.
     * Should only be called immediately before adding the new individuals.
     *
     * @param num
     * @return
     */
    private int[] getNextIndices(int num) {
        int[] indices = new int[num];
        for (int i = 0; i < num; ++i) {
            indices[i] = particleIndexCounter;
            ++particleIndexCounter;
        }
        return indices;
    }

    public void reinitIndividuals(int numIndies) {
        addNewParticlesToPopulation(getNextIndices(numIndies));
    }

    /**
     * *******************************************************************************************************************
     * updateTopology
     */

    protected AbstractEAIndividual getIndyByParticleIndexAndPopulation(Population pop, Integer index) {
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
//    		Integer tmp = (Integer)indy.getData("particleIndex"); // CPU-Time Hotspot
//    		if (index.equals(tmp)) return indy;
            if (index.intValue() == indy.getIndividualIndex()) {
                return indy;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see eva2.optimization.strategies.ParticleSwarmOptimization#addSortedIndizesTo(eva2.optimization.populations.Population)
     */
    @Override
    protected void addSortedIndicesTo(Object[] sortedPopulation, Population pop) {
        int origIndex;
        for (int i = 0; i < pop.size(); i++) {
            // cross-link the sorted list for faster access
            // (since NichePSO and ANPSO allow individuals
            // to leave and enter a population the index in the
            // population does not necessarily match the individual index)
            origIndex = ((AbstractEAIndividual) sortedPopulation[i]).getIndividualIndex();
            AbstractEAIndividual indy = getIndyByParticleIndexAndPopulation(pop, origIndex);
            indy.putData(sortedIndexKey, new Integer(i));
        }
    }

/**********************************************************************************************************************
 * updates
 */
    /**
     * @tested ps
     * when particles enter or leave the population,
     * call this method to update the status of the population
     */
    public void populationSizeHasChanged() {
        updateMBestIndividual();
    }

    /**
     * @tested junit&
     * updates the m_BestIndividual member which represents the gbest individual
     * (i.e. the best position and fitness encountered by any particle of the swarm)
     * Beware: if a particle enters the swarm its knowledge about its pbest enters the swarm,
     * on the other side: if a particle leaves the swarm its knowledge leaves the swarm as well.
     */
    protected void updateMBestIndividual() {
        if (getPopulation().size() == 0) {
            return;
        }
        // First: set m_BestIndividual to any personal best position in the swarm  (-> bestindypbest).
        // This is necessary because the current m_BestIndividual my came from
        // an individual that left the swarm and would never be replaced if it
        // would dominate all remaining positions in the swarm.
        AbstractEAIndividual bestindy = getPopulation().getBestEAIndividual();
        AbstractEAIndividual bestindypbest = (AbstractEAIndividual) bestindy.getData("PersonalBestKey");
        m_BestIndividual = (AbstractEAIndividual) bestindypbest.clone();
        for (int i = 0; i < getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = getPopulation().getEAIndividual(i);
            AbstractEAIndividual currentindypbest = (AbstractEAIndividual) currentindy.getData("PersonalBestKey");
            if (currentindypbest.isDominating(m_BestIndividual)) {
                m_BestIndividual = (AbstractEAIndividual) currentindypbest.clone();
//				++gbestImprovmentsInARow;
            } //else gbestImprovmentsInARow = 0;
        }
    }

    /**
     * @tested junit, dbg
     * adds the current fitnessvalue to the fitnessarchive for every individual in the swarm.
     * Keeps the fitnessarchive at a limited size (lim+1).
     */
    public void updateFitnessArchives() {
        //int lim = 3; // maximal number of fitnessvalues remembered from former iterations
        for (int i = 0; i < m_Population.size(); ++i) {
            AbstractEAIndividual indy = (AbstractEAIndividual) m_Population.getEAIndividual(i);
            Vector<Double> fitArchive_old = (Vector<Double>) (indy.getData(NichePSO.fitArchiveKey));
            double scalarFitness = sum(indy.getFitness()); // if multiobjective, use the sum of all fitnessvalues (dont use the norm because fitnessvalues may be negative)
            Double fitness = new Double(scalarFitness);

            Vector<Double> fitArchive_new = new Vector<Double>();
            int end = fitArchive_old.size();
            int start = 0;
            if (end >= fitnessArchiveSize) {
                start = end - fitnessArchiveSize;
            }

            for (int j = start; j < end; ++j) {
                fitArchive_new.add(fitArchive_old.get(j));
            }
            fitArchive_new.add(fitness);
            indy.putData(NichePSO.fitArchiveKey, fitArchive_new);
        }
    }

    /**
     * @tested junit
     * sets the std dev value of all individuals in the swarm
     * to the std deviation over the last 3 fitness values
     */
    public void updateFitnessStdDev() {
        for (int i = 0; i < m_Population.size(); ++i) {
            AbstractEAIndividual currentindy = m_Population.getEAIndividual(i);
            Vector<Double> fitnessArchive = (Vector<Double>) (currentindy.getData(NichePSO.fitArchiveKey));
            // the stddev is computed over 3 values as suggested in
            // "a niching particle swarm optimizer" by Brits et al.
            double sd = stdDev(fitnessArchive, NichePSO.defaultFitStdDevHorizon);
            currentindy.putData(NichePSO.stdDevKey, new Double(sd));
        }
    }

    /**
     * @tested junit&, junit
     * update the personal best representation if the current individual is better than the pbest
     */
    public void updatePersonalBest() {
        for (int i = 0; i < m_Population.size(); ++i) {
            AbstractEAIndividual currentindy = m_Population.getEAIndividual(i);
            AbstractEAIndividual pbest = (AbstractEAIndividual) currentindy.getData("PersonalBestKey");
            if (currentindy.isDominating(pbest)) {
                initPersonalBestOf(currentindy);

                //PBestImprovementsInARow
                Integer counter = (Integer) currentindy.getData("PBestImprovementsInARow");
                counter = new Integer(counter.intValue() + 1);
                currentindy.putData("PBestImprovementsInARow", counter);
            } else {
                initPBestImprInARowOf(currentindy);
            }
        }
    }

    /**
     * @tested junit ..
     * updates the member representing the maximal possible distance in the current searchspace
     */
    public void updateMaxPosDist() {
        //  compute the maximal possible distance in the search space:
        AbstractOptimizationProblem prob = (AbstractOptimizationProblem) m_Problem;
        // problem must have called initializeProblem, so that the template is set correctly. This shouls always be the case here...
        AbstractEAIndividual template = prob.getIndividualTemplate();
        if (template == null) {
            System.out.println("Problem does not implement getIndividualTemplate, updateMaxPosDist could not infer dimensions");
            return;
        }
        if (!(template instanceof ESIndividualDoubleData) && !(template instanceof InterfaceDataTypeDouble)) {
            System.out.println("Problem does not use ESIndividualDoubleData or InterfaceDataTypeDouble. UpdateMaxPosDist could not infer dimensions.");
            return;
        }
//		ESIndividualDoubleData min = (ESIndividualDoubleData)template.clone();
//		ESIndividualDoubleData max = (ESIndividualDoubleData)template.clone();

        double[][] range = null;
        if (template instanceof ESIndividualDoubleData) {
            range = ((ESIndividualDoubleData) template).getDoubleRange();
        } else {
            range = ((InterfaceDataTypeDouble) template).getDoubleRange();
        }
        double[] minValInDim = new double[range.length];
        double[] maxValInDim = new double[range.length];
        for (int i = 0; i < minValInDim.length; ++i) {
            minValInDim[i] = range[i][0]; // get lower boarder for dimension i
            maxValInDim[i] = range[i][1]; // get upper boarder for dimension i
        }
//		min.setDoubleGenotype(minValInDim); // set all dimensions to min
//		max.setDoubleGenotype(maxValInDim); // set all dimensions to max
        this.maxPosDist = Mathematics.euclidianDist(minValInDim, maxValInDim);
    }

//	/**
//	 * Parametervalues of the optimizer may change over time
//	 */
//	protected void updateParameters() {
//		setInertnessOrChi(inertnessAging.getNewParameterValue(getInertnessOrChi(), getPopulation().getGeneration()));
//	}
/**********************************************************************************************************************
 * dist, mean, stddev, sum
 */
    /**
     * @param indy1
     * @param indy2
     * @return
     * @tested junit
     * returns the euclidean distance in the search space between indy1 and indy2.
     */
    public double distance(AbstractEAIndividual indy1, AbstractEAIndividual indy2) {
        return EuclideanMetric.euclideanDistance(AbstractEAIndividual.getDoublePositionShallow(indy1),
                AbstractEAIndividual.getDoublePositionShallow(indy2));
    }

    /**
     * @param vec
     * @param range number of values (beginning at the end of vec!) considered to compute the mean
     * @return
     * @tested junit
     * returns the mean value for the provided data vec but only for a given number of values.
     * If range > vec.size() the mean is computed only for the available data.
     */
    protected static double mean(Vector<Double> vec, int range) {
        if (vec.size() < range) {
            range = vec.size();
        }
        double sum = 0;
        for (int i = vec.size() - range; i < vec.size(); ++i) {
            sum += vec.get(i).doubleValue();
        }
        return sum / range;
    }

    /**
     * @param vec   data
     * @param range number of values (beginning at the end of vec!) considered to compute the std deviation
     * @return
     * @tested junit
     * returns the std deviation for the provided data vec but only for a given number of values
     */
    public static double stdDev(Vector<Double> vec, int range) {
        double ssum = 0;
        if (vec.size() - range < 0 || range < 2) {
            return Double.POSITIVE_INFINITY;
        } // not enough values, dont risk early convergence
        double mean = mean(vec, range);
        for (int i = vec.size() - range; i < vec.size(); ++i) {
            ssum += Math.pow(vec.get(i).doubleValue() - mean, 2);
        }
        double result = Math.sqrt(ssum / (range - 1));
        return result;
    }

    private static double sum(double[] fitness) {
        double ret = 0.;
        for (double d : fitness) {
            ret += d;
        }
        return ret;
    }

    /**
     * @param normalisedRadius
     * @return
     * @tested ps
     * Interpretes the given maximal radius as normalised according to the current search space.
     * Values from [0,1], 1 means the radius can be as large as the maximal possible distance
     * (between any two points) in the search space.
     * Because the swarmradius and distances (e.g. in merging and absortion) are given in a standard euclidean metric,
     * this function converts the normalised distance into the standard euclidean distance.
     */
    public double interpreteAsNormalisedSwarmRadius(double normalisedRadius) {
        if (normalisedRadius > 1 || normalisedRadius < 0) {
            System.out.println("interpreteAsNormalisedSwarmRadius: Radius not normalised to [0,1]");
        }
        // compute standard euclidean radius from normalised radius:
        return normalisedRadius * maxPosDist;
    }

/**********************************************************************************************************************
 * addNewParticlesToPopulation ...
 */

    /**
     * @param particleIndices set of indices that should be used for the added particles, if null new indices are created
     * @tested junit ...
     * adds new particles to this swarm, rndly inited over the search space by the problem
     */
    private void addNewParticlesToPopulation(int[] particleIndices) {
        if (particleIndices == null) {
            throw new RuntimeException("Error, unable to use null index array (ParticleSubSwarmOptimization.addNewParticlesToPOpulation)");
        }

        Population tmp = new Population();
        tmp.setTargetSize(particleIndices.length);
        //////////////
        AbstractOptimizationProblem prob = (AbstractOptimizationProblem) m_Problem;
        AbstractEAIndividual template = prob.getIndividualTemplate(); // problem must be inited at this point
        AbstractEAIndividual tmpIndy;

        for (int i = 0; i < tmp.getTargetSize(); i++) {
            tmpIndy = (AbstractEAIndividual) ((AbstractEAIndividual) template).clone();
            tmpIndy.init(prob);
            tmp.add(tmpIndy);
        }
        tmp.init();
        ///////////

        ParticleSubSwarmOptimization tmpopt = new ParticleSubSwarmOptimization();
        tmpopt.setProblem(this.m_Problem);
        tmpopt.evaluatePopulation(tmp);
        tmpopt.initByPopulation(tmp, false); // + size FCs

        if (particleIndices != null) { // use given indices
            for (int i = 0; i < tmpopt.getPopulation().size(); ++i) {
                AbstractEAIndividual indy = tmpopt.getPopulation().getEAIndividual(i);
                indy.setIndividualIndex(particleIndices[i]);//SetData("particleIndex", new Integer(particleIndices[i]));
                indy.putData("newParticleFlag", new Boolean(true)); // for plotting
            }
        }

        addPopulation(tmpopt); //  add to the mainswarm (FCs will be considered)
        populationSizeHasChanged();
    }

/**********************************************************************************************************************
 * add and remove functions that keep the function calls of the population "accurate"
 * (here "accurate" applies to the situation where an added population is always deleted in the next step, like in merging...)
 */
    /**
     * @param pop
     * @tested nn
     * adds a population and its function calls to this.population
     */
    public void addPopulation(ParticleSubSwarmOptimization pop) {
        addPopulation(pop.getPopulation());
    }

    /**
     * @param pop
     * @tested junit& ..
     * adds a population and its function calls to this.population
     */
    public void addPopulation(Population pop) {
        m_Population.addPopulation(pop);

        // dont peculate the function calls from the added population (which is going to be deleted in NichePSO)
        m_Population.incrFunctionCallsBy(pop.getFunctionCalls());
    }

    /**
     * @param ind
     * @return
     * @tested nn
     * adss an inidividual
     */
    public boolean addIndividual(IndividualInterface ind) {
        // nothing to do regarding function calls
        // old calls were counted in old population new calls are now counted in this population
        return m_Population.addIndividual(ind);
    }

    /**
     * @param o
     * @return
     * @tested nn
     */
    public boolean add(Object o) {
        return addIndividual((IndividualInterface) o);
    }

    /**
     * @param indy
     * @tested nn
     * adds indy to the swarm
     */
    public void add(AbstractEAIndividual indy) {
        addIndividual(indy);
    }

    /**
     * @param ind
     * @tested nn
     * removes an individual
     */
    public boolean removeSubIndividual(IndividualInterface ind) {
        return m_Population.removeMember(ind);
    }

    public void removeSubPopulation(Population pop, boolean allowMissing) { // this is very slow...
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            if (!removeSubIndividual(indy)) {
                if (!allowMissing) {
                    throw new RuntimeException("Warning, assumed sub population was not contained (ParticleSubSwarmOptimization).");
                }
            }
        }
    }

	/*
    public void removeNIndividuals(int n) {
	}
	public void removeDoubleInstances() {
	}
	public void removeDoubleInstancesUsingFitness() {
	}
	public void removeIndexSwitched(int index) {
	}*/

/**********************************************************************************************************************
 * getter, setter
 */
    /**
     * @param problem
     * @tested ps
     * This method will set the problem that is to be optimized
     */
    @Override
    public void setProblem(InterfaceOptimizationProblem problem) {
        this.m_Problem = problem;
        updateMaxPosDist();
    }

    /**
     * @return the maximal euclidean distance between the swarms gbest and any other particle in the swarm.
     * @tested junit
     */
    public double getSwarmRadius() {
        if (getPopulation().size() == 0 || getPopulation().size() == 1) {
            return 0;
        }

        double max = Double.NEGATIVE_INFINITY;
        //PhenotypeMetric metric = new PhenotypeMetric();

        for (int i = 0; i < m_Population.size(); ++i) {
            AbstractEAIndividual indy = m_Population.getEAIndividual(i);
            //double dist = metric.distance(m_BestIndividual, indy);
            double sqrdDist = EuclideanMetric.squaredEuclideanDistance(AbstractEAIndividual.getDoublePositionShallow(m_BestIndividual),
                    AbstractEAIndividual.getDoublePositionShallow(indy));

            //dist = distance(m_BestIndividual, indy);
            if (sqrdDist > max) {
                max = sqrdDist;
            }
        }
        return Math.sqrt(max);
    }

    /**
     * @return
     * @tested ps
     * returns the maximal distance between the gbest position and any individual in the swarm
     * this distance is not allowed to exceed a given threshold
     */
    public double getBoundSwarmRadius() {
        // convert the normalised (i.e. relative) maxrad to a standard euclidean (i.e. absolute) maxrad
        double maxAllowedSwarmRadiusAbs = getMaxAllowedSwarmRadiusAbs();
        // only compare (absolute) euclidean distances
        return Math.min(getSwarmRadius(), maxAllowedSwarmRadiusAbs);
    }

    public double getMaxAllowedSwarmRadiusAbs() {
        // convert the normalised (i.e. relative) maxrad to a standard euclidean (i.e. absolute) maxrad
        return interpreteAsNormalisedSwarmRadius(maxAllowedSwarmRadiusNormal);
    }

    /**
     * @return the average distance a particle has to its neighbor
     */
    public double getAveDistToNeighbor() {
        Population pop = getPopulation();
        double sum = 0;
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual indy = pop.getEAIndividual(i);
            AbstractEAIndividual neigbor = getMemberNeighbor(indy);
            if (neigbor == null) {
                return -1;
            }
            sum += distance(indy, neigbor);
        }
        return sum / (double) pop.size();
    }

    /**
     * @return a measure for the diversity of the swarm
     */
    public double getEuclideanDiversity() {
        double meanDistanceFromGBestPos = 0;
        AbstractEAIndividual gbest = getGBestIndividual();
        for (int i = 0; i < getPopulation().size(); ++i) {
            AbstractEAIndividual indy = getPopulation().getEAIndividual(i);
            meanDistanceFromGBestPos += distance(gbest, indy);
        }
        meanDistanceFromGBestPos /= (double) getPopulation().size();
        return meanDistanceFromGBestPos;
    }

    /**
     * @param maxAllowedSwarmRadius value from the intervall [0,1]
     * @tested nn
     * binds the swarmradius to the given normalised value
     * (f.e. 1 means, that the swarmradius is allowed to take the maximal possible range in the search space)
     */
    public void SetMaxAllowedSwarmRadius(double maxAllowedSwarmRadius) {
        this.maxAllowedSwarmRadiusNormal = maxAllowedSwarmRadius;
    }

    /**
     * @return
     * @tested nn
     */
    public double getMaxAllowedSwarmRadius() {
        return this.maxAllowedSwarmRadiusNormal;
    }

    /**
     * @param active
     * @tested nn
     * marks the swarm as active or inactive
     */
    public void SetActive(boolean active) {
        this.active = active;
    }

    /**
     * @return
     * @tested nn
     */
    public boolean isActive() {
        return active;
    }

//	public InterfaceParameterAging getInertnessAging() {
//		return inertnessAging;
//	}
//
//	/**
//	 * sets a strategy that changes the value of the inertness parameter during an optimization run
//	 * @param inertnessAging
//	 */
//	public void setInertnessAging(InterfaceParameterAging inertnessAging) {
//		this.inertnessAging = inertnessAging;
//	}

    /**
     * @param indy particle should be from this swarm
     * @return null if there is no neighbor else neighbor
     * @tested junit
     * returns the particle with the minimal distance to indy
     */
    public AbstractEAIndividual getMemberNeighbor(AbstractEAIndividual indy) {
        if (getPopulation().size() == 0) {
            System.out.println("getNeighbor: swarm empty");
            return null;
        }
        // check if there is at least a second particle...
        if (getPopulation().size() == 1) {
            //System.out.println("getNeighbor: swarm too small, no neighbor available");
            //return (AbstractEAIndividual)indy.clone(); // would conflict with constant size for overall population...
            return null;
        }

        // get the neighbor...
        int index = -1;
        double mindist = Double.POSITIVE_INFINITY;
        boolean found = false;
        for (int i = 0; i < getPopulation().size(); ++i) {
            AbstractEAIndividual currentindy = getPopulation().getEAIndividual(i);
            if (indy.getIndyID() != currentindy.getIndyID()) { // dont compare particle to itself or a copy of itself
                double dist = distance(indy, currentindy);
                if (dist < mindist) {
                    mindist = dist;
                    index = i;
                }
            } else {
                found = true;
            }
        }

        if (!found) {
            System.err.println("getNeighbor: particle searching for neighbor is not part of the swarm");
        }
        return getPopulation().getEAIndividual(index);
    }

    public AbstractEAIndividual getGBestIndividual() {
        return m_BestIndividual;
    }

    /**
     * @param indyToExclude
     * @return particle with worst personal best position in the swarm. The given particle is excluded.
     */
    public AbstractEAIndividual getParticleWithWorstPBestButNot(AbstractEAIndividual indyToExclude) {
        Population pop = getPopulation();
        if (pop.size() < 2) {
            System.out.println("getParticleWithWorstPBestButNot: Population < 2 - returning null");
            return null;
        }
        AbstractEAIndividual indyWithWorstPBest = pop.getEAIndividual(0);
        if (indyWithWorstPBest == indyToExclude) {
            indyWithWorstPBest = pop.getEAIndividual(1);
        }
        AbstractEAIndividual worstPBest = (AbstractEAIndividual) indyWithWorstPBest.getData("PersonalBestKey");
        for (int i = 0; i < pop.size(); ++i) {
            AbstractEAIndividual currentindy = pop.getEAIndividual(i);
            AbstractEAIndividual currentpbest = (AbstractEAIndividual) currentindy.getData("PersonalBestKey");
            if (currentindy != indyToExclude && worstPBest.isDominating(currentpbest)) {
                indyWithWorstPBest = currentindy;
                worstPBest = currentpbest;
            }
        }
        return indyWithWorstPBest;
    }


    public int getFitnessArchiveSize() {
        return fitnessArchiveSize;
    }

    public void setFitnessArchiveSize(int fitnessArchiveSize) {
        this.fitnessArchiveSize = fitnessArchiveSize;
    }

    public String fitnessArchiveSizeTipText() {
        return "The number of fitness values stored per individual for deactivation strategies.";
    }
}
