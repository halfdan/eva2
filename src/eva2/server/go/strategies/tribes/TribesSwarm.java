package eva2.server.go.strategies.tribes;

import java.util.ArrayList;
import java.util.List;

import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.Tribes;
import eva2.tools.math.RNG;


public class TribesSwarm implements java.io.Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = 1L;
	int tribeNb, explorerLabel, memoryLabel;
    Tribe[] tribes;
    Tribes masterTribe;
    private double[][] range, initRange;
    TribesMemory bestMem = null;

    int explorerNb, memoryNb;
    int size;
    TribesExplorer queen = null;
    //int[] worst = new int[2]; // Tribe and rank in the tribe of the worst memory

    /**
     * If initRange is not null, new particles are generated in that range only. 
     */
    public TribesSwarm(Tribes master, double[][] range, double[][] initRange) {
    	masterTribe = master;
    	tribes = new Tribe[Tribes.maxTribeNb];
        queen = new TribesExplorer(range, (master == null) ? 0. : master.getObjectiveFirstDim());
        bestMem = new TribesMemory(range.length);

        this.range = range;
        if (initRange != null) {
        	this.initRange = initRange;
        } else {
        	this.initRange = null;
        }
    }

    public TribesSwarm(TribesSwarm other) {
    	tribeNb = other.tribeNb;
    	explorerLabel = other.explorerLabel;
    	memoryLabel = other.memoryLabel;
    	tribes = other.tribes.clone();
    	masterTribe = other.masterTribe; // no deep clone as this is an uplink
    	range = other.range.clone();
    	initRange = other.initRange.clone();
    	bestMem = other.bestMem.clone();
    	size = other.size;
    	queen = other.queen.clone();
    }

    public TribesSwarm clone() {
    	return new TribesSwarm(this);
    }

    public Population toPopulation() {
    	Population pop = new Population(numExplorers());
        for (int n = 0; n < tribeNb; n++) {
        	for (int i=0; i<tribes[n].explorerNb; i++) pop.add(tribes[n].explorer[i]);
        }
        pop.add(getBestMemory().asDummyExplorer(range, masterTribe.getObjectiveFirstDim()));
        pop.setPopulationSize(pop.size());
    	return pop;
    }

    public TribesMemory getBestMemory() {
    	return bestMem;
    }

    public int getTribeCnt() {
    	return tribeNb;
    }
    public int getProblemDim() {
    	return masterTribe.getProblemDim();
    }

    /**
     * 
     * @param initNb
     * @param swarmInitOption Options: 0 - random, 1 - on the bounds, 2 - sunny spell, 3 - around a center
     * @param rangeInitType initType: for options 0,1: 1 means use initRange, 0 use default range
     * @param prob
     */
    public void generateSwarm(int initNb, int swarmInitOption, int rangeInitType, InterfaceOptimizationProblem prob) {
        /*
          Une seule tribu.
         Les mÃ©moires sont en mÃªme nombre que les exploratrices et aux mÃªmes positions
         */
//        boolean feasible;
//        int d;

        TribesExplorer explorer[] = new TribesExplorer[initNb];

        tribeNb = 0;
        explorerLabel = 0;
        memoryLabel = 0;

        int n;
        for (n = 0; n < initNb; n++) {
            // swarmInitOption -> param option Options: 0 - random, 1 - on the bounds, 2 - sunny spell, 3 - around a center
            // rangeInitType -> initType: for options 0,1: 1 means use initRange, 0 use default range
            explorer[n] = generateExplorer(null, -1, swarmInitOption, -1, rangeInitType, prob, false);
            masterTribe.incEvalCnt();
            // Note : le contact sera dÃ©fini durant la gÃ©nÃ©ration de la tribu

        }
//        masterTribe.incEvalCnt(initNb);
        addTribe(initNb, explorer);
    }

    public void moveSwarm(double[][] range, TribesParam pb,
                         int informOption, InterfaceOptimizationProblem prob) {

        // print("\nmoveSwarm",out);
        int n;
//        int evals=0;

        //Save the Best as previous Best
        bestMem.setPrevPos(bestMem.getPos().clone());

        /* Move each tribe
         Note: sequential method. Slightly more effective than parallel method
         (cf. my book)
         */

        for (n = 0; n < tribeNb; n++) {
//            evals += tribes[n].moveTribe(range, pb, n, this, informOption, prob);
            tribes[n].moveTribe(range, pb, n, this, informOption, prob);
        }
//    	public void moveTribe(double[][] range, TribesParam pb, int fitnessSize, int tribeRank,
//    			TribesSwarm swarm, int informOption, AbstractOptimizationProblem prob) {
        // Find an copy the new Best
        findBest();

        if (Tribes.adaptOption >= 3) {
            updateQueen(range, prob); // this costs an evaluation
//            evals++;

            if (queen.position.firstIsBetter(queen.position.getFitness(),
                                          bestMem.getPos().getFitness())) {

                int[] worst = findWorst();

                // Replace the worst by the queen
                tribes[worst[0]].memory[worst[1]].setPos(queen.position.clone());
                // (Note: the status is not modified)

                //       System.out.print("\nqueen " + Best.position.fitness + " => " +
                //                        queen.position.fitness);

                // Replace the Best by the queen
                bestMem.setPrevPos(bestMem.getPos().clone());
                bestMem.setPos(queen.position.clone());
            }
        }
//        return evals;
    }

    /**
     * Find the best or the worst memory of the swarm, depending on a boolean switch.
     * Return in an Integer array the tribe ID and the rank within the tribe of
     * the found memory.
     *
     * @param switchBest if true, the best memory is returned, else the worst
     * @return an Integer array the tribe ID and the rank within the tribe of the found memory
     */
    public int[] find(boolean switchBest) {
        /* Find and copy the best memory of the swarm
         */
        int n, m;
        int[] found = new int[2];
        found[0] = 0;
        found[1] = 0;
        double[] f = tribes[found[0]].memory[found[1]].getPos().getFitness();
        double[] f2;

        for (n = 0; n < tribeNb; n++) {
            for (m = 0; m < tribes[n].memoryNb; m++) {
//                System.arraycopy(tribes[n].memory[m].position.fitness, 0, f2,
//                                 0, fitnessSize);
            	f2 = tribes[n].memory[m].getPos().getFitness();
            	// this is not XOR! (for multi-objective fitness, betterThan is not symmetrical)
                if ((switchBest && tribes[n].memory[m].getPos().firstIsBetter(f2, f)) 
                	|| (!switchBest && tribes[n].memory[m].getPos().firstIsBetter(f, f2))) {
                    found[0] = n; // tribe
                    found[1] = m; // rank in the tribe (memories)
                    f = f2;	// MK: a pointer should actually be sufficient here (its only read from, never written to f or f2)
//                    for (i = 0; i < fitnessSize; i++) {
//                        f[i] = f2[i];
//                    }
                }
            }
        }
        return found;
    }

    /**
     * Returns al memory particles as double vectors.
     * @return
     */
    public List<TribesPosition> collectMem() {
    	ArrayList<TribesPosition> bestList = new ArrayList<TribesPosition>();
    	
	    for (int n = 0; n < tribeNb; n++) {
	        for (int m = 0; m < tribes[n].memoryNb; m++) {
	        	bestList.add(tribes[n].memory[m].getPos());
	        }
	    }
	    return bestList;
    }
    
    /**
     * This searches for the best memory, and also sets the bestMem member of the swarm.
     *
     * @return an Integer array the tribe ID and the rank within the tribe of the found memory
     * @see find(boolean switchBest)
     */
    public int[] findBest() {
    	int best[] = find(true);

        bestMem = tribes[best[0]].memory[best[1]].clone();
        return best;
//        System.out.println("best was " + bestMem.position.getTotalError());

    }

    /**
     * This searches for the worst memory.
     *
     * @return an Integer array the tribe ID and the rank within the tribe of the found memory
     * @see find(boolean switchBest)
     */
    public int[] findWorst() {
//        // Find the worst shaman of the swarm (its tribe and its rank)
//        int i;
//        int n, m;
//        int[] worst = new int[2];
//        worst[0] = 0;
//        worst[1] = 0;
//        int fitnessSize = tribes[worst[0]].memory[worst[1]].position.fitness.length;
//        double[] f = new double[fitnessSize];
//        double[] f2 = new double[fitnessSize];
//        System.arraycopy(tribes[worst[0]].memory[worst[1]].position.fitness, 0,
//                         f,
//                         0, fitnessSize);
//
//        for (n = 0; n < tribeNb; n++) {
//            for (m = 0; m < tribes[n].memoryNb; m++) {
//                System.arraycopy(tribes[n].memory[m].position.fitness, 0, f2,
//                                 0, fitnessSize);
//                if (tribes[n].memory[m].position.betterThan(f, f2)) {
//                    worst[0] = n;
//                    worst[1] = m;
//                    for (i = 0; i < fitnessSize; i++) {
//                        f[i] = f2[i];
//                    }
//                }
//            }
//        }

    	return find(false);
    }

    public void addTribe(int explorerNb, TribesExplorer explorer[]) {
        /*
         From a list of new particles, build a new tribe
         */
        int n;

        int memoryNb = explorerNb;
        /*
         For the moment, there is just as memories than explorers
         and each explorer is informed by the memory of same rank
         In a future version, I may try to separately adapt the number
         of memories to generate
         */
        // memory memory[] = new memory[Tribes.maxMemoryNb];
        TribesMemory memo[] = new TribesMemory[memoryNb];

        for (n = 0; n < memoryNb; n++) {
            memo[n] = new TribesMemory(explorer[n].position.x.length);
            memo[n].status = 0;
            memo[n].setPos(explorer[n].position.clone());
            memo[n].setPrevPos(memo[n].getPos().clone());
        }

        Tribe t = new Tribe();
        tribes[tribeNb] = t;
        tribes[tribeNb].newTribe(explorerNb, explorer, memoryNb, memo);
        tribeNb = tribeNb + 1;

        findBest();

    }

    public int numExplorers() {
        /*
         Compute the number of explorers
         WARNING: it does NOT compute the number of memories
         */
        int size = 0;
        int n;
        for (n = 0; n < tribeNb; n++) {
            size = size + tribes[n].getNumExplorers();
        }
        return size;
    }

    public int numParticles() {
        int size = 0;
        int n;
        for (n = 0; n < tribeNb; n++) {
            size = size + tribes[n].getNumExplorers() + tribes[n].getNumMemories();
        }
        return size;
    }

    public void setSwarmSize() { /*
        Compute the number of explorers
        WARNING: it does NOT compute the number of memories
        */
    	size = numExplorers();
    }

//    public void displaySwarm(TribesSwarm swarm, out out) {
//        int n;
//        print("\nSWARM", out);
//        for (n = 0; n < swarm.tribeNb; n++) {
//            print("\nTribe " + n, out);
//            swarm.tribes[n].displayTribe(out);
//        }
//
//    }

    /**
     * 
     */
    public void adaptSwarm(int initType, InterfaceOptimizationProblem prob) {
        int centerRank = 0; // Arbitrary value to avoid compiler error
        // "the variable may not have been initialized"
        int particlesToGenerate;
        int gmax = 4;

//        int[] gener = {0,1,2,0}; // For gmax kinds of particles to generate
        int[] gener = {0,1,1,2}; // For gmax kinds of particles to generate
        /*
                 0 => at random anywhere in the search space
                 1 => on bounds
                 2 => in the biggest "no man's land"
                 3 => around the local best (shaman)
         */

        int gOption;
        int gMin;
        int gMode = RNG.randomInt(2); // Random choice; 
        /* gMode:
        0 = according to gener
        1 = first particle (center) in the biggest not yet searched area
             and the other around it
             Only 2 and a variant of strategy 3 (around the center) are then used
        */
       
       int n;
        int explorerNb = 0;
        double radius = -1;
        int shaman;
        int welcomeTribe;
        int worstRank;

        TribesExplorer explorer[] = new TribesExplorer[
                              Tribes.maxExplorerNb];

        for (n = 0; n < tribeNb; n++) {
            switch (tribes[n].status) {
            default: // Do nothing
                break;

            case -1: // Bad tribe => generation

                // Number of particles to generate
                particlesToGenerate = Tribes.particleNb(range.length, tribeNb);
                if (particlesToGenerate < 2) {
                    particlesToGenerate = 2;
                }

                // If too many particles, do nothing in order to avoid memory overflow
                if (explorerNb >= Tribes.maxExplorerNb - particlesToGenerate - 1) {
                    break;
                }

                //Generate some particles
                switch (gMode) {
                default: // According to gener[]
                    shaman = tribes[n].shaman;

                    for (int g = 0; g < particlesToGenerate; g++) {
                        gOption = g % gmax;
                        explorer[explorerNb] = generateExplorer(tribes[n].memory[shaman].getPos(), -1, gener[gOption], n, 0, prob, true);
//                        	explorer[explorerNb].generateExplorer(pb, swarm,
//                                tribes[n].memory[shaman].position, -1,
//                                gener[gOption],
//                                n, 0, evalF);

                        explorerNb++;
                    }
                    break;

                case 1: /* First particle in the biggest not yet searched area
                     and the other around it
                     */
                    if (explorerNb == 0) { // Generate the center in an "empty" area
                        centerRank = 0; //
                        explorer[explorerNb] = generateExplorer(null, -1, 2, n, initType, prob, true);

                        radius = explorer[explorerNb].position.isolation;
                        explorerNb++;
                        gMin = 1;
                    } else {
                        gMin = 0;
                    }

                     //System.out.print("\nadaptSwarm " + explorer[centerRank].position.Dimension);
                     // Generate the other around the center

                     for (int g = gMin; g < particlesToGenerate; g++) {
                         explorer[explorerNb] = generateExplorer(explorer[centerRank].position, radius, 3, n, initType, prob, true);
                         explorerNb++;

                     }
                     break;
                }

                break;

            case 1:

                // Possibly remove a particle
                /*
                 Possible SIMPLIFICATION: this situation occurs quite rarely.
                 So all this part could be easily removed.
                 It would just mean that no explorer would be removed or exilated.
                 */

                // If the tribe has already no explorer anymore, do nothing
                if (tribes[n].explorerNb > 0) {
                    // Look for the worst explorer of the tribe
                    //  worstRank = tribeClass.worstExplorer(tribes[n]);
                    tribes[n].worstExplorer();
                    worstRank = tribes[n].worst;

                    // Check if exile is possible
                    welcomeTribe = migrateCheck(worstRank, n);

                    /*
                     If exile is possible, add the particle to the tribe that accept it
                     */

                    if (welcomeTribe >= 0) {
                        tribes[welcomeTribe].migrateAccept(tribes[n].explorer[worstRank]);
                        //   System.out.print("\n EXIL "+n+ " => "+worstRank);
                    }

                    /*
                     In any case, remove the explorer from the tribe
                     Note: it may then arrive that the contact memory becomes
                      a "dead memory"  (with no link with an explorer).
                     However such a memory is still useful for the "SunnySpell"
                     generation method (in an "empty" area)
                     */

//                    System.out.print("\ntribe " + n + " deletes");
                    tribes[n].deleteExplorer(worstRank);
                }
                break;
            }

        }

        /*
         If some particles have been added, build a new tribe with them
         */

        if (explorerNb > 0) {
            addTribe(explorerNb, explorer);
        }
    }


    public int linkNb(TribesSwarm swarm) {
        /*
         Quickly estimate the number of information links.
         This is NOT a precise computation
         */
        int link = 0;
        int n;
        for (n = 0; n < swarm.tribeNb; n++) {
            // In a tribe the graph is completely connected
            link = link +
                   swarm.tribes[n].explorerNb *
                   swarm.tribes[n].memoryNb;
        }
        // Roughly speaking, each shaman is linked to all the others
        link = link + swarm.tribeNb * (swarm.tribeNb - 1);
        return link;
    }

    public int migrateCheck(int explorerRank,
                            int tribeRank) {
        /* Teste si une exploratrice est "exilable" c'est-Ã -dire cherche une tribu
         dont au moins une mÃ©moire est moins bonne que le contact de l'exploratrice
                 (au sens de valeur de la position).
                 On prend la premiÃ¨re trouvÃ©e
         */
        double f1, f2;
        int k, m;

        f1 = tribes[tribeRank].explorer[explorerRank].position.getTotalError();

        for (m = 0; m < tribeNb; m++) {
            if (m == tribeRank) { // On exclut la tribu d'origine
                continue;
            }
            for (k = 0; k < tribes[m].memoryNb; k++) {
                f2 = tribes[m].memory[k].getPos().getTotalError();
                if (f2 > f1) {
                    return m; // On retourne le rang de la possible tribu d'accueil
                }
            }
        }
        return -1; // Valeur arbitraire, signifiant "Echec"
    }


    private void updateQueen(double[][] range, InterfaceOptimizationProblem prob) {
        int d, t;
        double mTot = 0;
        double w;
        for (d = 0; d < range.length; d++) {
            queen.position.x[d] = 0;
            for (t = 0; t < tribeNb; t++) {
                w = 1 / tribes[t].memory[tribes[t].shaman].getPos().getTotalError();
                /*  All memories
                                 for (m = 0; m < tribes[t].memoryNb; m++)
                                 {
                    mTot++;
                    queen.position.x[d] = queen.position.x[d] +
                                          tribes[t].memory[m].position.x[d];
                                 }
                 */
                // Just shamans

                mTot = mTot + w;
                queen.position.x[d] = queen.position.x[d] +
                                      w * tribes[t].memory[tribes[t].shaman].
                                      getPos().x[d];

            }
        }

        for (d = 0; d < range.length; d++) {
            queen.position.x[d] = queen.position.x[d] / mTot;
        }
        queen.keepIn(range);
//        queen.granul(pb.H);
        prob.evaluate(queen);
        masterTribe.incEvalCnt();
    }

//    public TribesExplorer generateExplorer(TribesPosition center, double radius,
//    		int option, int fromTribe,
//    		int initType, InterfaceOptimizationProblem prob, boolean notify) {
//    	return generateExplorer(range, initRange, center, radius, option, fromTribe, initType, prob, notify);
//    }

    /**
     * 
     * @param center
     * @param radius
     * @param option Options: 0 - random, 1 - on the bounds, 2 - sunny spell, 3 - around a center
     * @param fromTribe
     * @param initType: for options 0,1: 1 means use initRange, 0 use default range
     * @param prob
     * @return
     */
    public TribesExplorer generateExplorer(/*double[][] range, double[][] initRange,*/
    		TribesPosition center, double radius,
    		int option, int fromTribe,
    		int initType, InterfaceOptimizationProblem prob, boolean notify) {
    	/*
         Generation of a new explorer ("scout")
         If fromTribe=-1, this is the very first generation
    	 */

    	TribesExplorer expl = new TribesExplorer(range, masterTribe.getObjectiveFirstDim());
    	expl.SetDoubleRange(range);

//    	System.out.println("generating expl, option " + option + ", init " + initType + ", from tribe " + fromTribe);

    	int d, dmax, dmod;
    	int m;
    	//  int rank;
    	// int shaman;
    	double rho;
    	int D = range.length;
    	TribesPosition posTemp = new TribesPosition(range.length);
    	double[] rand_i;

		if (Tribes.TRACE) System.out.println("+ generateExplorer option " + option);
    	switch (option) {
    	case 3: // around a "center"
    		if (Tribes.TRACE) System.out.println("+ around center ");
    		if (radius < 0) {
    			// Choose at random a memory
    			m = RNG.randomInt(this.tribes[fromTribe].memoryNb);

    			// Compute the distance to the "center" = radius
    			rho = center.distanceTo(this.tribes[fromTribe].memory[m].getPos());
    		} else {
    			rho = radius;
    		}

    		// Define a random point in the hypersphere (center, rho)
    		expl.position.setDoubleArray(RNG.randHypersphere(center.getDoubleArray(), rho, 1.5));

    		// Define another random point
    		rand_i = RNG.randHypersphere(center.getDoubleArray(), rho, 1.5);

    		// Derive a random velocity
    		for (d = 0; d < D; d++) {
    			expl.velocity.x[d] = rand_i[d] - expl.position.x[d];
    		}

    		break;

    	case 2: /* In the biggest "terra incognita" ("no man's land")

             In order to do that all memorizez positions are used, including de "dead" ones
               See SunnySpell
    	 */
    		if (Tribes.TRACE) System.out.println("+ sunny spell ");

    		// if only initRange should be used for initialization, give that one to the sspell
    		expl.position = expl.position.maxIsolated((initRange == null) ? range : initRange, this);

    		// At this point, fitness[0] contains the "isolation" value
    		rand_i = RNG.randHypersphere(expl.position.getDoubleArray(), expl.position.fitness[0], 1.5);
    		for (d = 0; d < D; d++) {
    			expl.velocity.x[d] = rand_i[d] - expl.position.x[d];
    		}

    		break;

    	default:
	    	// For pure random (0) method, or option 1 (on the bounds)
    		if (initType==1) {	// use initRange
        		if (Tribes.TRACE) System.out.println("+ in initRange ");
        		if (initRange == null) System.err.println("unexpected null initRange!");
    			// this allows for a random position plus a random (but valid) last velocity
    			expl.initExplorerSpace(initRange);
    			posTemp = expl.position.clone();
    			expl.initExplorerSpace(initRange);
    		} else {	// use default range
        		if (Tribes.TRACE) System.out.println("+ in whole range ");
    			//default: // In the whole search space
    			expl.initExplorerSpace(range);
    			posTemp = expl.position.clone();
    			expl.initExplorerSpace(range);
    		}
	    	for (d = 0; d < range.length; d++) {
	    		expl.velocity.x[d] = posTemp.x[d] - expl.position.x[d];
	    	} 
	    	if (option == 1) {
	    		/* On the boundary of the search space
	              For some dimensions, set the coordinate to the min or the max
	    		 */
	    		dmax = RNG.randomInt(D); // For a random number of dimensions
	    		//    dmax=D-1; // For all dimensions
	    		for (dmod = 0; dmod <= dmax; dmod++) {
	    			//   m = Tribes.generator.nextInt(2);
	    			//  if(m==0) continue; // With a probability of 1/2

	    			d = RNG.randomInt(D); // 0,1, ... D-1
	    			m = RNG.randomInt(2); // 0 or 1
	    			if ((initRange == null) || (initType == 0)) {
	    				expl.position.x[d] = range[d][m];
	    			} else {
	    				expl.position.x[d] = initRange[d][m];
	    			}
	    			// velocity.v[d] = 0;
	    		}
	    	}
	    	break;

    	}

    	// Complete the explorer
    	prob.evaluate(expl);
    	if (notify) masterTribe.incEvalCnt();
    	/* necessary for initialization when not all explorers have a valid fitness
    	 * to avoid zero fitness plot from population.getBest.
    	 */

    	expl.positionT_1 = expl.position.clone();
    	expl.positionT_2 = expl.position.clone();
    	expl.label = explorerLabel++;

    	expl.status = 5; // At the beginning, neutral status

    	/* By which tribe the explorer is generated. The value of "fromTribe"
         has been arbitrarily set to -1 for the very first generation (initialisation)
    	 */
    	if (fromTribe >= 0) {
    		expl.iGroup[0][0] = fromTribe;
    		expl.iGroup[0][1] = this.tribes[fromTribe].shaman;

    		/* Note : at this point the "contact" (i.e. the memory to update)
                         is not yet defined
    		 */
    	} else {
    		expl.iGroupNb = 0;
    	}
    	return expl;
    }

	public void reinitTribe(int tribeIndex, int initType, InterfaceOptimizationProblem prob) {
		tribes[tribeIndex].reinitTribe(this, prob, initType);
	}
	
	public double[][] getRange() {
		return range;
	}

	public double[][] getInitRange() {
		return initRange;
	}
}
