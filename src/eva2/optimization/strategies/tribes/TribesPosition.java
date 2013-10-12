package eva2.optimization.strategies.tribes;

import eva2.optimization.strategies.Tribes;
import eva2.tools.math.RNG;

import java.util.Arrays;


public class TribesPosition implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean TRACE = false;
    double x[];
    int[][] maxIsoLink = null;
    double[] fitness;
    private double totalError;
    double isolation;

    public TribesPosition(int maxDimension) {
        x = new double[maxDimension];
        fitness = new double[1];// new double[maxFunctionNb]; // TODO
    }

//    public TribesPosition clone(TribesPosition position, int fitnessSize) {
//    	// TODO this method might better vanish
//        int n;
//        // Pour remplacer "implements Cloneable"
//        TribesPosition Clone = new TribesPosition(x.length, fitness.length);
//        Clone.Dimension = position.Dimension;
//        System.arraycopy(position.x, 0, Clone.x, 0, position.Dimension);
//        for (n = 0; n < fitnessSize; n++) {
//            Clone.fitness[n] = position.fitness[n];
//        }
//
//        Clone.totalError = position.totalError;
//        Clone.isolation=position.isolation;
//        return Clone;
//    }

    @Override
    public TribesPosition clone() {
        // Pour remplacer "implements Cloneable"
        TribesPosition Clone = new TribesPosition(x.length);
        System.arraycopy(x, 0, Clone.x, 0, x.length);

        Clone.fitness = fitness.clone();

        Clone.totalError = totalError;
        Clone.isolation = isolation;
        return Clone;
    }

    public double[] getDoubleArray() {
        return x;
    }

    /**
     * This one makes a deep copy.
     *
     * @param vals
     */
    public void setDoubleArray(double[] vals) {
        x = vals.clone();
    }

    /**
     * This one makes a shallow copy.
     *
     * @param fit
     */
    public void setFitness(double[] fit) {
        fitness = fit;
    }

    public int getMaxDimension() {
        return x.length;
    }

    public double[] getFitness() {
        return fitness;
    }

    public double[] getPos() {
        return x;
    }

    public double distanceTo(TribesPosition pos) {
        double di, dist = 0;
        int d;

        for (d = 0; d < x.length; d++) {
            di = x[d] - pos.x[d];
            dist += (di * di);
        }
        dist = Math.sqrt(dist);
        return dist;
    }
//    public boolean betterThan(double[] f1, double[] f2,int fitnessSize) {
//        // Return "true" if f1 better than f2
//        int n;
//        if (!Tribes.testBC) { // Multiobjective approach
//            for (n = 0; n < fitnessSize; n++) {
//                if (f1[n] > f2[n]) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        else { // Total error approach
//            if(totalError(f1,fitnessSize)<totalError(f2,fitnessSize)) return true;
//            return false;
//        }
//    }
//
//
////  =================================================================== FITNESS EVALUATION
//    public int fitnessEval(int[] codeFunction, double objective,
//    		int fitnessSize,int eval) {
//
//    	//Tribes.eval++; // IncrÃ©mente le nombre total d'Ã©valuations
//    	int n;
//    	double r;
//
//    	switch (codeFunction[0]) {
//    	default:
//    		fitness[0] = Tribes.infinity; // En fait, c'est une erreur Ã  traiter !
//    	break;
//    	case 0:
//    		fitness[0] = alpine();
//
//    		break;
//
//    	case 1:
//    		fitness[0] = parabola();
//    		break;
//
//    	case 2:
//    		fitness[0] = griewank();
//    		break;
//
//    	case 3:
//    		fitness[0] = rosenbrock();
//    		break;
//
//    	case 4:
//    		fitness[0] = rastrigin();
//    		break;
//
//    	case 5:
//    		fitness[0] = ackley();
//    		break;
//
//    	case 6:
//    		fitness[0] = tripod();
//    		break;
//
//    	case 7:
//    		fitness[0] = pressureVessel();
//    		break;
//
//    	case 8:
//    		fitness[0] = compressionSpring();
//    		break;
//
//    	case 9:
//    		fitness[0] = product();
//    		break;
//
//    	case 10:
//    		fitness[0] = sin_sin2_3();
//    		break;
//
//    	case -1: // Benchmark. Sphere (Parabola)
//    	fitness[0] = benchmark.sphere(Dimension, x);
//    	break;
//
//    	case -2: // Benchmark. Schwefel
//    	fitness[0] = benchmark.schwefel_1_2(Dimension, x);
//    	break;
//
//    	case -3: // Benchmark. Elliptic
//    		fitness[0] = benchmark.elliptic(Dimension, x);
//    		break;
//
//    	case -4: // Benchmark. Schwefel + noise
//    		fitness[0] = benchmark.schwefel_1_2(Dimension, x);
//    		r = Tribes.generator.nextGaussian();
//    		fitness[0] = fitness[0] + (1 + 0.4 * Math.abs(r));
//    		break;
//
//    	case -5: // Benchmark. Schwefel with optimum on bounds
//    		fitness[0] = benchmark.schwefel_bounds(Dimension, x);
//    		break;
//
//    	case -6: // Benchmark. Rosenbrock
//    		fitness[0] = benchmark.rosenbrock(Dimension, x);
//    		break;
//
//    	case -7: // Benchmark. Griewank
//    		fitness[0] = benchmark.griewank(Dimension, x);
//    		break;
//
//    	case -8: // Benchmark. Ackley with optimum on bounds
//    		fitness[0] = benchmark.ackley_bounds(Dimension, x);
//    		break;
//
//    	case -9: // Benchmark. Rastrigin
//    		fitness[0] = benchmark.rastrigin(Dimension, x, false);
//    		break;
//
//    	case -10: // Benchmark. Rastrigin, rotated
//    		fitness[0] = benchmark.rastrigin(Dimension, x, true);
//    		break;
//
//    	case -11: // Benchmark. Weierstrass
//    		fitness[0] = benchmark.weierstrass(Dimension, x);
//    		break;
//
//    		// ---------------   Benchmark 2, with constraints
//    	case -101:
//    		fitness = benchmarkConstr.g01(x);
//    		break;
//
//    	case -102:
//    		fitness = benchmarkConstr.g02(x);
//    		break;
//    	case -103:
//    		fitness = benchmarkConstr.g03(x);
//    		break;
//    	case -104:
//    		fitness = benchmarkConstr.g04(x);
//    		break;
//    	case -105:
//    		fitness = benchmarkConstr.g05(x);
//    		break;
//    	case -106:
//    		fitness = benchmarkConstr.g06(x);
//    		break;
//    	case -107:
//    		fitness = benchmarkConstr.g07(x);
//    		break;
//    	case -108:
//    		fitness = benchmarkConstr.g08(x);
//    		break;
//    	case -109:
//    		fitness = benchmarkConstr.g09(x);
//    		break;
//    	case -110:
//    		fitness = benchmarkConstr.g10(x);
//    		break;
//    	case -111:
//    		fitness = benchmarkConstr.g11(x);
//    		break;
//    	case -112:
//    		fitness = benchmarkConstr.g12(x);
//    		break;
//    	case -113:
//    		fitness = benchmarkConstr.g13(x);
//    		break;
//    	case -114:
//    		fitness = benchmarkConstr.g14(x);
//    		break;
//    	case -115:
//    		fitness = benchmarkConstr.g15(x);
//    		break;
//    	case -116:
//    		fitness = benchmarkConstr.g16(x);
//    		break;
//    	case -117:
//    		fitness = benchmarkConstr.g17(x);
//    		break;
//    	case -118:
//    		fitness = benchmarkConstr.g18(x);
//    		break;
//    	case -119:
//    		fitness = benchmarkConstr.g19(x);
//    		break;
//    	case -120:
//    		fitness = benchmarkConstr.g20(x);
//    		break;
//    	case -121:
//    		fitness = benchmarkConstr.g21(x);
//    		break;
//    	case -122:
//    		fitness = benchmarkConstr.g22(x);
//    		break;
//    	case -123:
//    		fitness = benchmarkConstr.g23(x);
//    		break;
//    	case -124:
//    		fitness = benchmarkConstr.g24(x);
//    		break;
//
//    	}
//
//    	fitness[0] = Math.abs(fitness[0] - objective);
//    	/* WARNING.  For the other functions, the objective has to be zero
//                 It is in particular the case for constraints
//    	 */
//    	totalError = totalError(fitness, fitnessSize);
//
//    	return eval+1;
//    }
//
//    public double totalError(double[] fitness, int fitnessSize) {
//    	/*
//      Warning. The objective value has been taken into account only for fitness[0]
//    	 */
//    	double t = 0;
//    	int n;
//    	for (n = 0; n < fitnessSize; n++) {
//    		t = t + Math.abs(fitness[n]);
//    	}
//    	return t;
//    }
//
//    private double alpine() {
//    	// Clerc's f1, Alpine function, min 0 in (0,...,0)
//    	int d;
//    	double xd;
//    	double f = 0;
//
//    	for (d = 0; d < Dimension; d++) {
//    		xd = x[d];
//    		f = f + Math.abs(xd * Math.sin(xd) + 0.1 * xd);
//    	}
//    	return f;
//    }
//
//    private double parabola() {
//    	// Sometimes called Sphere
//    	int d;
//    	double f = 0;
//
//    	for (d = 0; d < Dimension; d++) {
//    		f = f + x[d] * x[d];
//    	}
//    	return f;
//    }
//
//    private double griewank() {
//    	int d;
//    	double f;
//    	double f1 = 0;
//    	double f2 = 1;
//
//    	for (d = 0; d < Dimension; d++) {
//    		f1 = f1 + Math.pow(x[d] - 100, 2);
//    		f2 = f2 * Math.cos((x[d] - 100) / Math.sqrt(d + 1));
//    	}
//
//    	f = f1 / 4000 - f2 + 1;
//
//    	return f;
//    }
//
//    private double rosenbrock() {
//    	int d;
//    	double f = 0;
//
//    	for (d = 0; d < Dimension - 1; d++) {
//    		f = f +
//    		100 *
//    		Math.pow(x[d + 1] - x[d] * x[d], 2) +
//    		Math.pow(x[d] - 1, 2);
//    	}
//
//    	return f;
//    }
//
//    private double rastrigin() {
//    	int d;
//    	double f = 0;
//    	for (d = 0; d < Dimension; d++) {
//    		f = f + Math.pow(x[d], 2) -
//    		10 * Math.cos(2 * Math.PI * x[d]);
//    	}
//    	f = f + 10 * Dimension;
//    	return f;
//    }
//
//    private double ackley() {
//    	int d;
//    	int D = Dimension;
//    	double f;
//    	double sum1 = 0;
//    	double sum2 = 0;
//    	double xd;
//    	for (d = 0; d < D; d++) {
//    		xd = x[d];
//    		xd = xd - 0.5; // Test shift
//    		sum1 = sum1 + xd * xd;
//    		sum2 = sum2 + Math.cos(2 * Math.PI * xd);
//    	}
//    	f = -20 * Math.exp( -0.2 * Math.sqrt(sum1 / D)) - Math.exp(sum2 / D) +
//    	20 + Math.E;
//    	return f;
//    }
//
//    private double tripod() {
//    	int d;
//    	double f = 0;
//    	double x1, x2;
//    	// on [-100, 100], min 0
//    	x1 = x[0];
//    	x2 = x[1];
//
//    	if (x2 < 0) {
//    		f = Math.abs(x1) + Math.abs(x2 + 50);
//    	} else {
//    		if (x1 < 0) {
//    			f = 1 + Math.abs(x1 + 50) + Math.abs(x2 - 50);
//    		} else {
//    			f = 2 + Math.abs(x1 - 50) + Math.abs(x2 - 50);
//    		}
//    	}
//    	return f;
//    }
//
//    private double pressureVessel() {
//
//    	// Pressure vessel
//    	// Ref New Optim. Tech. in Eng. p 638
//    	/* D=4
//          1.1 <= x1 <= 12.5        granularity 0.0625
//          0.6 <= x2 <= 12.5         granularity 0.0625
//          0 .0 <= x3 <= 240
//          0.0 <= x4 <= 240
//          constraints
//          g1:= 0.0193*x3-x1 <=0
//          g2 := 0.00954*x3-x2<=0
//          g3:= 750*1728-pi*x3*x3*(x4-(4/3)*x3)  <=0
//    	 */
//    	double f;
//    	double y;
//    	double c;
//    	double x1, x2, x3, x4;
//    	x1 = x[0];
//    	x2 = x[1];
//    	x3 = x[2];
//    	x4 = x[3];
//
//    	f = 0.6224 * x1 * x3 * x4 + 1.7781 * x2 * x3 * x3 +
//    	3.1611 * x1 * x1 * x4 + 19.84 * x1 * x1 * x3;
//
//    	// Constraints
//    	y = 0.0193 * x3 - x1;
//    	if (y > 0) {
//    		c = 1 + Math.pow(10, 10) * y;
//    		f = f * c * c;
//    	}
//    	y = 0.00954 * x3 - x2;
//    	if (y > 0) {
//    		c = 1 + y;
//    		f = f * c * c;
//    	}
//    	y = 750 * 1728 - Math.PI * x3 * x3 * (x4 + (4.0 / 3) * x3);
//    	if (y > 0) {
//    		c = 1 + y;
//    		f = f * c * c;
//    	}
//
//    	return f;
//
//    }
//
//    private double compressionSpring() {
//    	/* Coil compression spring. Ref New Optim. Tech. in Eng. p 644
//    	 */
//    	double x1, x2, x3;
//    	double f;
//    	double c, Cf, K, sp, lf;
//    	double y;
//    	double Fmax = 1000.0;
//    	double S = 189000.0;
//    	double lmax = 14.0;
//    	double dmin = 0.2;
//    	double Dmax = 3.0;
//    	double Fp = 300;
//    	double spm = 6.0;
//    	double sw = 1.25;
//    	double G = 11500000;
//
//    	x1 = x[0];
//
//    	x2 = x[1];
//    	x3 = x[2];
//
//    	f = Math.PI * Math.PI * x2 * x3 * x3 * (x1 + 2) * 0.25;
//
//    	// Constraints
//    	Cf = 1 + 0.75 * x3 / (x2 - x3) + 0.615 * x3 / x2;
//    	K = 0.125 * G * Math.pow(x3, 4) / (x1 * x2 * x2 * x2);
//    	sp = Fp / K;
//    	lf = Fmax / K + 1.05 * (x1 + 2) * x3;
//
//    	y = 8 * Cf * Fmax * x2 / (Math.PI * x3 * x3 * x3) - S;
//    	if (y > 0) {
//    		c = 1 + y;
//    		f = f * c * c * c;
//    	}
//
//    	y = lf - lmax;
//    	if (y > 0) {
//    		c = 1 + y;
//    		f = f * c * c * c;
//    	}
//
//    	y = sp - spm;
//    	if (y > 0) {
//    		c = 1 + y;
//    		f = f * c * c * c;
//    	}
//
//    	//y=sp+(Fmax-Fp)/K + 1.05*(x1+2)*x3 - lf;
//    	y = sp - Fp / K;
//
//    	if (y > 0) {
//    		c = 1 + Math.pow(10, 10) * y;
//    		f = f * c * c * c;
//    	}
//
//    	y = sw - (Fmax - Fp) / K;
//    	if (y > 0) {
//    		c = 1 + Math.pow(10, 10) * y;
//    		f = f * c * c * c;
//    	}
//
//    	return f;
//
//    }
//
//    private double product() {
//    	/* Interesting only for integer numbers
//      Show that Tribes is _not_ good for decomposition of a number N:
//    it tends to find pow(N,1/D) for each component.
//    	 */
//    	double f=1;
//    	int d;
//    	for (d = 0; d < Dimension; d++) {
//    		f = f *x[d];
//    	}
//    	return f;
//    }
//
//    private double sin_sin2_3() {
//    	// On [3, 13]^D. Best solution -1.21598*D
//    	double  f=0;
//    	int d;
//    	for (d = 0; d < Dimension; d++) {
//    		f = f +Math.sin(x[d]) + Math.sin(2*x[d]/3);
//    	}
//
//    	return f;
//    }

    //====================================================================================
    private double isolation(double[][] range, TribesPosition pos,
                             TribesSwarm swarm0) {

        int d, m, n;
        double r;
        double rmin;
//System.out.print("\n x0,x1 "+pos.x[0]+" "+pos.x[1]);
        /*Boucle sur les dimensions
                 On cherche Ã  maximiser la plus petite des distances
         */
        rmin = Math.min(Math.abs(pos.x[0] - range[0][0]),
                Math.abs(pos.x[0] - range[0][1]));
        for (d = 1; d < range.length; d++) {
            r = Math.min(Math.abs(pos.x[d] - range[d][0]),
                    Math.abs(pos.x[d] - range[d][1]));
            if (r < rmin) {
                rmin = r;
            }
        }

// Boucle sur les mÃ©moires
// System.out.print("\n tribeNb "+swarm0.tribeNb);
        for (n = 0; n < swarm0.tribeNb; n++) {
            // System.out.print(" memoryNb "+swarm0.tribes[n].memoryNb);
            for (m = 0; m < swarm0.tribes[n].memoryNb; m++) {
                r = pos.distanceTo(swarm0.tribes[n].memory[m].getPos());
                if (r < rmin) {
                    rmin = r;
                }
            }
        }
        return rmin;
    }


    public TribesPosition maxIsolated(double[][] range, TribesSwarm swarm0) {
        /* Utilise ma mÃ©thode SunnySpell pour trouver une position Ã©loignÃ©e de toutes
          celles connues
         */
        /*
         Maurice.Clerc@WriteMe.com
         First version: 2005-08-21
         Last update:2005-08-27

         N points Pi in the search space H.
         Add a point Xm so that the distribution {P1, ..., PN,Xm} is
         as uniform/regular as possible.

         Method (explanation for dimension D=2
         ------
         For any point X(x1,x2) in S, let s(X) be the distance of X to S, i.e.
         s(X)=min(x1,1-x1,x2,1-x2)

         We define the "isolation" ("isolement" in French) function
         I(X)=min(min_i(norm(X-Pi))+ s(X))

         We are looking for the point Xm where this function reaches its maximum.

         Note:
         You could think a VoronoÃ¯/Delaunay tesselation does the job.
         It is not the case for such a method don't take into account the infinite
         number of points on the "frontier" of the search space, so the result may be
         a point to near of this "frontier".


         Why "SunnySpell"?
         ----------------
         Let's suppose each point on the frontier of S, and each Pi, is the center
         of a disc of radius r. Such a disc can be seen as a "cloud", and S is the "sky".

         A point X is "covered" if it belongs to at least one "cloud".

         As long as r>I(Xm), all points are covered (the sky is overcast)
         As soon as r<I(Xm), there is a "sunny spell" ("Ã©claircie" in French):
         some points are not covered (at least Xm).

         What is for?
         ------------
         In an adaptive Particle Swarm Optimiser like Tribes, some particles are
         added from time to time. Doing that just purely at random may be not the best way.
         It could be better to add a particle that is as "isolated" as possible
         in order to explore some "no man's lands" in the search space.
         It means in particular that we are not looking for a very precise result,
         but just a "good enough" one.

         */
        //-----------------------------------
        /* Search is performed by using  a very basic PSO version */
        int D = range.length;
        // One swarm of constant size
        // only one range is regarded here, the "right" one must be provided 
        TribesSwarm swarm = new TribesSwarm(null, range, range); // MK: slight abuse of master (set to null)

        swarm.size = (int) Math.round(9.5 + 0.124 * (D - 9)); //Cf. my "Binary PSO" study
        swarm.size = Math.min(swarm.size, Tribes.maxExplorerNb);

        // One tribe of constant size
        swarm.tribeNb = 1;
        Tribe tribe = new Tribe();
        swarm.tribes[0] = tribe;
        swarm.tribes[0].explorerNb = swarm.size;
        swarm.tribes[0].memoryNb = swarm.size;
        swarm.tribes[0].status = 0;

// Constant numerical coefficients
        double c1 = 1 / (2 * Math.log(2)); // Cf. my "Stagnation Analysis" study
        double cmax = c1 + 2 * Math.log(2) - 1;

// one explorer <=> one memory
        TribesExplorer explorer = new TribesExplorer(range, 0.);    // obj value will not be regarded here
        TribesMemory memory = new TribesMemory(range.length);

        //int link[][] =
        if ((maxIsoLink == null) || (maxIsoLink.length != swarm.size)) {
            maxIsoLink = new int[swarm.size][swarm.size];
        }
        for (int i = 0; i < swarm.size; i++) {
            Arrays.fill(maxIsoLink[i], 0);
            maxIsoLink[i][i] = 1;
        }

        int best;
        int d, m, n;
        double f, fBest, f2;
        double r, r1, r2;
        boolean improv;
        int iter;
        int iterMax = (int) Math.round(300 * Math.log(D)); // Purely empirical
        boolean stop;
        double xd;
        double vd;
        int neighbourhoodSize = 3;
        //double eps = 0.01;

        /*We are just looking for a position that is "enough isolated".
                 No need of a very precise solution
         */

        if (TRACE) {
            System.out.println("sunny start");
        }
        // INITIALISATION

        for (n = 0; n < swarm.size; n++) {

            swarm.tribes[0].explorer[n] = explorer;

            for (d = 0; d < D; d++) {
                r = RNG.randomDouble();

                swarm.tribes[0].explorer[n].position.x[d] = range[d][0] +
                        (range[d][1] - range[d][0]) * r;

                r = RNG.randomDouble();
                swarm.tribes[0].explorer[n].velocity.x[d] = (1 - 2 * r) *
                        (range[d][1] - range[d][0]) /
                        2;
            }
            // Fitness evaluation
            swarm.tribes[0].explorer[n].position.fitness[0] = isolation(range,
                    swarm.tribes[0].explorer[n].position, swarm0);
            // At the beginnin, memory=explorer
            swarm.tribes[0].memory[n] = memory;
            swarm.tribes[0].memory[n].setPos(swarm.tribes[0].explorer[n].position.clone());
            swarm.tribes[0].memory[n].setPrevPos(swarm.tribes[0].memory[n].getPos().clone());
        }

// Find the best
        swarm.findBest();

        stop = false;
        improv = false;
        iter = 0;

        while (!stop) {
// We are looking for a _maximum_ (isolation)
            improv = swarm.bestMem.getPos().fitness[0] >
                    swarm.bestMem.getPrevPos().fitness[0];
            //swarm.bestMem.position=swarm.bestMem.position.clone(); // MK: whats this??

            if (!improv) {
                /* Redefine information links at random.
                 However, there is always one link between an explorer and "its" memory
                 (cf. my book, algorithm OEP0)

                 */
                for (n = 0; n < swarm.size; n++) {
                    Arrays.fill(maxIsoLink[n], 0);
                    maxIsoLink[n][n] = 1;
                }
                for (n = 0; n < swarm.size; n++) {
//                    m = RNG.randomInt(0,neighbourhoodSize-1);
//                    maxIsoLink[n][m] = 1;
                    for (int k = 0; k < neighbourhoodSize; k++) {
                        m = RNG.randomInt(swarm.size);
                        maxIsoLink[n][m] = 1;
                    }
                }
            }

            for (m = 0; m < swarm.size; m++) {
                //  Find the best neighbour memory. Warning, fitness is to be maximized
                best = 0;
                fBest = swarm.tribes[0].memory[best].getPos().fitness[0];
                for (n = 1; n < swarm.size; n++) { // check neighbors
                    if (maxIsoLink[n][m] == 1) {
                        f2 = swarm.tribes[0].memory[n].getPos().fitness[0];
                        if (f2 > fBest) {
                            best = n;
                            fBest = f2;
                        }
                    }
                }

                // Move
                for (d = 0; d < D; d++) {
                    r1 = RNG.randomDouble();
                    r2 = RNG.randomDouble();
                    xd = swarm.tribes[0].explorer[m].position.x[d];

                    swarm.tribes[0].explorer[m].velocity.x[d] = c1 *
                            swarm.tribes[0].explorer[m].velocity.x[d] +
                            cmax * r1 *
                                    (swarm.tribes[0].memory[m].getPos().x[d] - xd) +
                            cmax * r2 *
                                    (swarm.tribes[0].memory[best].getPos().x[d] - xd);

                    swarm.tribes[0].explorer[m].position.x[d] = xd +
                            swarm.tribes[0].explorer[m].velocity.x[d];
                }
                /* Keep in the box
                 Note that the solution is certainly not on the boundary
                 That is why velocity is not set to zero as usually
                 */
                for (d = 0; d < D; d++) {
                    xd = swarm.tribes[0].explorer[m].position.x[d];
                    vd = swarm.tribes[0].explorer[m].velocity.x[d];
                    if (xd < range[d][0]) {
                        swarm.tribes[0].explorer[m].position.x[d] = range[d][0];
                        swarm.tribes[0].explorer[m].velocity.x[d] = -vd / 2;
                    }
                    if (xd > range[d][1]) {
                        swarm.tribes[0].explorer[m].position.x[d] = range[d][1];
                        swarm.tribes[0].explorer[m].velocity.x[d] = -vd / 2;
                    }

                }

                //Evaluation of the new position
                f = isolation(range, swarm.tribes[0].explorer[m].position, swarm0);
                swarm.tribes[0].explorer[m].position.fitness[0] = f;

                // If improvement change memory, and possibly swarm.Best
                if (f > swarm.tribes[0].memory[m].getPos().fitness[0]) {
                    swarm.tribes[0].memory[m].setPos(swarm.tribes[0].explorer[m].position.clone());
                }

                if (f > swarm.bestMem.getPos().fitness[0]) {
                    swarm.bestMem.setPos(swarm.tribes[0].explorer[m].position.clone());
                }
            }

// Evaluate stop criterion
            /*
                         f1 = swarm.Best.position.fitness;
                         f2 = swarm.Best.positionPrev.fitness;
                         stop = 2*(f1 - f2) / (f1+f2) < eps;
             */

            iter++;
            stop = (iter >= iterMax);
        }
        //       System.out.print(" \n Isolation amongst "+ swarm0.tribes[0].memoryNb+" memories: "+swarm.Best.position.fitness+"\n");
        /*
               for(d=0;d<D;d++)
             System.out.print(swarm.Best.position.x[d]+" ");
         */
        swarm.bestMem.getPos().isolation = swarm.bestMem.getPos().fitness[0];
        if (TRACE) {
            System.out.println("sunny end, ret " + swarm.bestMem.getPos().toString());
        }
        return swarm.bestMem.getPos();
    }

    /**
     * Returns true, if fitness f1 is better than f2, else false.
     *
     * @param f1
     * @param f2
     * @return
     */
    public boolean firstIsBetter(double[] f1, double[] f2) {
        // Return "true" if f1 better than f2
        int n;
        if (!Tribes.testBC) { // Multiobjective approach
            for (n = 0; n < f1.length; n++) {
                if (f1[n] > f2[n]) {
                    return false;
                }
            }
            return true;
        } else { // Total error approach
            if (calcTotalError(f1) < calcTotalError(f2)) {
                return true;
            }
            return false;
        }
    }

    public double getTotalError() {
        return (totalError == 0) ? 0.00000000000000001 : totalError;
    }

    public void setTotalError(double objectiveFirstDim) {
        totalError = calcTotalError(objectiveFirstDim, fitness);
    }

    public void setTotalError() {
        totalError = calcTotalError(fitness);
    }

    public double calcTotalError(double objectiveFirstDim, double[] fitness) {
        /*
         Take into account the objective value in the first dimension.
    	    	 */
        double t = Math.abs(fitness[0] - objectiveFirstDim);

        int n;
        for (n = 1; n < fitness.length; n++) {
            t += Math.abs(fitness[n]);
        }
        return t;
    }

    public double calcTotalError(double[] fitness) {
    	/*
  Warning. The objective value might not been taken into account!
    	 */
        double t = 0;
        int n;
        for (n = 0; n < fitness.length; n++) {
            t += Math.abs(fitness[n]);
        }
        return t;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("TribesMemory at [");
        for (int i = 0; i < x.length; i++) {
            sb.append(x[i]);
            sb.append(",");

        }
        sb.append("]");
        return sb.toString();
    }
}
