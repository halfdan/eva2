package eva2.server.go.individuals;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import eva2.gui.BeanInspector;
import eva2.server.go.IndividualInterface;
import eva2.server.go.individuals.codings.gp.InterfaceProgram;
import eva2.server.go.operators.constraint.InterfaceConstraint;
import eva2.server.go.operators.crossover.InterfaceCrossover;
import eva2.server.go.operators.crossover.NoCrossover;
import eva2.server.go.operators.mutation.InterfaceMutation;
import eva2.server.go.operators.mutation.NoMutation;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.tools.EVAERROR;
import eva2.tools.math.RNG;

/** This is the abstract EA individual implementing the most important methods giving
 * access to mutation and crossover rates and operators, fitness values and selection
 * probabilities. All EA individuals should typically extend this abstract EA individual.
 * In that case the EA individuals only implement the genotype and phenotype interfaces.
 * The names of the implementation should be built like this:
 * (Genotype)Individual(Phenotype)
 * Thus a binary individual coding double values is named GAIndividualDoubleData and a
 * real-valued individual coding binary values is named ESIndividualBinaryData.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 11.05.2003
 * Time: 14:36:09
 * To change this template use Options | File Templates.
 */
public abstract class AbstractEAIndividual implements IndividualInterface, java.io.Serializable {
    public int                              m_FunctionCalls         = 0;        // TODO ist irgendwie eine Kruecke
    protected int                           m_Age                   = 0;
//    protected String                        m_Name                  = GONamingBox.getRandomName();
    
    private long 							m_ID	= 0;
    private static long						m_IDcounter = 0;
//  private int								logParentLen = 10;
    private boolean 						logParents = false;
	// heritage is to contain a list of all parents of the individual
    private Long[]							parentIDs = null;
    transient private AbstractEAIndividual[]			parentTree = null;

    protected double[]                      m_Fitness               = new double[1];
    private double                          m_ConstraintViolation   = 0;
    public boolean                          m_AreaConst4ParallelViolated = false; // no idea what felix used this for...
    public boolean                          m_Marked                = false;    // is for GUI only!
    public boolean							m_isPenalized			= false;	// may be set true for penalty based constraints

    protected double[]                      m_SelectionProbability  = new double[1];;
    protected double                           m_CrossoverProbability  = 1.0;
    protected double                           m_MutationProbability   = 0.2;
    protected InterfaceMutation             m_MutationOperator      = new NoMutation();
    protected InterfaceCrossover            m_CrossoverOperator     = new NoCrossover();
//    protected String[]                      m_Identifiers           = new String[m_ObjectIncrement];
//    protected Object[]                      m_Objects               = new Object[m_ObjectIncrement];
    protected HashMap<String,Object> 		m_dataHash 				= new HashMap<String,Object>();
    
    // introduced for the nichingPSO/ANPSO (M.Aschoff)
    private int individualIndex = -1;
    
    public AbstractEAIndividual() {
    	m_IDcounter++;
    	m_ID = m_IDcounter;
//  	System.out.println("my id is " + m_ID);
    }

    public long getIndyID() {
    	return m_ID;
    }

    public int getIndividualIndex() {
    	return individualIndex;
    }

    public void SetIndividualIndex(int index) {
    	this.individualIndex = index;
    }
    
    /** This method will enable you to clone a given individual
     * @return The clone
     */
    public abstract Object clone();

    /**
     * Set the mutation/crossover operator and probabilities to the given values.
     * 
     * @param mutOp
     * @param pMut
     * @param coOp
     * @param pCross
     */
    public void setOperators(InterfaceMutation mutOp, double pMut, InterfaceCrossover coOp, double pCross) {
    	m_MutationProbability = pMut;
    	m_MutationOperator = mutOp;
    	m_CrossoverProbability = pCross;
    	m_CrossoverOperator = coOp;
    }
    
    /**
     * Clone and init the mutation/crossover operator for the individual 
	 * and initialize the operators and probabilities to the given values.
     * 
     * @param mutOp
     * @param pMut
     * @param coOp
     * @param pCross
     */
    public void initCloneOperators(InterfaceMutation mutOp, double pMut, InterfaceCrossover coOp, double pCross, InterfaceOptimizationProblem problem) {
    	m_MutationProbability = pMut;
    	m_MutationOperator = (InterfaceMutation)mutOp.clone();
    	m_MutationOperator.init(this, problem);
    	m_CrossoverProbability = pCross;
    	m_CrossoverOperator = (InterfaceCrossover)coOp.clone();
    	m_CrossoverOperator.init(this, problem);
    }
    
    /**
     * Set the mutation/crossover operator and probabilities of the given individual to the given values.
     * 
     * @param indy
     * @param mutOp
     * @param pMut
     * @param coOp
     * @param pCross
     * @return the modified AbstractEAIndividual
     */
    public static AbstractEAIndividual setOperators(AbstractEAIndividual indy, InterfaceMutation mutOp, double pMut, InterfaceCrossover coOp, double pCross) {
    	indy.setOperators(mutOp, pMut, coOp, pCross);
    	return indy;
    }
    
    /** This methods allows you to clone the user data
     * Objects
     * @param individual    The individual to clone.
     */
    public void cloneAEAObjects(AbstractEAIndividual individual) {
//        m_Name              = new String(individual.m_Name);
    	m_dataHash			= (HashMap<String,Object>)(individual.m_dataHash.clone());
        m_ConstraintViolation = individual.m_ConstraintViolation;
//        m_AreaConst4ParallelViolated = individual.m_AreaConst4ParallelViolated;
        m_Marked            = individual.m_Marked;
        m_isPenalized		= individual.m_isPenalized;
        individualIndex = individual.individualIndex;
        if (individual.parentIDs != null) {
        	parentIDs = new Long[individual.parentIDs.length];
        	System.arraycopy(individual.parentIDs, 0, parentIDs, 0, parentIDs.length);
        	parentTree = new AbstractEAIndividual[individual.parentTree.length];
        	for (int i=0; i<parentTree.length; i++) parentTree[i] = individual.parentTree[i];
        }
    }

    /** This method allows you to compare two individuals
     * @param obj       The individual to compare to.
     * @return boolean  if equal true else false
     */
    public boolean equals(Object obj) {
    	if (this==obj) return true;
        if (obj instanceof AbstractEAIndividual) {
            AbstractEAIndividual indy = (AbstractEAIndividual) obj;

            // fitness is an important indicator
            if (!this.equalFitness(indy)) return false;
            this.m_ConstraintViolation = indy.m_ConstraintViolation;

            // check the genotypes
            if (!this.equalGenotypes(indy)) return false;

            // Age will not be used
            //if (this.m_Age != indy.m_Age) return false;

            // checking on mutation/crossover probabilities
            if (this.m_MutationProbability != indy.m_MutationProbability) return false;
            if (this.m_CrossoverProbability != indy.m_CrossoverProbability) return false;

            // checking in mutation/crossover operators
            if (!this.m_MutationOperator.equals(indy.m_MutationOperator)) return false;
            if (!this.m_CrossoverOperator.equals(indy.m_CrossoverOperator)) return false;
//            System.err.println("Check whether this is semantically meant by equality!!! (AbstractEAIndividual.equals())");
            return true;
        } else {
            return false;
        }
    }

    /** this method compares two individuals using only the fitness values
     * This is especially interesting perhaps for MOEAs
     * @param indy  The individual to compare to.
     * @return boolean if equal fitness true else false.
     */
    public boolean equalFitness(AbstractEAIndividual indy) {
    	double[] myF = getFitness();
    	double[] oF = indy.getFitness();
        if (myF.length != oF.length) return false;
        for (int i = 0; i < oF.length; i++) {
            if (myF[i] != oF[i]) return false;
        }
        return true;
    }

    public String getName() {
        return "AbstractEAIndividual";
    }
    
//    public String getIndividualName() {
//        return this.m_Name;
//    }

    /** This method is used when a new offspring is created
     * the increment the name.
     */
    public void giveNewName() {
    	// I dont think this is required for now
//        String  name = "";
//        if (this.m_Name.length() == 0) {
//            this.m_Name = GONamingBox.getRandomName();
//        } else {
//            if (this.m_Name.split(" ").length > 1) {
//                name = GONamingBox.getRandomName() + " " + this.m_Name.split(" ")[0] + "owitsch";
//                this.m_Name = name;
//            } else {
//                this.m_Name = GONamingBox.getRandomName() + " " + this.m_Name + "owitsch";
//            }
//        }
    }

    /** 
     * Returns a hash code value for the object. This method is supported for the
     * benefit of hashtables such as those provided by java.util.Hashtable
     */
    public int hashCode() {
        String t = AbstractEAIndividual.getDefaultStringRepresentation(this);
        return t.hashCode();
    }

    /** This method checks on equality regarding genotypic equality
     * @param individual      The individual to compare to.
     * @return boolean if equal true else false.
     */
    public abstract boolean equalGenotypes(AbstractEAIndividual individual);

//    /** Every object should have it's equals method, but who to programm it.
//     * Currently i will limit myself to check the class and the fitness
//     * values.
//     * @param obj
//     * @return True if the objects are equal.
//     */
//    public boolean equals(Object obj) {
//        if (obj instanceof AbstractEAIndividual) {
//            AbstractEAIndividual indy = (AbstractEAIndividual)obj;
//            if (this.m_Fitness.length != indy.m_Fitness.length) return false;
////            for (int i = 0; i < this.m_Fitness.length; i++) if (this.m_Fitness[i] != indy.m_Fitness[i]) return false;
////            for (int i = 0; i < this.m_Fitness.length; i++)
////                if (new Double(this.m_Fitness[i]).compareTo(new Double(indy.m_Fitness[i])) != 0) return false;
//            for (int i = 0; i < this.m_Fitness.length; i++) {
//                if (Math.abs(this.m_Fitness[i]- indy.m_Fitness[i]) > 0.00000001) return false;
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }

    /** This method will allow a default initialisation of the individual
     * @param opt   The optimization problem that is to be solved.
     */
    public void init(InterfaceOptimizationProblem opt) {
        this.defaultInit(opt);
        this.m_MutationOperator.init(this, opt);
        this.m_CrossoverOperator.init(this, opt);
    }

    /** This method will init the individual with a given value for the
     * phenotype.
     * @param obj   The initial value for the phenotype
     * @param opt   The optimization problem that is to be solved.
     */
    public abstract void initByValue(Object obj, InterfaceOptimizationProblem opt);

    /** This method will mutate the individual randomly
     */
    public void mutate() {
        if (RNG.flipCoin(this.m_MutationProbability)) this.m_MutationOperator.mutate(this);
    }

	/** 
	 * This method will mate the Individual with given other individuals
	 * of the same type with the individuals crossover probability and operator.
	 * The default operation is implemented here. Specialized individuals may
	 * override.
	 *  
	 * @param partners  The possible partners
	 * @return offsprings
	 */
	public AbstractEAIndividual[] mateWith(Population partners) {
	    AbstractEAIndividual[] result;
	    if (RNG.flipCoin(this.m_CrossoverProbability)) {
	        result = this.m_CrossoverOperator.mate(this, partners);
	        if (logParents) {
	        	for (int i = 0; i < result.length; i++) {
	        		result[i].setParents(this, partners);
	        	}
		    }
	    } else {
	        // simply return a number of perfect clones
	        result = new AbstractEAIndividual[partners.size() +1];
	        result[0] = (AbstractEAIndividual)this.clone();
	        for (int i = 0; i < partners.size(); i++) {
	            result[i+1] = (AbstractEAIndividual) ((AbstractEAIndividual)partners.get(i)).clone();
	        }
	        if (logParents) {
	        	result[0].setParent(this);
		        for (int i = 0; i < partners.size(); i++) {
		            result[i+1].setParent(partners.getEAIndividual(i));
		        }
		    }
	    }
//	    for (int i = 0; i < result.length; i++) {
//	    	result[i].giveNewName();
//	    }
	    return result;
	}
	
//	/**
//	 * Toggle the parent logging mechanism. It keeps track of the ancestor IDs of an individual
//	 * if mutation/crossover are used. Set the desired length of logging history (generations) or
//	 * set it to 0 to deactivate heritage logging.
//	 * 
//	 * @param logPs
//	 */
//	public void setLogHeritagetLen(int logLen) {
//		logParentLen = logLen;
//	}
	
	/**
	 * Add an ancestor generation with multiple parents.
	 * 
	 * @param parents
	 */
	protected void setParents(AbstractEAIndividual parent, Population parents) {
		int parentCnt = (parents == null) ? 1 : (1+parents.size());
		parentIDs = new Long[parentCnt];
		parentTree = new AbstractEAIndividual[parentCnt];
		parentIDs[0] = parent.getIndyID();
		parentTree[0] = (AbstractEAIndividual)parent.clone();
		if ((parents != null) && (parents.size() > 0)) {
			for (int i=0; i<parents.size(); i++) {
				parentIDs[i+1] = parents.getEAIndividual(i).getIndyID();
				parentTree[i+1] = (AbstractEAIndividual)parents.getEAIndividual(i).clone();
			}
		}
		
//		addHeritage(parentIDs);
	}
	
	/**
	 * Add an ancestor list with multiple parents.
	 * 
	 * @param parents
	 */
	public void setParents(List<AbstractEAIndividual> parents) {
		if ((parents == null) || (parents.size() == 0)) {
			parentIDs = null;
			parentTree = null;
		} else  {
			int parentCnt = parents.size();
			parentIDs = new Long[parentCnt];
			parentTree = new AbstractEAIndividual[parentCnt];
			
			for (int i=0; i<parentCnt; i++) {
				parentIDs[i] 	= parents.get(i).getIndyID();
				parentTree[i] 	= (AbstractEAIndividual)parents.get(i).clone();
			}
		}
	}
	
	public String getHeritageTree(int depth) {
		StringBuffer sb = new StringBuffer();
		sb.append(getIndyID());
		sb.append(" ");
		if ((depth > 0) && (parentTree != null)) {
			sb.append("[ ");
			for (int i=0; i<parentTree.length; i++) {
				sb.append(parentTree[i].getHeritageTree(depth - 1));
//					if ((i+1) < parentTree.length) sb.append(", ");
			}
			sb.append("] ");
		}
		return sb.toString();
	}
	
//	private void addHeritage(Long[] parentIDs) {
//		heritage.add(parentIDs);
////		if (heritage.size() > logParentLen) heritage.remove(0);
//	}
	
	/**
	 * Add an ancestor generation with only one parent.
	 * 
	 * @param parent
	 */
	protected void setParent(AbstractEAIndividual parent) {
		setParents(parent, null);
	}
	
	public Long[] getParentIDs() {
		return parentIDs;
	}
	
//	/**
//	 * Returns the last set of parental IDs or null if none are available.
//	 * 
//	 * @return the last set of parental IDs or null if none are available
//	 */
//	public Long[] getHeritage() {
//		if (heritage != null) return heritage.getLast();
//		else return null;
//	}
	
    /** This method will allow you to get the current age of an individual
     * Zero means it has not even been evaluated.
     * @return The current age.
     */
    public int getAge() {
        return this.m_Age;
    }

    /** This method allows you to set the age of an individual. The only
     * class allowed to set the age on an individual is the problem.
     * @param age   The new age.
     */
    public void SetAge(int age) {
        this.m_Age = age;
    }

    /** This method will incr the current age by one.
     */
    public void incrAge() {
        this.m_Age++;
    }

    /** This method allows you to reset the user data
     */
    public void resetUserData() {
    	m_dataHash.clear();
    }

    /** This method allows you to reset the level of constraint violation for an
     * individual
     */
    public void resetConstraintViolation() {
    	m_isPenalized=false;
        this.m_ConstraintViolation = 0;
    }

    /** This method allows you to add a new constraint violation to
     * the current level of constraint violation
     * @param c     The constraint violation.
     */
    public void addConstraintViolation(double c) {
        this.m_ConstraintViolation += Math.abs(c);
    }

    /** This method allows you to read the current level of constraint violation
     * @return The current level of constraint violation
     */
    public double getConstraintViolation() {
        return this.m_ConstraintViolation;
    }

    /** This method checks whether or not a constraint is violated
     * @return True if constraints are violated
     */
    public boolean violatesConstraint() {
        if (this.m_ConstraintViolation > 0) return true;
        else return false;
    }

    /** This method returns whether or not the individual is marked. This
     * feature is for GUI only and has been especially introduced for the
     * MOCCO GUI.
     * @return true if marked false if not
     */
    public boolean getMarked() {
        return this.m_Marked;
    }
    public void SetMarked(boolean t) {
        this.m_Marked = t;
    }
    public boolean isMarked() {
        return this.m_Marked;
    }
    public void unmark() {
        this.m_Marked = false;
    }
    public void mark() {
        this.m_Marked = true;
    }

    /**
     * Allows marking an individual as infeasible if fitness penalty is used.
     * @return
     */
    public boolean isMarkedPenalized() {
    	return m_isPenalized;
    } 
    /**
     * Allows marking an individual as infeasible if fitness penalty is used.
     * @return
     */
    public void SetMarkPenalized(boolean p) {
    	m_isPenalized=p;
    }
    
    /** This method can be used to read the current fitness of the individual.
     * Please note that the fitness can be based on multiple criteria therefore
     * double[] is used instead of a single double.
     * @return The complete fitness array
     */
    public double[] getFitness() {
        return this.m_Fitness;
    }

    /** This method returns the i-th fitness value if existent. If the i-th fitness
     * value does not exist zero is returned as default.
     * @param index       The index of the requested fitness value.
     * @return The fitness value at index
     */
    public double getFitness(int index) {
        if (this.m_Fitness.length > index) return this.m_Fitness[index];
        else return 0;
    }

    /** This method will set the complete Fitness of the individual
     * @param fitness   The new fitness array
     */
    public void SetFitness(double[] fitness) {
        this.m_Fitness = fitness;
    }

    /** This method allows you to set the i-th fitness value
     * @param index   The index of the fitness value to set.
     * @param fitness The new fitness value.
     */
    public void SetFitness(int index, double fitness) {
        if (this.m_Fitness.length > index) this.m_Fitness[index] = fitness;
        else {
            double[] tmpD = new double[index+1];
            for (int i = 0; i < this.m_Fitness.length; i++) {
                tmpD[i] = this.m_Fitness[i];
            }
            this.m_Fitness = tmpD;
            this.m_Fitness[index] = fitness;
        }
    }
    
    /** 
     * This method will set the fitness of the individual to the given value in every component.
     * If the fitness was null, nothing will be done.
     * 
     * @param resetVal   The new fitness array
     */
    public void resetFitness(double resetVal) {
    	if (m_Fitness!=null) {
    		for (int i=0; i<m_Fitness.length; i++) m_Fitness[i]=resetVal;
    	}
    }
    
    /** This method will check the constraints imposed by the separation schemes
     * for parallelizing MOEAs
     */
    public void checkAreaConst4Parallelization(ArrayList Constraints) {
        this.m_AreaConst4ParallelViolated = false;
        if (Constraints != null) {
            for (int i = 0; i < Constraints.size(); i++) {
                if (!((InterfaceConstraint)Constraints.get(i)).isValid(this)) this.m_AreaConst4ParallelViolated = true;
            }
        }
    }

    /** This method will allow you to compare two individuals regarding the dominance.
     * Note this is dominance! If the individuals are not comparable this method will
     * return false!
     * @param indy      The individual to compare to.
     * @return True if better false else
     */
    public boolean isDominating(AbstractEAIndividual indy) {
        if (this.m_AreaConst4ParallelViolated) return false;
        if (indy.m_AreaConst4ParallelViolated) return true;
        return isDominatingFitness(getFitness(), indy.getFitness());
//        for (int i = 0; (i < this.m_Fitness.length) && (i < tmpFitness.length); i++) {
//            if (this.m_Fitness[i] <= tmpFitness[i]) result &= true;
//            else result &= false;
//        }
//        return result;
    }
    
    /**
     * Returns true if the first fitness vector dominates the second one in every component.
     * Symmetric case: if the vectors are equal, true is returned.
     * 
     * @param fit1 first fitness vector to look at
     * @param fit2 second fitness vector to look at
     * @return true, if the first fitness vector dominates the second one
     */
    public static boolean isDominatingFitness(double[] fit1, double fit2[]) {
    	boolean result = true;
    	int i=0;
    	while (result && (i < fit1.length) && (i < fit2.length)) {
    		if (firstIsFiniteAndLargerOrEqual(fit1[i], fit2[i])) result = false;
    		i++;
    	}
    	return result;
    }
    
    private static boolean firstIsFiniteAndLargerOrEqual(double a, double b) {
		if (Double.isNaN(a) || Double.isInfinite(a)) return false;
		else return (a >= b);
	}

	/**
     * Returns true if the first fitness vector truly dominates the second one in every component.
     * 
     * @param fit1 first fitness vector to look at
     * @param fit2 second fitness vector to look at
     * @return true, if the first fitness vector truly dominates the second one
     */
    public static boolean isDominatingFitnessNotEqual(double[] fit1, double fit2[]) {
    	boolean result = true;
    	int i=0;
    	while (result && (i < fit1.length) && (i < fit2.length)) {
    		if (fit1[i] >= fit2[i]) result = false;
    		i++;
    	}
    	return result;
    }
    
    /** 
     * This method will allow you to compare two individuals regarding the constraint violation only, 
     * and only if the specific tag has been set. (For, e.g., additive penalty there is no way of discriminating
     * fitness and penalty after assigning).
     * The one with lesser violation is regarded better. If the instance is better than the given
     * indy, 1 is returned. If it is worse than the given indy, -1 is returned. 
     * If they both violate them equally, 0 is returned. 
     * This means that if both do not violate the constraints, 0 is returned.
     * 
     * @param indy      The individual to compare to.
     * @return 1 if the instance is better (regarding constraints only), -1 if is worse, 0 if they are equal in that respect.
     */
    public int compareConstraintViolation(AbstractEAIndividual indy) {
        if ((this.m_ConstraintViolation > 0) && (indy.m_ConstraintViolation <= 0)) return -1;
        if ((this.m_ConstraintViolation <= 0) && (indy.m_ConstraintViolation > 0)) return 1;
        else {  // both violate: ((this.m_ConstraintViolation > 0) && (indy.m_ConstraintViolation > 0)) {
            if (this.m_ConstraintViolation < indy.m_ConstraintViolation) return 1 ;
            else if (this.m_ConstraintViolation > indy.m_ConstraintViolation) return -1;
            else return 0;
        }
    }
    
    /** This method will allow you to compare two individuals regarding the dominance.
     * Note this is dominance! If the individuals are not comparable this method will
     * return false!
     * @param indy      The individual to compare to.
     * @return True if the own fitness dominates the other indy's
     */
    public boolean isDominatingDebConstraints(AbstractEAIndividual indy) {
        int constrViolComp = compareConstraintViolation(indy);
        if (constrViolComp==0) return isDominatingFitness(getFitness(), indy.getFitness());
        else return (constrViolComp > 0);
//        for (int i = 0; (i < this.m_Fitness.length) && (i < tmpFitness.length); i++) {
//            if (this.m_Fitness[i] <= tmpFitness[i]) result &= true;
//            else result &= false;
//        }
//        return result;
    }

    /** This method will allow you to compare two individuals regarding the dominance.
     * Note this is dominance! If the individuals are not comparable this method will
     * return false!
     * @param indy      The individual to compare to.
     * @return True if better false else
     */
    public boolean isDominatingEqual(AbstractEAIndividual indy) {
    	// TODO: should this method really be called "..Equal"?
        if (this.m_AreaConst4ParallelViolated) return false;
        if (indy.m_AreaConst4ParallelViolated) return true;
        return isDominatingFitnessNotEqual(getFitness(), indy.getFitness());
    }

    /** 
     * This method will allow you to compare two individuals regarding the dominance.
     * Note this is dominance! If the individuals are not comparable this method will
     * return false!
     *
     * @param indy      The individual to compare to.
     * @return True if better false else
     */
    public boolean isDominatingDebConstraintsEqual(AbstractEAIndividual indy) {
    	// TODO: should this method really be called "..Equal"?
        int constrViolComp = compareConstraintViolation(indy);
        if (constrViolComp==0) return isDominatingFitnessNotEqual(getFitness(), indy.getFitness());
        else return (constrViolComp > 0);
//        return isDominatingFitnessNotEqual(getFitness(), indy.getFitness());
    }

    /** This method can be used to read the current selection probability of the individual.
     * Please note that the selection probability can be based on multiple criteria therefore
     * double[] is used instead of a single double.
     * @return The complete selection probability array
     */
    public double[] getSelectionProbability() {
        return this.m_SelectionProbability;
    }

    /** This method returns the i-th selection probability value if existent. If the i-th selection probability
     * value does not exist zero is returned as default.
     * @param index       The index of the requested fitness value.
     * @return The selection probability value at index
     */
    public double getSelectionProbability(int index) {
        if (this.m_SelectionProbability.length > index) return this.m_SelectionProbability[index];
        else return 0;
    }

    /** This method will set the complete selection probability of the individual
     * @param sel   The new selection probability array
     */
    public void SetSelectionProbability(double[] sel) {
        this.m_SelectionProbability = sel;
    }

    /** This method allows you to set the i-th selection probability value
     * @param index     The index of the selection probability value to set.
     * @param sel       The new selection probability value.
     */
    public void SetSelectionProbability(int index, double sel) {
        if (this.m_SelectionProbability.length > index) this.m_SelectionProbability[index] = sel;
        else {
            double[] tmpD = new double[index+1];
            for (int i = 0; i < this.m_SelectionProbability.length; i++) {
                tmpD[i] = this.m_SelectionProbability[i];
            }
            this.m_SelectionProbability = tmpD;
            this.m_SelectionProbability[index] = sel;
        }
    }

    /** This method allows you to choose from multiple mutation operators.
     * Note: If the operator doeesn't suite the data nothing will happen.
     * @param mutator   The mutation operator.
     */
    public void setMutationOperator(InterfaceMutation mutator) {
        this.m_MutationOperator = mutator;
    }
    public InterfaceMutation getMutationOperator() {
        return this.m_MutationOperator;
    }
    public String mutationOperatorTipText() {
        return "Choose the mutation operator to use.";
    }

    /** This method allows you to set the mutation probability, e.g. the chance that
     * mutation occurs at all.
     * @param mutprob   The mutation probability.
     */
    public void setMutationProbability(double mutprob) {
        if (mutprob < 0) mutprob = 0;
        if (mutprob > 1) mutprob = 1;
        m_MutationProbability = mutprob;
    }
    public double getMutationProbability() {
        return m_MutationProbability;
    }
    public String mutationProbabilityTipText() {
        return "The chance that mutation occurs.";
    }

    /** This method allows you to choose from multiple crossover operators.
     * Note: If the operator doeesn't suite the data nothing will happen.
     * @param crossover   The crossover operator.
     */
    public void setCrossoverOperator(InterfaceCrossover crossover) {
        this.m_CrossoverOperator = crossover;
    }
    public InterfaceCrossover getCrossoverOperator() {
        return this.m_CrossoverOperator;
    }
    public String crossoverOperatorTipText() {
        return "Choose the crossover operator to use.";
    }

    /** This method allows to set the crossover probability
     * @param prob
     */
    public void setCrossoverProbability(double prob) {
        this.m_CrossoverProbability = prob;
        if (this.m_CrossoverProbability > 1) this.m_CrossoverProbability = 1;
        if (this.m_CrossoverProbability < 0) this.m_CrossoverProbability = 0;
    }
    public double getCrossoverProbability() {
        return this.m_CrossoverProbability;
    }
    public String crossoverProbabilityTipText() {
        return "The chance that crossover occurs.";
    }

    /** This method allows you to store an arbitrary value under an arbitrary
     * name.
     * @param name      The identifying name.
     * @param obj       The object that is to be stored.
     */
    public void putData(String name, Object obj) {
   		m_dataHash.put(name, obj);
    }

    /** 
     * This method will return a stored object associated with a key String.
     * The String "Fitness" is always a valid key connected to the
     * individual fitness array.
     * 
     * @param name      The name of the requested Object
     * @return Object	The associated object or null if none is found
     */
    public Object getData(String name) {
//        if (name.equalsIgnoreCase("SelectionProbability")) return this.getSelectionProbability();
//        if (name.equalsIgnoreCase("SelectionProbabilityArray")) return this.getSelectionProbability();
//        if (name.equalsIgnoreCase("Fitness")) return this.getFitness();
//        if (name.equalsIgnoreCase("FitnessArray")) return this.getFitness();
    	Object data = m_dataHash.get(name);
    	if (data==null) { // Fitness is actually in use... so lets have a minor special treatment
//    		try {
    			if (name.compareToIgnoreCase("Fitness")==0) data = getFitness();
    			else {
        			EVAERROR.errorMsgOnce("Warning: data key " + name + " unknown (pot. multiple errors)!");
//    				throw new RuntimeException(name + ": unknown key!");
    			}
//    		} catch(Exception e) {
//    			e.printStackTrace(System.err);
//    		}
    	}
        return data;
    }
    
    /** 
     * Check whether the individual has an object in store which is associated with
     * the given key String. The String "Fitness" is always a valid key connected to the
     * individual fitness array.
     * 
     * @param key      The name of the requested Object.
     * @return true if data is associated with the key, else false
     */
    public boolean hasData(String key) {
    	if (m_dataHash.get(key)==null) {
    		return (key.compareToIgnoreCase("Fitness")==0);
    	} else return true;
    }

    /** This method will return a string description of the Individal
     * noteably the Genotype.
     * @return A descriptive string
     */
    public abstract String getStringRepresentation();
    
    /** 
     * This method creates a default String representation for an AbstractEAIndividual
     * with genotype and fitness representation.
     * 
     * @param individual    The individual that is to be shown.
     * @return The description.
     */
    public static String getDefaultStringRepresentation(AbstractEAIndividual individual) {
    	// Note that changing this method might change the hashcode of an individual 
    	// which might interfere with some functionality.
        StringBuffer sb = new StringBuffer("Fit.:\t");
        sb.append(BeanInspector.toString(individual.getFitness()));
        if (individual.isMarkedPenalized() || individual.violatesConstraint()) 
        	sb.append(", X"); 
        sb.append(", ID: ");
        sb.append(individual.getIndyID());
        sb.append(",\t");
        sb.append(getDefaultDataString(individual));

        if (individual.getParentIDs()!=null) {
        	sb.append(", parents: ");
            sb.append(BeanInspector.toString(individual.getParentIDs()));
        }
        return sb.toString();
    }
    
    /**
     * This method creates a default String representation for a number Individual interfaces
     * containing the genotype.
     * 
     * @param individual
     * @return
     */
    public static String getDefaultDataString(IndividualInterface individual) {
    	// Note that changing this method might change the hashcode of an individual 
    	// which might interfere with some functionality.
    	return getDefaultDataString(individual, ",");
    }
    
    /**
     * This method creates a default String representation for a number Individual interfaces
     * containing the genotype.
     * 
     * @param individual
     * @return
     */
    public static String getDefaultDataString(IndividualInterface individual, String separator) {
    	// Note that changing this method might change the hashcode of an individual 
    	// which might interfere with some functionality.
    	if (individual == null) return "null";
        StringBuffer sb = new StringBuffer("");
        Formatter fm = new Formatter(sb);
        char left = '{';
        char right = '}';
        sb.append(left);
        if (individual instanceof InterfaceDataTypeBinary) {
            BitSet b = ((InterfaceDataTypeBinary)individual).getBinaryData();
            for (int i = 0; i < ((InterfaceDataTypeBinary)individual).size(); i++) {
                if (b.get(i)) sb.append("1");
                else sb.append("0");
            }
        } else if (individual instanceof InterfaceDataTypeInteger) {
            int[] b = ((InterfaceDataTypeInteger)individual).getIntegerData();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append(separator);
            }
        } else if (individual instanceof InterfaceDataTypeDouble) {
            double[] d = ((InterfaceDataTypeDouble)individual).getDoubleData();
            for (int i = 0; i < d.length; i++) {
//                sb.append(d[i]);
//                if ((i+1) < d.length) sb.append(separator);
            	fm.format("% .3f", d[i]);
            	if ((i+1) < d.length) sb.append(separator);
            }
        } else if (individual instanceof InterfaceDataTypePermutation) {
            int[] b = ((InterfaceDataTypePermutation)individual).getPermutationData()[0];
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i]);
                if ((i+1) < b.length) sb.append(separator);
            }
        } else if (individual instanceof InterfaceDataTypeProgram) {
            InterfaceProgram[] b = ((InterfaceDataTypeProgram)individual).getProgramDataWithoutUpdate();
            for (int i = 0; i < b.length; i++) {
                sb.append(b[i].getStringRepresentation());
                if ((i+1) < b.length) sb.append(separator);
            }
        } else if (BeanInspector.hasMethod(individual, "toString", null) != null) {
        	EVAERROR.errorMsgOnce("warning in AbstractEAIndividual::getDefaultDataString: type " + individual.getClass() + " has no default data representation, using toString...");
        	return individual.toString();
        } else {
        	System.err.println("error in AbstractEAIndividual::getDefaultDataString: type " + individual.getClass() + " not implemented");
        }
        sb.append(right);
        return sb.toString();
    }

    public String toString() {
    	return getDefaultStringRepresentation(this);
    }
    
	/**
	 * For any AbstractEAIndividual try retrieve its position as double[].
	 * Returns null if there is no conversion available. Makes shallow copies if possible.
	 * 
	 * @param indy
	 * @return double valued position of an individual or null
	 */
	public static double[] getDoublePositionShallow(AbstractEAIndividual indy) {
		if (indy instanceof InterfaceESIndividual) {
			return ((InterfaceESIndividual)indy).getDGenotype();
		} else if (indy instanceof InterfaceDataTypeDouble) {
			return ((InterfaceDataTypeDouble)indy).getDoubleData();
		} else if (indy instanceof InterfaceDataTypeInteger) {
			int[] intData =  ((InterfaceDataTypeInteger)indy).getIntegerData();
			double[] pos = new double[intData.length];
			for (int i=0; i<intData.length; i++) pos[i] = (double)intData[i];
			return pos;
		} // TODO check some more types here?
		EVAERROR.errorMsgOnce("Unhandled case in AbstractEAIndividual.getPosition()!");
		return null;
	}
	
	/**
	 * For any AbstractEAIndividual try to convert its position to double[] and return it.
	 * Returns null if there is no conversion available.
	 * 
	 * @param indy
	 * @return double valued position of an individual or null
	 */
	public static double[] getDoublePosition(AbstractEAIndividual indy) {
		if (indy instanceof InterfaceESIndividual) {
			return ((InterfaceESIndividual)indy).getDGenotype().clone();
		} else if (indy instanceof InterfaceDataTypeDouble) {
			return ((InterfaceDataTypeDouble)indy).getDoubleData().clone();
		} else if (indy instanceof InterfaceDataTypeInteger) {
			int[] intData =  ((InterfaceDataTypeInteger)indy).getIntegerData();
			double[] pos = new double[intData.length];
			for (int i=0; i<intData.length; i++) pos[i] = (double)intData[i];
			return pos;
		} // TODO check some more types here?
		EVAERROR.errorMsgOnce("Unhandled case in AbstractEAIndividual.getDoublePosition()!");
		return null;
	}
    
	/**
	 * For any AbstractEAIndividual try to convert its position to double[] and return it.
	 * Returns null if there is no conversion available.
	 * 
	 * @param indy
	 * @return double valued position of an individual or null
	 */
	public static boolean setDoublePosition(AbstractEAIndividual indy, double[] pos) {
		if (indy instanceof InterfaceESIndividual) {
			((InterfaceESIndividual)indy).SetDGenotype(pos);
			return true;
		} else if (indy instanceof InterfaceDataTypeDouble) {
			((InterfaceDataTypeDouble)indy).SetDoubleGenotype(pos);
			return true;
		} else if (indy instanceof InterfaceDataTypeInteger) {
			EVAERROR.errorMsgOnce("Warning, double position truncated to integer! (AbstractEAIndividual.setDoublePosition)");
			int[] intData = new int[pos.length];
			for (int i=0; i<intData.length; i++) intData[i] = (int)pos[i];
			((InterfaceDataTypeInteger)indy).SetIntGenotype(intData);
			return true;
		} // TODO check some more types here?
		EVAERROR.errorMsgOnce("Unhandled case in AbstractEAIndividual.setDoublePosition()!");
		return false;
	}
	
	/**
	 * Try to convert the individuals position to double[] and return it.
	 * Returns null if there is no conversion available.
	 * 
	 * @param indy
	 * @return double valued position of an individual or null
	 */
	public double[] getDoublePosition() {
		return AbstractEAIndividual.getDoublePosition(this);
	}
	
	
	 /**
     * @return true if parent history logging is activated
     * 
     */
    protected boolean isLogParents() {
		return logParents;
	}

    /** This method allows you to toggle parent history logging
     * @param logParents   true if logging should be activated
     */
	protected void setLogParents(boolean logParents) {
		this.logParents = logParents;
	}
	
	
/**********************************************************************************************************************
 * Implementing the Individual Interface
 */
    public IndividualInterface getClone() {
        return (IndividualInterface)this.clone();
    }

//    /** This method is used to get the basic data type of an individual double[].
//     * @deprecated Since not all EAIndividuals provide double as basic data type
//     * the fitness can be is returned as default value.
//     * @see #getFitness()
//     * @return double[]
//     */
//    public double[] getDoubleArray() {
//        if (this instanceof InterfaceDataTypeDouble) return ((InterfaceDataTypeDouble)this).getDoubleData();
//        else return this.getFitness();
//    }
    
    public boolean isDominantNotEqual(double[] otherFitness) {
    	return isDominatingFitnessNotEqual(m_Fitness, otherFitness);
    }
    
    /**
     * @return true if the own fitness dominates otherFitness.
     * @see #isDominatingFitness(double[], double[])
     */
    public boolean isDominant(double[] otherFitness) {
    	return isDominatingFitness(m_Fitness, otherFitness);
    }
    
    /**
     * @return true if the individual dominates the given one.
     * @see #isDominatingDebConstraints(AbstractEAIndividual)
     */
    public boolean isDominant(IndividualInterface indy) {
    	return isDominatingDebConstraints((AbstractEAIndividual)indy);
    }

    /**
     * Return the individual data (phenotype) as BitSet, int[], double[], int[] (coding permutation)
     * or InterfaceProgram[].
     * 
     * @param individual
     * @return
     */
	public static Object getIndyData(AbstractEAIndividual individual) {
        if (individual instanceof InterfaceDataTypeBinary) {
            BitSet b = ((InterfaceDataTypeBinary)individual).getBinaryData();
            return b;
        } else if (individual instanceof InterfaceDataTypeInteger) {
            int[] b = ((InterfaceDataTypeInteger)individual).getIntegerData();
            return b;
        } else if (individual instanceof InterfaceDataTypeDouble) {
            double[] b = ((InterfaceDataTypeDouble)individual).getDoubleData();
            return b;
        } else if (individual instanceof InterfaceDataTypePermutation) {
            int[] b = ((InterfaceDataTypePermutation)individual).getPermutationData()[0];
            return b;
        } else if (individual instanceof InterfaceDataTypeProgram) {
            InterfaceProgram[] b = ((InterfaceDataTypeProgram)individual).getProgramData();
            return b;
        } else {
        	System.err.println("error in AbstractEAIndividual::getDefaultDataString: type " + individual.getClass() + " not implemented");
        	return null;
        }
	}

}
  