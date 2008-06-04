package eva2.server.go.strategies.tribes;

import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.individuals.InterfaceESIndividual;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.InterfaceOptimizationProblem;
import eva2.server.go.strategies.Tribes;
import wsi.ra.math.RNG;

public class TribesExplorer extends AbstractEAIndividual implements InterfaceESIndividual, InterfaceDataTypeDouble {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	TribesPosition positionT_2; // Position at time t-2
    TribesPosition positionT_1;  // Position at time t-1
    TribesPosition position;  // Current position
    TribesPosition velocity; 
    double[][] range = null;
    protected double objectiveValueFirstDim = 0.;
    
    // int informant[] = new int[4];
    /*
         informant[0] = tribe rank in the swarm
              1  = memory rank in the tribe of the best informant
              2  = tribe rank ...
              3     and memory rank of the external best informant

     */
    double[] confCoeff = new double[2]; // For adaptive confidence coefficient(s) strategy
    int contact; // MÃ©moire "affectÃ©e" Ã  l'exploratrice
    int iGroup[][]; // = new int[Tribes.maxTribeNb][2];
    /* Informants:
         iGroup[t][0] = rank of the tribe in the swarm
         iGroup[t][1] = rank of the memory in the tribe
     */
    int iGroupNb;
    int strategy; // Moving strategy
    int status; // 1 to 9
    int label;
    
    public TribesExplorer(double[][] range, double objFirstDim) {
    	init(range.length, Tribes.maxTribeNb);
    	SetDoubleRange(range);
    	objectiveValueFirstDim = objFirstDim;
    }
    
    public TribesExplorer(TribesExplorer expl) {
    	this(expl.getDoubleRange(), expl.objectiveValueFirstDim);
        int n;
        for (n = 0; n < 2; n++) {
            // Clone.informant[n] = explorer.informant[n];
            confCoeff[n] = expl.confCoeff[n];
        }
        contact = expl.contact;
        position = expl.position.clone();
        positionT_2 = (expl.positionT_2 == null) ? null : expl.positionT_2.clone();
        positionT_1 = (expl.positionT_1 == null) ? null : expl.positionT_1.clone();
        velocity = expl.velocity.clone();
        strategy = expl.strategy;
        status = expl.status;
        iGroupNb = expl.iGroupNb;
        for (n = 0; n < expl.iGroupNb; n++) {
            iGroup[n][0] = expl.iGroup[n][0];
            iGroup[n][0] = expl.iGroup[n][0];
        }
    }
    
    public void init(int maxDimension, int maxTribeNb) {
    	initPositions(maxDimension);
    	iGroup = new int[maxTribeNb][2];
    }

	protected void initPositions(int maxDimension) {
		positionT_2 = new TribesPosition(maxDimension); // Position at time t-2
    	positionT_1 = new TribesPosition(maxDimension); // Position at time t-1
    	position = new TribesPosition(maxDimension); // Current position
    	velocity = new TribesPosition(maxDimension);
	}
//    private void print(String string, out output) {
//        output.out.append(string);
//    }
    
    public double[] getFitness() {
    	return position.getFitness();
    }
    
    public double getFitness(int index) {
    	return position.getFitness()[index];
    }
    
    /**
     * Be aware that for a TribesExplorer, an objective value might be taken into account
     * by reducing the fitness (in the first dimension).
     */
    public void SetFitness(double[] fitness) {
    	position.fitness = fitness;
    	super.SetFitness(fitness);
    	fitness[0] -= objectiveValueFirstDim;
    	position.setTotalError();
    }
    
    /**
     * Be aware that for a TribesExplorer, an objective value might be taken into account
     * by reducing the fitness (in the first dimension).
     */
    public void SetFitness(int index, double fitness) {
    	super.SetFitness(index, fitness);
    	if (index > position.fitness.length) {
    		double[] newFit = new double[index+1];
    		System.arraycopy(position.fitness, 0, newFit, 0, position.fitness.length);
    		position.fitness = newFit; 
    	}
    	if (index == 0) { 
    		position.fitness[index] = (fitness-objectiveValueFirstDim);
    	} else {
    		position.fitness[index] = fitness;
    	}
    	position.setTotalError();
    }

    public TribesExplorer clone() {
    	return new TribesExplorer(this);
    }
    
    /**
     * Resets all positions and velocity.
     */
    public void clearPosVel() {
    	initPositions(position.getMaxDimension());
    }
    
//
//    public int generateExplorer(TribesParam pb,
//    		TribesSwarm swarm, TribesPosition center, double radius,
//    		int option, int fromTribe,
//    		int initType, int eval, int label) {
//    	/*
//         Generation of a new explorer ("scout")
//         If fromTribe=-1, this is the very first generation
//    	 */
//
//    	TribesExplorer expl = this;
//    	
//    	int d, dmax, dmod;
//    	int evalF = eval;
//    	int m;
//    	//  int rank;
//    	// int shaman;
//    	double rho;
//    	int D = pb.H.Dimension;
//    	TribesPosition posTemp = new TribesPosition(Tribes.maxDimension, Tribes.maxFunctionNb);
//    	double[] rand_i;
//    	expl.velocity.Dimension = D;
//
//    	switch (option) {
//    	case 3: // around a "center"
//    		if (radius < 0) {
//    			// Choose at random a memory
//    			m = RNG.randomInt(swarm.tribes[fromTribe].memoryNb);
//
//    			// Compute the distance to the "center" = radius
//    			rho = center.distanceTo(swarm.tribes[fromTribe].memory[m].position);
//    		} else {
//    			rho = radius;
//    		}
//
//    		// Define a random point in the hypersphere (center, rho)
//    		expl.position.setDoubleArray(RNG.randHypersphere(center.getDoubleArray(), rho, 1.5));
//
//    		// Define another random point
//    		rand_i = RNG.randHypersphere(center.getDoubleArray(), rho, 1.5);
//
//    		// Derive a random velocity
//    		for (d = 0; d < D; d++) {
//    			expl.velocity.x[d] = rand_i[d] - expl.position.x[d];
//    		}
//
//    		break;
//
//    	case 2: /* In the biggest "terra incognita" ("no man's land")
//
//             In order to do that all memorizez positions are used, including de "dead" ones
//               See SunnySpell
//    	 */
//
//    		expl.position = expl.position.maxIsolated(pb.H, swarm);
//
//    		// At this point, fitness[0] contains the "isolation" value
//    		rand_i = RNG.randHypersphere(expl.position.getDoubleArray(), expl.position.fitness[0], 1.5);
//    		for (d = 0; d < D; d++) {
//    			expl.velocity.x[d] = rand_i[d] - expl.position.x[d];
//    		}
//
//    		break;
//
//    	default: // For pure random (0) method, or first step of option 1 (on the bounds)
//    		if (initType==1) {
//    			initExplorerSpace(pb.H.Dimension, pb.H.xInitMin, pb.H.xInitMax);
//    			posTemp = expl.position.clone();
//    			initExplorerSpace(pb.H.Dimension, pb.H.xInitMin, pb.H.xInitMax);
//
//    			for (d = 0; d < pb.H.Dimension; d++) {
//    				expl.velocity.x[d] = posTemp.x[d] - expl.position.x[d];
//    			}
//    		} else {
//    			//default: // In the whole search space
//    			initExplorerSpace(pb.H.Dimension, pb.H.xMin, pb.H.xMax);
//    			posTemp = expl.position.clone();
//    			initExplorerSpace(pb.H.Dimension, pb.H.xMin, pb.H.xMax);
//
//    			for (d = 0; d < pb.H.Dimension; d++) {
//    				expl.velocity.x[d] = posTemp.x[d] - expl.position.x[d];
//    			}
//    		}
//    	
//    		break;
//
//    	}
//
//    	if (option == 1) {
//    		/* On the boundary of the search space
//              For some dimensions, set the coordinate to the min or the max
//    		 */
//    		dmax = RNG.randomInt(D); // For a random number of dimensions
//    		//    dmax=D-1; // For all dimensions
//    		for (dmod = 0; dmod <= dmax; dmod++) {
//    			//   m = Tribes.generator.nextInt(2);
//    			//  if(m==0) continue; // With a probability of 1/2
//
//    			d = RNG.randomInt(D); // 0,1, ... D-1
//    			m = RNG.randomInt(2); // 0 or 1
//    			if (m == 0) {
//    				expl.position.x[d] = pb.H.xMin[d];
//    			} else {
//    				expl.position.x[d] = pb.H.xMax[d];
//    			}
//    			// velocity.v[d] = 0;
//    		}
//    	}
//
//    	// Complete the explorer
//    	evalF = expl.position.fitnessEval(pb.function, pb.objective, pb.fitnessSize,
//    			evalF);
//    	positionT_1 = expl.position.clone();
//    	positionT_2 = expl.position.clone();
//    	expl.label = label;
//
//    	status = 5; // At the beginning, neutral status
//
//    	/* By which tribe the explorer is generated. The value of "fromTribe"
//         has been arbitrarily set to -1 for the very first generation (initialisation)
//    	 */
//    	if (fromTribe >= 0) {
//    		iGroup[0][0] = fromTribe;
//    		iGroup[0][1] = swarm.tribes[fromTribe].shaman;
//
//    		/* Note : at this point the "contact" (i.e. the memory to update)
//                         is not yet defined
//    		 */
//    	} else {
//    		iGroupNb = 0;
//    	}
//    	return evalF;
//    }

    public void informExplorer(int fromTribe, int rank, TribesSwarm swarm,
                               int informOption) {
        /* Look for the best informant of the explorer
         informOption=-1 => the "true" best
                     = 1 => depending on pseudo-gradient (for niching)
         For the shaman, look for the other shamans
         */

        int m, n;
//        double f1, f2;
//        int best;
        int tribeRank = fromTribe;
//        double dist;
//        double pseudoGradient, pseudoGradientBest;

        switch (informOption) {
        default:
            System.err.println("moveExplorer error");
            break;
        case -1: // The "true" best informant

            /* In the tribe it should be the shaman
             */

            iGroup[0][0] = tribeRank;
            iGroup[0][1] = swarm.tribes[tribeRank].shaman;

            /*
             System.out.print("\ninformExplorer " + iGroup[0][0]+ " " +
                                          iGroup[0][1] + " ");
             */
//For the shaman, links to the other shamans
            m = 1;
            for (n = 0; n < swarm.tribeNb; n++) {
                if (n == fromTribe) {
                    continue;
                }
                iGroup[m][0] = n;
                iGroup[m][1] = swarm.tribes[n].shaman;
                m++;
            }

            break;
            /*
                    case 1: // Depending on pseudo-gradient
                        f1 = position.totalError;
                        best = 0;
                        pseudoGradientBest = 0;

// In the tribe search

             for (m = 1; m < swarm.tribes[tribeRank].memoryNb; m++) {
             f2 = swarm.tribes[tribeRank].memory[m].position.totalError;
                            dist = tools.distance(position,
             swarm.tribes[tribeRank].memory[m].
                                                  position);
                            if (dist > 0) {
                                pseudoGradient = (f1 - f2) / dist;
                                if (pseudoGradient > pseudoGradientBest) {
                                    pseudoGradientBest = pseudoGradient;
                                    best = m;
                                }

                            }
                        }

// Out of the tribe search
                        if (iGroupNb > 0) {
                            for (m = 0; m < iGroupNb; m++) {
                                tribeRank = iGroup[m][0];
                                n = iGroup[m][1];
             f2 = swarm.tribes[tribeRank].memory[n].position.totalError;
                                dist = tools.distance(position,
             swarm.tribes[tribeRank].memory[n].
                                                      position);
                                if (dist > 0) {
                                    pseudoGradient = (f1 - f2) / dist;
                                    if (pseudoGradient > pseudoGradientBest) {
                                        pseudoGradientBest = pseudoGradient;
                                        best = m;
                                    }
                                }
                            }
                        }
                        informant[0] = tribeRank; // Which tribe
                        informant[1] = best; // Which memory in the tribe

                        break;
             */
        }
    }


    public boolean moveExplorer(int fromTribe, int rank, TribesSwarm swarm,
                            int informOption, InterfaceOptimizationProblem prob) {
        int i;
        boolean fitnessEval;
        int n;
        boolean outH = false;

        // Compute the status
        statusExplorer();

        // Define which strategy to use
        int statusTribe;
        if (fromTribe >= 0) {
            statusTribe = swarm.tribes[fromTribe].status;
        } else {
            statusTribe = -1; // Arbitrary value for the very first tribe
        }
        strategy(strategy, statusTribe);

// Redefine the best informant (cf informant[])
        informExplorer(fromTribe, rank, swarm, informOption);//informOption -1 : normal neighbourhood, 1 : niching

        // Shift the memorized positions
        positionT_2 = positionT_1;
        positionT_1 = position;

        // Update velocity
        /*
                 velocity.changeVelocity(position,
                                swarm.tribes[fromTribe].memory[contact].
                                position,
                                swarm.tribes[iGroup[0][0]].memory[
                                iGroup[0][1]].position, strategy, confCoeff);
         */
        changeVelocity(velocity, fromTribe, swarm, strategy, confCoeff);

        // May use repelling
        if (Tribes.repel && (swarm.tribes[fromTribe].explorerNb > 2) &&
            (rank == swarm.tribes[fromTribe].worst)) {
            for (i = 0; i < velocity.x.length; i++) {
                velocity.x[i] = -velocity.x[i];
            }
  //System.out.print("\n repelling");
        }

        //  Update position
        for (i = 0; i < position.x.length; i++) {
            position.x[i] = position.x[i] + velocity.x[i];
        }

        if (swarm.masterTribe.isCheckConstraints()) { // Keep inside the search space ...
            keepIn(range);
        } else { // ... or just modify the velocity and use later a penalty method
            outH = false;
            for (i = 0; i < position.x.length; i++) {
                if ((position.x[i] < range[i][0]) || (position.x[i] > range[i][1])) {
                    outH = true;
                    velocity.x[i] = -velocity.x[i];
                    //velocity.v[d] = 0;
                }
            }
        }

        // Take granularity into account
        //granul();// TODO or not? granularity?

        // Check if fitness has to be re-evaluated
        fitnessEval = true;
        if (Tribes.blind > 0 && strategy == 6) {
            if (RNG.randomDouble() < Tribes.blind) {
                fitnessEval = false;
                //System.out.print("\n no fitness eval");
            }
        }

        // Re-evaluate the fitness
        //    System.out.print("\n fitness before move "+movedExplorer.position.fitness);
        if (fitnessEval) {
            if (swarm.masterTribe.isCheckConstraints() || !outH) { // certainly within range
            	prob.evaluate(this);
            	swarm.masterTribe.incEvalCnt();
//                evalF = position.fitnessEval(pb.function, pb.objective,
//                                             pb.fitnessSize, eval);
            } else { // Artificial fitness by using penalties
                for (n = 0; n < position.fitness.length; n++) {
                	SetFitness(n, swarm.tribes[fromTribe].memory[
                                          contact].
                                          getPos().
                                          fitness[n] +
                                          keepInPenalty(range, position));
                }
                // position.totalError(position.fitness); // MK: this wont actually do anything
            }
        }
//        System.out.println("after move: "+getStringRepresentation());
        return fitnessEval;
    }

    public void statusExplorer() {
        double delta1, delta2;
        int d1, d2;

        /* Compute the status according to the recent past
              Note that we are trying to minimize:
              delta>0 => improvement (noted +)
              =0 => statu quo (noted =)
              <0 => deterioration (noted -)
              9 statuses
              1  2  3     4  5     6  7  8      9
              -- =- +-    -= ==    += -+ =+     ++

         */
        // delta1 = explorer.positionT_1.fitness - explorer.position.fitness;
        // delta2 = explorer.positionT_2.fitness - explorer.positionT_1.fitness;
        delta1 = positionT_1.getTotalError() - position.getTotalError();
        delta2 = positionT_2.getTotalError() - positionT_1.getTotalError();
        if (delta1 < 0) {
            d1 = 0;
        } else if (delta1 > 0) {
            d1 = 6;
        } else {
            d1 = 3;
        }
        if (delta2 < 0) {
            d2 = 1;
        } else if (delta2 > 0) {
            d2 = 3;
        } else {
            d2 = 2;
        }
        status = d1 + d2;

        Tribes.status[status - 1]++; // Just for information

    }


    public void strategy(int strategyCurrent, int statusTribe) {
        strategy = strategyCurrent;

        /* Strategy groups. Cf changeVelocity for details
         */

        switch (status) {
        default:

            //  strategy = RNG.randomInt(4); // 0 to 3
            strategy = 1 + RNG.randomInt(3); // 1 to 3

        case 1:
            strategy = 1 + RNG.randomInt(3);
            break;
        case 2:
            strategy = 1 + RNG.randomInt(3);
            break;
        case 3:
            strategy = 1 + RNG.randomInt(3);
            break;

        case 4:
            strategy = 1 + RNG.randomInt(3);
            break;
        case 5:
            strategy = 1 + RNG.randomInt(3);
            break;

        case 6:

            break;

        case 7:
            if (statusTribe == 1) {
                strategy = RNG.randomInt(2);
            }
            break;
        case 8:
            if (statusTribe == 1) {
                strategy = RNG.randomInt(2);
            }
            break;

        case 9:
            if (statusTribe == 1) {
                strategy = RNG.randomInt(2);
            } else if (Tribes.blind > 0) {
                strategy = 6; // Keep moving the same way
            } else {
                //strategy = 4; // Quasi-gradient (questionable)
                strategy = 5; // Adaptive coefficients
            }
            break;
        }
        /*
                // Bias in favor of strategy 5
                if (Tribes.generator.nextInt(2) == 0) {
                    strategy = 5;
                }
         */
        /*
               // TEST Sometimes strategy 7 (velocity+hyperspheres
         if (Tribes.generator.nextInt(2) == 0 && status<=3) {
           strategy = 7;
         }
         */
//strategy=9; //***  TEST non adaptive parametric strategy (cf. velocitClass)

         // For information. Will be displayed
         Tribes.strategies[strategy - 1]++;
    }

//    public void displayExplorer(out out) {
//        int d;
//        print("\ncontact " + this.contact + " Status " +
//              status, out);
//        print("\ntotalError " + this.position.totalError, out);
//        print("\nposition ", out);
//        for (d = 0; d < this.position.Dimension; d++) {
//            print(this.position.x[d] + " ", out);
//        }
//    }

//    public void initExplorerSpace(int D, double[] xmin, double[] xmax) {
//        // Random initialisation of position
//        int d;
//        double r;
//        position.Dimension = D;
//
//        for (d = 0; d < D; d++) {
//            r = RNG.randomDouble();
//            position.x[d] = xmin[d] + (xmax[d] - xmin[d]) * r;
//        }
//
//        // Confidence coefficients for some strategies
//        r = 1 / (2 * Math.log(2));
//        confCoeff[0] = r;
//        confCoeff[1] = (r + 1) * (r + 2) / 4; // Mean value (Cf. "Stagnation Analysis")
//
//    }
    public void initExplorerSpace(double[][] range) {
        // Random initialisation of position

        double r;

        for (int i = 0; i < range.length; i++) {
            r = RNG.randomDouble();
            position.x[i] = range[i][0] + (range[i][1] - range[i][0]) * r;
        }

        // Confidence coefficients for some strategies
        r = 1 / (2 * Math.log(2));
        confCoeff[0] = r;
        confCoeff[1] = (r + 1) * (r + 2) / 4; // Mean value (Cf. "Stagnation Analysis")

    }
    private double keepInPenalty(double[][] range, TribesPosition pos) {
        int d;
        double penalty = 0;
        for (d = 0; d < pos.x.length; d++) {
            if (pos.x[d] < range[d][0]) {
                penalty = penalty + range[d][0] - pos.x[d];
            }
            if (pos.x[d] > range[d][1]) {
                penalty = penalty - range[d][1] + pos.x[d];
            }
        }
        return penalty;
    }

    public void confCoeffUpdate(double c) { // Cf. "Stagnation Analysis"
        double c1New = 0;
        double cMin = 0;
        double cMax = 1;
        if (status <= 3) {
            c1New = cMin + (c - cMin) / 2;
        }
        if (status >= 7) {
            c1New = c + (cMax - c) / 2;
        }
        confCoeff[0] = c1New;
        confCoeff[1] = (c1New + 1) * (c1New + 2) / 4;

        /*
                 double z=1/(2*Math.log(2));
                 double cmin=(z+1)/2;
                 double cmax=(z+1)*(z+1)/2;
                 double c2New=0;

              if(status<=3) {
                  c2New=cmin+(c-cmin)/2;
              }
              if(status>=7) {
                  c2New=c+(cmax-c)/2;
              }

              confCoeff[1]=c2New;
         */
    }

    public void keepIn(double[][] range) {
        int d;
        double alpha = 0; // Usual value: 0
        for (d = 0; d < range.length; d++) {
            if (position.x[d] < range[d][0]) {
                position.x[d] = range[d][0];
                velocity.x[d] = alpha * velocity.x[d];
            } else if (position.x[d] > range[d][1]) {
                position.x[d] = range[d][1];
                velocity.x[d] = alpha * velocity.x[d];
            }
        }

    }

//    int gNb; // Number of g constraints  (<=0)
//    int hNb; // Number of h constraints (<ups)
//    double ups; // Tolerance for h constraints
    public void constraint(int gNb, int hNb, double ups) {
        int d;
        boolean back = false;

        for (d = 0; d < gNb; d++) {
            if (position.fitness[1 + d] > 0) {
                back = true;
            }
        }
        for (d = 0; d < hNb; d++) {
            if (Math.abs(position.fitness[1 + gNb + d]) > ups) {
                back = true;
            }
        }

        if (back) {
            for (d = 0; d < position.x.length; d++) {
                position.x[d] = position.x[d] - velocity.x[d];
                velocity.x[d] = velocity.x[d] / 2;
            }
        }

    }

//    public void granul(TribesSearchSpace H) { // TODO what about granularity?
//        int d;
//        for (d = 0; d < H.Dimension; d++) {
//            if (H.granularity[d] > 0) {
//                position.x[d] = TribesSearchSpace.regranul(position.x[d], H.granularity[d]);
//            }
//        }
//    }

    public void changeVelocity(TribesPosition velocity, int fromTribe, TribesSwarm swarm,
    		int strategy, double confCoeff[]) {

//  	System.out.println("\n changeVelocity");
    	double c, c0, c1, c2, c3;

    	int d;
    	double dx;
    	double f1, f2;
    	double lambda = 1;

    	double r, r1, r2;

    	int rankMem, rankTribe;
    	double rho, rho2, rho3;

    	double gradient;
    	double[] rand_i, rand_g;

    	double z;
    	TribesPosition pos1 = this.position;
    	TribesPosition pos2 = swarm.tribes[fromTribe].memory[this.contact].getPos();
    	rankTribe = this.iGroup[0][0];
    	rankMem = this.iGroup[0][1];
    	TribesPosition pos3 = swarm.tribes[rankTribe].memory[rankMem].getPos();

    	switch (strategy) {
    	case 0: // Around the best informant
    		rho = pos1.distanceTo(pos2);
    		rand_i = RNG.randHypersphere(pos2.getDoubleArray(), rho, 1.5);

    		for (d = 0; d < velocity.x.length; d++) {
    			velocity.x[d] = rand_i[d]-pos1.x[d];
    		}
    		break;

    	case 2: // Like 1 (cf. below) + Gaussian noise

    		c0 = (pos2.getTotalError() - pos1.getTotalError()) /
    		(pos2.getTotalError() + pos1.getTotalError());

    		lambda = 1 + RNG.gaussianDouble(c0);

//  		NO BREAK HERE. Continue with case 1

    	case 1: // Pivot and hyperspheres
    		rho = pos2.distanceTo(pos3);
    		rho2 = rho;
    		rho3 = rho;

    		rand_i = RNG.randHypersphere(pos2.getDoubleArray(), rho2, 0);
    		rand_g = RNG.randHypersphere(pos3.getDoubleArray(), rho3, 0);

    		c1 = 1 / pos2.getTotalError();
    		c2 = 1 / pos3.getTotalError();
//    		if (Double.isInfinite(c1)) c1 = Double.MIN_VALUE;
//    		if (Double.isInfinite(c2)) c2 = Double.MIN_VALUE;
    		c3 = c1 + c2;
    		c1 = c1 / c3;
    		c2 = c2 / c3;

    		for (d = 0; d < velocity.x.length; d++) {
    			dx = c1 * rand_i[d] + c2 * rand_g[d];
    			velocity.x[d] = dx - pos1.x[d];
    		}

    		break;

    	case 3: //  Independent Gaussians
    		for (d = 0; d < velocity.x.length; d++) {
    			rho2 = Math.abs(pos3.x[d] - pos2.x[d]);
    			r = RNG.gaussianDouble(rho2);
    			velocity.x[d] = pos3.x[d] + r - pos1.x[d];
    		}

    		break;

    	case 4: // Quasi-gradient (questionable)

    		f1 = pos1.getTotalError();
    		f2 = pos3.getTotalError();
    		gradient=( -f1 / (f2 - f1)) ;

    		for (d = 0; d < velocity.x.length; d++) {
    			if (f2 == f1) {
    				velocity.x[d]=0;
    			} else {
    				velocity.x[d] = gradient*(pos3.x[d] - pos1.x[d]);
    			}
    		}
    		break;

    	case 5: // Adaptive confidence coefficients
    		c1 = confCoeff[0];
    		c = confCoeff[1];

    		for (d = 0; d < velocity.x.length; d++) {
    			r1 = RNG.randomDouble(); // Sur [0,1]
    			r2 = RNG.randomDouble();
    			velocity.x[d] = c1 * velocity.x[d] +
    			r1 * c *
    			(pos2.x[d] - pos1.x[d]) +
    			r2 * c *
    			(pos3.x[d] - pos1.x[d]);
    		}

    		break;

    	case 6: // Keep moving the same way, with a smaller velocity (quasi-gradient)
    		r = RNG.randomDouble() / 2;
    		for (d = 0; d < velocity.x.length; d++) {
    			velocity.x[d] = r * velocity.x[d];
    		}
    		break;

    	case 7: /* Velocity and hyperspheres
Like Pivot and hyperspheres, but a velocity component is added
   Questionable.
    	 */
    		z = 2 * Math.log(2);
    		c1 = 1 / z;

//  		c1 = c1 * (1 - pos3.fitness[0] / pos2.fitness[0]);

    		rho = pos2.distanceTo(pos3);
    		rho2 = rho;
    		rho3 = rho;

    		rand_i = RNG.randHypersphere(pos2.getDoubleArray(), rho2, 0);
    		rand_g = RNG.randHypersphere(pos3.getDoubleArray(), rho3, 0);

    		c2 = 1 / pos2.getTotalError();
    		c3 = 1 / pos3.getTotalError();
    		c = c2 + c3;
    		c2 = c2 / c;
    		c3 = c3 / c;

    		for (d = 0; d < velocity.x.length; d++) {
    			dx = c2 * rand_i[d] + c3 * rand_g[d];
    			velocity.x[d] = c1 * velocity.x[d] + dx - pos1.x[d];
    		}

    		break;

    	case 9:

    		/*
"Classical" parametric method for tests (cf. my paper "Stagnation Analysis")
    		 */
    		z = 2 * Math.log(2);
    		c1 = 1 / z;
    		c = (c1 + 1) * (c1 + 2) / 4;

    		for (d = 0; d < velocity.x.length; d++) {
    			r1 = RNG.randomDouble(); // Sur [0,1]
    			r2 = RNG.randomDouble();
    			velocity.x[d] = c1 * velocity.x[d] +
    			r1 * c *
    			(pos2.x[d] - pos1.x[d]) +
    			r2 * c *
    			(pos3.x[d] - pos1.x[d]);
    		}

    		break;
    	}

    	if (strategy == 2) { // Add Gaussian noise
    		for (d = 0; d < velocity.x.length; d++) {
    			velocity.x[d] = velocity.x[d] * lambda;
    		}
    	}

//  	Below are some TESTS ...
    	/*
// Add repulsions for the shaman
if(rankMem==swarm.tribes[rankTribe].shaman) {
for(m=1;m<swarm.tribeNb;m++) { // For each other shaman S
  rankTribe=explorer.iGroup[m][0];
rankMem=explorer.iGroup[m][1];
pos2=swarm.tribes[rankTribe].memory[rankMem].position;
  r = tools.distance(pos1, pos2); // distance to S
  repuls=Math.pow(r,-(Dimension-1))/r;
  repuls=Math.pow(r,-1)/r;
//System.out.print("\n velocity, tribe, r, repuls "+rankTribe+" "+r+" "+repuls);
  for(d=0;d<Dimension;d++){
      v[d]=v[d]+(pos1.x[d]-pos2.x[d])*repuls;
  }
}
}
    	 */
    	/*
// Confinement for the shaman
if (rankMem == swarm.tribes[rankTribe].shaman) {
cmin = 1;
for (m = 1; m < swarm.tribeNb; m++) {
rankTribe = explorer.iGroup[m][0];
rankMem = explorer.iGroup[m][1];
pos2 = swarm.tribes[rankTribe].memory[rankMem].position;
r = tools.distance(pos1, pos2); // distance to S
r1 = 0; // Scalar product
for (d = 0; d < Dimension; d++) {
 r1 = r1 + (pos2.x[d] - pos1.x[d]) * v[d];
}

// Confinement for the current shamam
if (r1 > r ) {
 c = r / r1;
} else {
 c = 1;
}
// Max confinement (i.e. min coefficient)
if (c < cmin) {
 cmin = c;
}
}
//System.out.print("\n velocity, cmin "+cmin);
for (d = 0; d < Dimension; d++) {
v[d] = cmin * v[d];
}
}
    	 */

    }

	@Override
	public boolean equalGenotypes(AbstractEAIndividual individual) {
		return (position.distanceTo(((TribesExplorer)individual).position) == 0.);
	}

	@Override
	public String getStringRepresentation() {
		StringBuffer sb = new StringBuffer();
		sb.append("TribesExplorer [");
		for (int i=0; i<position.x.length; i++) {
			sb.append(position.x[i]);
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void init(InterfaceOptimizationProblem opt) {
		// TODO whats this for?
        for (int i = 0; i < this.position.x.length; i++) {
            this.position.x[0] = 0.;
        }
	}

	@Override
	public void initByValue(Object obj, InterfaceOptimizationProblem opt) {
        if (obj instanceof double[]) {
            double[]  x = (double[]) obj;
            if (x.length != position.x.length) System.err.println("Init value and requested length doesn't match!");
            this.SetDoubleDataLamarckian(x);
        } else {
            this.init(opt);
            System.err.println("Initial value for ESIndividualDoubleData is not double[]!");
        }
	}

	@Override
	public AbstractEAIndividual[] mateWith(Population partners) {
		System.err.println("TRIBES: mating is not available!");
		return null;
	}

	@Override
	public void mutate() {
		System.err.println("TRIBES: mutation is not available!");
	}

	public void SetDoubleData(double[] doubleData) {
		position.setDoubleArray(doubleData);
	}

	public void SetDoubleDataLamarckian(double[] doubleData) {
		position.setDoubleArray(doubleData);
	}

	public void SetDoubleRange(double[][] range) {
	    if (position.x.length != range.length) {	// we will need to fully reinit the particle
	    	initPositions(range.length);
	    }
	    this.range = range;
	}

	public double[] getDoubleData() {
		return position.x;
	}
	
	public double[] getVelocity() {
		return velocity.x;
	}		

	public double[] getDoubleDataWithoutUpdate() {
		return position.x;
	}

	public double[][] getDoubleRange() {
		return range;
	}

	public void setDoubleDataLength(int length) {
	    if (position.x.length != length) {	// we will need to fully reinit the particle
	    	initPositions(length);
	    }
	}

	public int size() {
		return position.x.length;
	}

	public void SetDGenotype(double[] b) {
		position.setDoubleArray(b);
	}

	public void defaultInit() {
		System.err.println("defaultInit not available for TribesExplorer!");
	}

	public void defaultMutate() {
		System.err.println("defaultMutate not available for TribesExplorer!");
	}

	public double[] getDGenotype() {
		return position.getDoubleArray().clone();
	}
}
