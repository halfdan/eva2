package eva2.server.go.strategies;

import java.util.Iterator;
import java.util.List;

import eva2.gui.GenericObjectEditor;
import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.InterfaceSolutionSet;
import eva2.server.go.populations.Population;
import eva2.server.go.populations.SolutionSet;
import eva2.server.go.problems.AbstractOptimizationProblem;
import eva2.server.go.problems.InterfaceHasInitRange;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.tribes.TribesExplorer;
import eva2.server.go.strategies.tribes.TribesParam;
import eva2.server.go.strategies.tribes.TribesPosition;
import eva2.server.go.strategies.tribes.TribesSwarm;


/**
 * This is the TRIBES algorithm, an adaptive, parameter-less PSO implementation.
 * I (MK) ported M.Clerc's java version 2006-02 21 and added the original notes below.
 * I had to do some modifications for the EvA framework, namely:
 * - minor adaptations allover the code to fit into the framework
 * - the objective value parameter must now be set within the GUI for each problem by hand (EvA doesnt assume to know an objective beforehand)
 * - discrete search spaces are not directly supported any more (no "granularity")
 * - the benchmark-collection is gone, it might be included into the EvA benchmark set in the future, though
 * - fixed two bugs (SunnySpell link generation, findWorst method)
 * - fixed bugs in the CEC 2005 benchmarks (see the corresponding class)
 * - I widely kept the original comments, except for places I changed the code so much that they might mislead
 * - thats all, I think
 *
 * I could produce similar results as Clerc's on Rosenbrock and Griewank, (in his book on p. 148),
 * I couldnt reproduce the 100% success rate on Ackley, though.
 *
 * @author Maurice Clerc, Marcel Kronfeld
 * @date 2007-09-13
 *
 * Original notes:
 * @version 2006-02 21
 * @author Maurice.Clerc@WriteMe.com
 * {@link http://mauriceclerc.net}
 * {@link http://clerc.maurice.free.fr/pso/}
 *
 */
/* Last updates (M.Clerc)
2006-02-21 Added a repelling option (see variable "repel" in Tribes). Not very convincing
2006-01-03 Fixed a bug in maxIsolated (the memory was not well updated after improvement)
           Also the same bug in "swarm" and "tribe". Thanks to Yann Cooreen.
           Fortunately it was apparently without incidence
2005-12-26  Added items ALL for benchmarks 1 and 2, so that you can launch the whole
            benchmark ... and have a cup of tea!
2005-12-26 Fixed a bug. When you launched several optimisations (almost) on the same time
             result were not all the same (synchronisation problems)
             So you had to launch each problem one at a time.
 Now it is done automatically. Even if you launch a problem before than the previous
 one is finished, in fact this second one is waiting the end of the other.

 2005-12-25 Removed bias in favor of strategy 5
 2005-12-25 Added a new way to generate the new tribe:
            the first particle in the biggest no man's land, and the other "around"
            A bit less efficient  at the beginning (i.e. for small number of
            evaluations, but also a bit _more_ efficient after that.
           See adaptSwarm, hardcoded parameter gMode.
 2005-12-25 Added some test options (repulsion) for TO DO 2005-12-23 items
            Not convincing for the moment
 2005-12-22 There was a mistake in memory management. Basically, I had written
            something like f2[]=f1[] for some vectors. In Java, it does _not_ make
            a copy of f1, but f2 and f1 are the same object (if f1 if modified later
            so is f2). The right way is a loop on components. Results are now a bit
            better.
 2005-12-21  Added strategy 7, mixture of velocity+hyperspheres. Not convincing
 2005-12-15 Non-multiobjective non-penalty approach for constrained problems.
            When a constraint is not satisfied, the particle simply goes back.
            See g01.txt for data example. Not convincing at all...
 2005-12-11 24 constrained problems (CEC 2006 benchmark
            http://www.ntu.edu.sg/home5/lian0012/cec2006/)
            seen as multiobjective problems (means you have to run a given problem
            several times, and to choose "manually" the best solution: all constraints
           equal to zero and minimum fitness. See synthSave.txt file
 2005-12-07 Multiobjective
 2005-11-30 "Blind" move for very good particles, with a probability Tribes.blind
            (?  Not very convincing)
 2005-11-25 Modified the way velocity is initialized (note that velocity is simply
            not used for some strategies, anyway). It is now less arbitrary.
            See explorer.generateExplorer()
 2005-11-24 Queen (don't seem to be very useful, though)
 2005-11-24 More benchmark problems (1 to 11, now)
 2005-11-24 Partly rewritten in a more modular way (easier to update).
            Note that it implies it is also  a bit slower...
 2005-11-21 Strategy 5 with adaptive confidence coefficients
 2005-11-21 keepIn option. If "false" the particle is not kept in the search space
           (an artifical fitness is computed, with a penalty)

 */

/* --- TO DO
 2006-02-23 Local search (say Newton-Raphson) from time to time around shamans
 2005-12-11 Automatically "clean up" the result file for multiobjective problems
            (as in the C version)
 2005-11-27 Local search for the shamans, for example pseudo-gradient method
 2005-11-25 Regular initialisation (cf. tesselation). Not really useful as long as
            the initial number of particles is very low.
 2005-11-22 possibility to define a list of values on any dimension (needed, for example
          for "Compression spring" problem) (Cf. C version of Tribes)

 */

/*  TO TRY
 2005-12-26  min(x1*x2-N) pour N entier donnÃ© et x1, x2 entiers
 2005-12-26 Un billard arrondi est chaotique, au contraire d'un rectangulaire.
            Que se passe-t-il pour les confinements si l'espace de recherche
            n'est plus un hyperparallÃ©lÃ©pipÃ¨de ?
 2005-12-26 Analyse harmonique. Vibrations de l'espace, des particules.
          DÃ©composition de Fourier au fur et Ã  mesure de l'Ã©chantillonnage =>
          description de plus en plus fine du paysage => estimations des zÃ©ros
 2005-12-25 Utiliser (cycliquement ?) une liste de nombres alÃ©atoires
            de plus en plus courte
 2005-12-23 WORK IN PROGRESS. Seriously modify the algorithm:
            - when generation is needed, generate one particle for the current tribe
              and one for the new tribe (?)
            - remove migration
            - maybe remove "delete particle" part

  2005-12-23  - use links between tribes for _repulsion_ :
              DONE as TEST. Not convincing. Maybe try some other rules
 2005-11-25 See if LS-CMA-ES (Hansen) could be sometimes used
 2005-11-21. Check if it is possible to easily find just the _value_ of the
             global minimum (not the position). "Chinese shadow" method?
 */

public class Tribes implements InterfaceOptimizer, java.io.Serializable {

	public static final boolean TRACE = false;

	protected String							m_Identifier = "TRIBES";
	transient private InterfacePopulationChangedEventListener m_Listener = null;
	protected AbstractOptimizationProblem 	m_problem;
	protected Population 					population;

	public static int maxExplorerNb = 200;
	public static int maxMemoryNb = 300;
	public static int maxTribeNb = 300;

	public static int[] strategies = new int[10]; // Just for information
	public static int[] status = new int[9]; // Just for information

	public static boolean testBC = false;  // TODO project to EvA2
	public static int adaptOption = 2;
	public static double blind=0;  // 0.5 //"Blind" move for very good particles, with a probability Tribes.blind
	public static boolean repel=false; // If 1, use a "repelling" strategy (see moveExplorer() )
	private boolean checkConstraints=true;

	private static final long serialVersionUID = 1L;

	TribesSwarm swarm = null;
	private int iter;
	protected double objectiveFirstDim = 0.;
	protected double[][] range, initRange;
	protected int notifyGenChangedEvery = 10;

	protected int problemDim;
	protected int adaptThreshold, adaptMax, adapt;
	protected int informOption;		/* For the best informant.
    								-1 => really the best
    								1 => the best according to a pseudo-gradient method
	 */
	protected int initExplorerNb=3; // Number of explorers at the very beginning
	// use full range (0) or subspace (1) for init options 0 and 1
	protected int rangeInitType=1;

   	private boolean m_Show = false;
   	transient protected eva2.gui.Plot      m_Plot = null;
//	private int useAnchors = 0;	// use anchors to detect environment changes? 

	public Object clone() {
		return new Tribes(this);
	}

	public Tribes() {
		hideHideable();
	}

	public Tribes(Tribes other) {
		this.SetProblem(other.getProblem());
		iter = other.iter;
		setObjectiveFirstDim(other.getObjectiveFirstDim());
		setDimension(other.range.length);
		setNotifyGenChangedEvery(other.getNotifyGenChangedEvery());
		range = other.range.clone();
		problemDim = other.problemDim;
		adaptThreshold = other.adaptThreshold;
		adaptMax = other.adaptMax;
		adapt = other.adapt;
		informOption = other.informOption;
		swarm = other.swarm.clone();
		initExplorerNb = other.initExplorerNb;
		rangeInitType = other.rangeInitType;
		population = new Population(1);
		hideHideable();
	}

	public void SetProblem(InterfaceOptimizationProblem problem) {
//		System.out.println("TRIBES.SetProblem()");
		m_problem = (AbstractOptimizationProblem)problem;
		range = null;
		if (problem instanceof InterfaceHasInitRange) {
			initRange = (double[][])((InterfaceHasInitRange)problem).getInitRange();
		}
		Population pop = new Population(1);
		problem.initPopulation(pop);
		setPopulation(pop);
	}

	public void init() {
//		System.out.println("TRIBES.init()");
		// Generate a swarm
		swarm = new TribesSwarm(this, range, initRange); // TODO initRange is hard coded equal to problem range
		//swarm.generateSwarm(initExplorerNb, initType, m_problem);

		//   swarm.displaySwarm(swarm,out);
		//  print("\n Best after init: "+swarm.Best.position.fitness,out);

        iter = 0;
        adapt = 0;
        informOption = -1;
//        Hard coded option
//        -1 = absolute best informant
//        1 = relative (pseudo-gradient) best informant. For "niching"
//       See also moveExplorer, which can be modified in order to avoid this parameter

        population.clear();
        population.addAll(swarm.toPopulation());
        population.init();	// necessary to allow for multi-runs
        
        if (m_Show) show();

	}

	/**
	 * As TRIBES manages an own structured set of particles (the list of Tribes containing explorers
	 * and memories), the setPopulation method is only telling Tribes the range
	 * of the indiviuals in the beginning of the run, the individuals will be discarded.
	 */
	public void initByPopulation(Population pop, boolean reset) {
		setPopulation(pop);
	}

	public AbstractEAIndividual getBestInd() {
		TribesPosition bestMemPos = swarm.getBestMemory().getPos();
		AbstractEAIndividual bestExp = population.getBestEAIndividual();
		if (bestMemPos.firstIsBetter(bestMemPos.getFitness(), bestExp.getFitness())) {
			AbstractEAIndividual indy = (AbstractEAIndividual)bestExp.clone();
			indy.SetFitness(bestMemPos.getFitness());
			((InterfaceDataTypeDouble)indy).SetDoubleGenotype(bestMemPos.getPos());
			return indy;
		} else return bestExp;
	}
	
	public void optimize() {
		
		int initOption = 0;
		if (iter == 0) { 	// first iteration!
			if (initRange == null) {
				rangeInitType = 0;
			} else {
				rangeInitType = 1; // 1 means in initRange for a start
			}
	     //* initOption Options: 0 - random, 1 - on the bounds, 2 - sunny spell, 3 - around a center
	     //* rangeInitType for options 0,1: 1 means use initRange, 0 use default range
			swarm.generateSwarm(initExplorerNb, initOption, rangeInitType, m_problem);
		}
		iter++;

		m_problem.evaluatePopulationStart(population);
		swarm.setSwarmSize();
		// swarm.Best.positionPrev = swarm.Best.position;

		swarm.moveSwarm(range, new TribesParam(), informOption, m_problem); //*** HERE IT MOVES and EVALUATES
		//public void moveSwarm(double[][] range, int fitnessSize, TribesParam pb, TribesSwarm swarm,
		//      int informOption, AbstractOptimizationProblem prob) {

		if (Tribes.adaptOption != 0) { // perform adaption
			if (Tribes.adaptOption == 1) { // Just reinitialize the swarm
				adaptThreshold = iter - adapt;
				//   adaptMax=swarmSize;
				adaptMax = swarm.linkNb(swarm);
				if (adaptThreshold >= adaptMax) {
					if (swarm.getBestMemory().getPrevPos().getTotalError() <=
						swarm.getBestMemory().getPos().getTotalError()) {
						adapt = iter; // Memorize at which iteration adaptation occurs

						for (int i = 0; i < swarm.getTribeCnt(); i++) {
							swarm.reinitTribe(i, rangeInitType, m_problem);
						}
					}
				}
			} else 		//   if(swarm.Best.positionPrev.getTotalError()<=swarm.Best.position.getTotalError())
			{
				// Structural adaptations

				adaptThreshold = iter - adapt;
				//   adaptMax=swarmSize;
				adaptMax = swarm.linkNb(swarm);

				if (adaptThreshold >= adaptMax) {
					adapt = iter; // Memorize at which iteration adaptation occurs
					swarm.adaptSwarm(rangeInitType, m_problem); // Re´alise l'adaptation
				}
			}
		}
		population.clear();
		population.addAll(swarm.toPopulation());
		if (m_Show) plotAll(population);
		m_problem.evaluatePopulationEnd(population);

//		this.population.incrFunctionCallsby(evals);
		this.population.incrGeneration();
		//this.firePropertyChangedEvent("NextGenerationPerformed");	// This is now done implicitely, as after every evaluation, addEvals is called

		if (TRACE) {
			System.out.println("loop finished after " + population.getFunctionCalls() + " evaluations");
			//for (int i=0; i<population.size(); i++) System.out.println(" * "+((TribesExplorer)population.get(i)).getStringRepresentation());
			System.out.println(" best: "+population.getBestEAIndividual().getStringRepresentation() + " - " + population.getBestEAIndividual().getFitness(0));
			System.out.println("swarm contains " + swarm.numParticles() +  " particles in iteration " + iter);
		}
	}

	private void plotAll(Population pop) {
		// TODO
		double pos[], vel[];

		for (int i=0; i<pop.size(); i++) {
			pos = ((TribesExplorer)pop.getEAIndividual(i)).getDoubleData();
			vel = ((TribesExplorer)pop.getEAIndividual(i)).getVelocity();
			plotIndy(pos, vel, i);
//			hier weiter!
		}
	}

	// TODO
	private void plotIndy(double[] curPosition, double[] curVelocity, int index) {
		if (this.m_Show) {
			if (curVelocity == null) {
				this.m_Plot.setUnconnectedPoint(curPosition[0], curPosition[1], index);
			} else {
				this.m_Plot.setConnectedPoint(curPosition[0], curPosition[1], index);
				this.m_Plot.setConnectedPoint(curPosition[0] + curVelocity[0], curPosition[1] + curVelocity[1], index);
			}
		}
	}

	/**
	 * This method is simply for debugging.
	 */
	protected void show() {
		if (this.m_Plot == null) {
//			InterfaceDataTypeDouble indy = (InterfaceDataTypeDouble)this.population.get(0);
//			double[][] range = indy.getDoubleRange();
//			double[] tmpD = new double[2];
//			tmpD[0] = 0;
//			tmpD[1] = 0;
			this.m_Plot = new eva2.gui.Plot("TRIBES "+ population.getGeneration(), "x1", "x2", range[0], range[1]);
//			this.m_Plot.setCornerPoints(range, 0);
		}
	}

	public static int particleNb(int D, int tribeNb) {
		//Cf. my "Binary PSO" study
		return (int) Math.round((9.5 + 0.124 * (D - 9)) / tribeNb);
	}

	/**

    public synchronized  void search(param pb, PrintStream runSave, PrintStream synthSave) {

        double epsMean, epsMin, epsMax;
        double evalMean;
        int n;
        int run, run1;
        int successNb;

        // Exploratrices gÃ©nÃ©rÃ©es
        explorer explorer[] = new explorer[
                                   Tribes.maxExplorerNb];

        double[] eps = new double[pb.maxRun];
        double[] evalNb = new double[pb.maxRun];
        double[] temp = new double[3];
        successNb = 0;
        for (n = 0; n < 9; n++) { // For information
            Tribes.strategies[n] = 0;
            Tribes.status[n] = 0;
        }
        ;
        epsMin = Tribes.infinity;
        epsMax = 0;

        // Titles

        print("\nIter.  Eval.  Best_fitness", displayPb);
        save("\n\n PROBLEM "+pb.function[0],synthSave);
        save("\nRun Iter.  Eval.  Best_fitness Position", synthSave);
        save("\n\n PROBLEM "+pb.function[0],runSave);

//        **
//         * Loop on runs
//         *
        for (run = 0; run < pb.maxRun; run++) {
            run1 = run + 1;
            save("\n" + run1 + " ", synthSave);
            temp = solve(pb, Tribes.initExplorerNb,runSave,synthSave);

            eps[run] = temp[0];
            evalNb[run] = temp[1];
            successNb = successNb + (int) temp[2];
            if (eps[run] < epsMin) {
                epsMin = eps[run];
            }
            if (eps[run] > epsMax) {
                epsMax = eps[run];
            }
        }

        // Mean values
        epsMean = 0;
        evalMean = 0;
        for (run = 0; run < pb.maxRun; run++) {
            epsMean = epsMean + eps[run];
            evalMean = evalMean + evalNb[run];
        }

        epsMean = epsMean / pb.maxRun;
        evalMean = evalMean / pb.maxRun;
        print("\nStatuses ", displayPb);
        for (n = 1; n < 10; n++) {
            print("\n" + n + " " + Tribes.status[n - 1] + " times",
                  displayPb);
        }
        print("\nStrategies ", displayPb);
        for (n = 1; n < 10; n++) {
            print("\n" + n + " " + Tribes.strategies[n - 1] + " times",
                  displayPb);
        }
        print("\nMIN BEST TOTAL_ERROR " + epsMin, displayPb);
        print("\nMEAN BEST TOTAL_ERROR " + epsMean, displayPb);
        print("\nMEAN EVAL. NUMBER " + evalMean, displayPb);
        print("\n SUCCESS RATE " + (double) successNb / pb.maxRun,
              displayPb);

        save("\nMIN BEST TOTAL_ERROR " + epsMin, synthSave);
        save("\nMEAN BEST TOTAL_ERROR " + epsMean, synthSave);
        save("\nMEAN EVAL. NUMBER " + evalMean, synthSave);
        save("\n SUCCESS RATE " + (double) successNb / pb.maxRun, synthSave);

        save("\n-1", runSave); // Special value for the end of the file. Used for graphics

    } // End of search()

    public double[] solve(param pb, int initExplorerNb, PrintStream runSave, PrintStream synthSave) {
        int adapt, adaptMax;
        int adaptThreshold;
        int d, D = pb.H.Dimension;
        int evalF;
        int iter;
        int n;
        boolean stop;
        double[] temp = new double[3];
        int informOption;
//         For the best informant.
//                              -1 => really the best
//                1 => the best according to a pseudo-gradient method

 // -----------INIT START
       // Generate a swarm
       evalF=0;
       swarm swarm = new swarm();
        evalF=swarm.generateSwarm(pb, initExplorerNb, pb.initType, displayPb,evalF);

        //   swarm.displaySwarm(swarm,out);
        //  print("\n Best after init: "+swarm.Best.position.fitness,out);

        // Move the swarm as long as the stop criterion is false
        iter = 0;
        adapt = 0;
        stop = false;
        informOption = -1;
//         Hard coded option
//        -1 = absolute best informant
//        1 = relative (pseudo-gradient) best informant. For "niching"
//       See also moveExplorer, which can be modified in order to avoid this parameter
//
 // -----------INIT END


 // -----------OPTIMIZE START
        iterations:while (!stop) {

            swarm.size = swarm.swarmSize(swarm);
            iter++;
            // swarm.Best.positionPrev = swarm.Best.position;

            evalF=swarm.moveSwarm(pb, swarm, informOption, displayPb, evalF); //*** HERE IT MOVES

//             Some display each time there is "enough" improvement
//              fduring the process

            double enough = 0.005;
            if ((1 - enough) * swarm.Best.positionPrev.totalError >
                swarm.Best.position.totalError) {
                print("\nIter. " + iter, displayPb);
                print(" Eval. " + evalF, displayPb);
                print("  totalError " + swarm.Best.position.totalError,
                      displayPb);
                print(" " + swarm.size + " particles", displayPb);
            }
// Save run info
            save("\n" + iter + " " + evalF + " " +
                 swarm.Best.position.totalError + " " + swarm.size, runSave);

            // Evaluate the stop criterion
            stop = evalF >= pb.maxEval ||
                   pb.accuracy > swarm.Best.position.totalError;

            if (Tribes.adaptOption == 0) {
                continue iterations;
            }

            if (Tribes.adaptOption == 1) { // Just reinitialize the swarm
                adaptThreshold = iter - adapt;
                //   adaptMax=swarmSize;
                adaptMax = swarm.linkNb(swarm);
                if (adaptThreshold >= adaptMax) {
                    if (swarm.Best.positionPrev.totalError <=
                        swarm.Best.position.totalError) {
                        adapt = iter; // Memorize at which iteration adaptation occurs

                        for (n = 0; n < swarm.tribeNb; n++) {
                            evalF=swarm.tribes[n].reinitTribe(pb,evalF);
                        }
                    }
                }
                continue iterations;
            }

            //   if(swarm.Best.positionPrev.totalError<=swarm.Best.position.totalError)
            {
                // Structural adaptations

                //swarmSize = swarm.swarmSize(swarm);

//                 print("\nSwarm size (explorers): " + swarmSize,out);
//                 print(" Tribes: (explorers/memories) ",out);
//                             for (n = 0; n < swarm.tribeNumber; n++) {
//                    print(swarm.tribes[n].explorerNb + "/" +
//                                     swarm.tribes[n].memoryNb + " ",out);
//                             }
//
//                 On "laisse le temps" Ã  chaque tribu de bouger avant Ã©ventuelle adaptation
//                  La rÃ¨gle est empirique et peut Ãªtre modifiÃ©e
//

                adaptThreshold = iter - adapt;
                //   adaptMax=swarmSize;
                adaptMax = swarm.linkNb(swarm);

                if (adaptThreshold >= adaptMax) {
                    adapt = iter; // Memorize at which iteration adaptation occurs
                    evalF=swarm.adaptSwarm(pb, Tribes.adaptOption, swarm,
                                     displayPb,evalF); // RÃ©alise l'adaptation

//                     Modifie la recherche de la meilleure informatrice
//                     normale (la "vraie" meilleure) ou dÃ©pendant d'un pseudo-gradient
//                     (cf. informExplorer)
//
                    //   informOption=-informOption;
                }
//                 print("\n Nb of tribes: " + swarm.tribeNumber +
//                                             "\n Particles/tribe:",out);
//                            for (n = 0; n < swarm.tribeNumber; n++) {
//                 print(swarm.tribes[n].explorerNb + " ",out);
//                            }
//
//                            print("\n Statuses       :");
//                            for (n = 0; n < swarm.tribeNumber; n++) {
//                               print(swarm.tribes[n].status + " ",out);
//                            }


            }
  // -----------OPTIMIZE END
        }

        // Result of the run
        print("\nBest: eval.= " + evalF + "\n", displayPb);
        swarm.Best.displayMemory(displayPb);

        save(" " + iter + " " + evalF+ " ", synthSave);

        for (d = 0; d < pb.fitnessSize; d++) {
            save(swarm.Best.position.fitness[d] + " ", synthSave);
        }

        for (d = 0; d < D; d++) {
            save(" " + swarm.Best.position.x[d], synthSave);
        }

// Prepare return
        temp[0] = swarm.Best.position.totalError;
        temp[1] = evalF;
        if (evalF < pb.maxEval) {
            temp[2] = 1;
        } else {
            temp[2] = 0;
        }

        return temp;
    }
	 **/

	/**
	 * Population will be hidden.
	 */
	public void hideHideable() {
		GenericObjectEditor.setShowProperty(getClass(), "population", false);
	}

	/**
	 * As TRIBES manages an own structured set of particles (the list of Tribes containing explorers
	 * and memories), the setPopulation method is only telling Tribes the range
	 * of the indiviuals in the beginning of the run, the individuals will be discarded.
	 */
	public void setPopulation(Population pop) {
		if (pop == null) return;
		population = pop;
		if (population.get(0) instanceof InterfaceDataTypeDouble) {
			range = ((InterfaceDataTypeDouble)population.get(0)).getDoubleRange();
			setDimension(range.length);
		} else {
			System.err.println("warning, TRIBES requires InterfaceESIndidivual instead of " + population.get(0).getClass() + ". Couldnt correctly init the problem range.");
		}
	}

	private void setDimension(int length) {
		problemDim = length;
		init();
	}

	public int getProblemDim() {
		return problemDim;
	}

	/**
	 * Be aware that TRIBES uses two kinds of particles: explorers and memories. As memories
	 * are inactive in that they dont search the problem space directly, they are not included
	 * in the returned population. This, however, means that the best found solution might not
	 * be inluded as well at several if not most stages of the search.
	 */
	public Population getPopulation() {
		return population;
	}
	
	/**
	 * Return a SolutionSet of TribesExplorers (AbstractEAIndividuals) of which some where 
	 * memory particles, thus the returned population is larger than the current population.
	 * 
	 * @return a population of possible solutions. 
	 */
    public InterfaceSolutionSet getAllSolutions() {
    	// return population and memories?
    	Population all = (Population)population.clone();
    	List<TribesPosition> mems = swarm.collectMem();
    	for (Iterator<TribesPosition> iterator = mems.iterator(); iterator.hasNext();) {
			TribesPosition tp = iterator.next();
			all.add(positionToExplorer(tp));
		}
    	all.SetFunctionCalls(population.getFunctionCalls());
    	all.setGenerationTo(population.getGeneration());
    	//all.addPopulation(pop);
    	return new SolutionSet(population, all);
    }
    
    protected TribesExplorer positionToExplorer(TribesPosition pos) {
    	TribesExplorer tmp = (TribesExplorer)population.get(0);
    	if (tmp == null) System.err.println("Error in Tribes::positionToExplorer!");
    	TribesExplorer indy = tmp.clone();
    	indy.clearPosVel();
    	indy.SetDoubleGenotype(pos.getPos());
    	indy.SetFitness(pos.getFitness());
    	return indy;
    }
	
    /** This method allows you to add the LectureGUI as listener to the Optimizer
	 * @param ea
	 */
	public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
		this.m_Listener = ea;
	}
	public boolean removePopulationChangedEventListener(
			InterfacePopulationChangedEventListener ea) {
		if (m_Listener==ea) {
			m_Listener=null;
			return true;
		} else return false;
	}
	protected void firePropertyChangedEvent(String name) {
		if (this.m_Listener != null) this.m_Listener.registerPopulationStateChanged(this, name);
	}

	public boolean notifyAfter(int evals) {
		return (evals % notifyGenChangedEvery) == 0;
	}

	public void freeWilly() {}

	public void setIdentifier(String name) {
		this.m_Identifier = name;
	}
	public String getIdentifier() {
		return m_Identifier;
	}

	public String getName() {
		return "TRIBES";
	}

	public InterfaceOptimizationProblem getProblem() {
		return m_problem;
	}

	public String getStringRepresentation() {
		return globalInfo();
	}
	
	public static String globalInfo() {
		return "TRIBES: a parameter free PSO implementation by Maurice Clerc.";
	}

	public void incEvalCnt() {
		population.incrFunctionCalls();
		if (notifyAfter(population.getFunctionCalls())) {
//			System.out.println("Notifying after " + population.getFunctionCalls());
			firePropertyChangedEvent(Population.nextGenerationPerformed);
		}
	}

	public double getObjectiveFirstDim() {
		return objectiveFirstDim;
	}

	public void setObjectiveFirstDim(double objectiveFirstDim) {
		this.objectiveFirstDim = objectiveFirstDim;
	}

	public String objectiveFirstDimTipText() {
		return "TRIBES uses an error approximation based on the minimum objective value in the first dimension depending on the problem";
	}

	public int getNotifyGenChangedEvery() {
		return notifyGenChangedEvery;
	}

	public void setNotifyGenChangedEvery(int notifyGenChangedEvery) {
		this.notifyGenChangedEvery = notifyGenChangedEvery;
	}

	public String notifyGenChangedEveryTipText() {
		return "Mainly for the GUI: plot fitness every n evaluations";
	}

	public boolean isCheckConstraints() {
		return checkConstraints;
	}

	public void setCheckConstraints(boolean checkConstraints) {
		this.checkConstraints = checkConstraints;
	}

	// TODO
//	/**
//	 * @return the useAnchor
//	 */
//	public boolean isUseAnchor() {
//		return useAnchor;
//	}
//
//	/**
//	 * @param useAnchor the useAnchor to set
//	 */
//	public void setUseAnchor(boolean useAnchor) {
//		this.useAnchor = useAnchor;
//	}

	/**
	 * @return the m_Show
	 */
	public boolean isShow() {
		return m_Show;
	}

	/**
	 * @param show the m_Show to set
	 */
	public void setShow(boolean show) {
		m_Show = show;
		if (!show) m_Plot = null;
	}
}
