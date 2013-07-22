package eva2.optimization.strategies;

import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.tools.math.RNG;

/**
 * This extends the particle swarm optimization implementation
 * so that the guaranteed global convergence pso can be used as suggested by
 * Franz van den Bergh in "An Analysis of Particle Swarm Optimizers".
 * In this modification the velocity of the global best particle is updated differently.
 */
public class ParticleSwarmOptimizationGCPSO extends ParticleSwarmOptimization {
    // choosable parameters:
    protected boolean gcpso;
    protected int sc;
    protected int fc;
    protected double rhoIncreaseFactor;
    protected double rhoDecreaseFactor;
    // members updated via updateGCPSOMember:
    protected int gbestParticleIndex = -1;
    protected boolean gbestParticleHasChanged;
    protected int numOfSuccesses;
    protected int numOfFailures;
    protected AbstractEAIndividual gbestParticle;
    private double rho; //initial rho value can be chosen

    protected int getAccelerationForGlobalBestParticleCounter = 0; // only for testing

/**********************************************************************************************************************
 * ctors, inits
 */
    /**
     * @tested ps
     * ctor - sets default values according to
     * "An Analyis of Paricle Swarm Optimizers" by Franz van den Bergh
     */
    public ParticleSwarmOptimizationGCPSO() {
        setGcpso(true);
        gbestParticleIndex = -1;
        //gbestParticle
        gbestParticleHasChanged = false;
        numOfSuccesses = 0;
        numOfFailures = 0;
        setRho(1);
        SetSc(15);
        SetFc(5);
        SetRhoIncreaseFactor(2.0);
        SetRhoDecreaseFactor(0.5);
    }

    /**
     * @param a
     * @tested ps
     */
    public ParticleSwarmOptimizationGCPSO(ParticleSwarmOptimizationGCPSO a) {
        super(a);
        this.setGcpso(a.gcpso);
        this.gbestParticleIndex = a.gbestParticleIndex;
        if (a.gbestParticle != null) {
            this.gbestParticle = (AbstractEAIndividual) a.gbestParticle.clone();
            double[] aFitness = (double[]) a.gbestParticle.getData(partBestFitKey);
            this.gbestParticle.putData(partBestFitKey, aFitness.clone());
        }
        this.gbestParticleHasChanged = a.gbestParticleHasChanged;
        this.numOfSuccesses = a.numOfSuccesses;
        this.numOfFailures = a.numOfFailures;
        this.setRho(a.getRho());
        this.SetSc(a.getSc());
        this.SetFc(a.getFc());
        this.SetRhoIncreaseFactor(a.getRhoIncreaseFactor());
        this.SetRhoDecreaseFactor(a.getRhoDecreaseFactor());
    }

    /**
     * This method returns a global info string
     *
     * @return description
     */
    public static String globalInfo() {
        return "Guaranteed Convergence Particle Swarm Optimiser (GCPSO) " +
                "as proposed by F. van den Bergh.";
    }

/**********************************************************************************************************************
 * overwritten
 */
    /**
     * @tested (non-Javadoc) @see javaeva.server.go.strategies.ParticleSwarmOptimization#optimize()
     */
    @Override
    public void optimize() {
        super.optimize(); //updatePopulation->updateIndividual->updateVelocity (s.u.)
        updateGCPSOMember();
    }

    /**
     * @tested junit&
     * (non-Javadoc) @see javaeva.server.go.strategies.ParticleSwarmOptimization#updateVelocity(int, double[], double[], double[], double[], double[][])
     * uses a special velocity update strategy for the gobal best particle.
     */
    @Override
    protected double[] updateVelocity(int index, double[] lastVelocity, double[] personalBestPos, double[] curPosition, double[] neighbourBestPos, double[][] range) {
        double[] accel, curVelocity = new double[lastVelocity.length];

        if (useAlternative) {
            accel = getAccelerationAlternative(index, personalBestPos, neighbourBestPos, curPosition, range);
        } else {
            if (index == gbestParticleIndex && isGcpso()) {
                accel = getAccelerationForGlobalBestParticle(personalBestPos, neighbourBestPos, curPosition, range);
            } else {
                accel = getAcceleration(personalBestPos, neighbourBestPos, curPosition, range);
            }
        }
        for (int i = 0; i < lastVelocity.length; i++) {
            curVelocity[i] = this.m_InertnessOrChi * lastVelocity[i];
            curVelocity[i] += accel[i];
        }
        return curVelocity;
    }

/**********************************************************************************************************************
 * GCPSO core
 */
    /**
     * @return the acceleration component.
     *         Should only be called for the global best particle
     *         (i.e. the particle with the best personal best position)
     * @tested ps
     */
    private double[] getAccelerationForGlobalBestParticle(
            double[] personalBestPos, double[] neighbourBestPos,
            double[] curPosition, double[][] range) {
        ++getAccelerationForGlobalBestParticleCounter;
        double[] accel = new double[curPosition.length];
        for (int i = 0; i < personalBestPos.length; i++) {
            // reset position to the global best position
            accel[i] = -curPosition[i] + personalBestPos[i]; //pbestpos of gbestparticle is gbestpos
            // random search around the global best position
            accel[i] += getRho() * (1.0 - 2.0 * RNG.randomDouble(0, 1));
        }
        //System.out.println("rho: " +getVecNorm(accel));
        //System.out.println("accel: " +getVecNorm(accel));
        return accel;
    }

    /**
     * @return the index of the particle with the best personal best position
     *         (i.e. the index of the global best particle)
     * @tested junit
     */
    protected int getIndexOfGlobalBestParticle() {
        if (getPopulation().size() == 0) {
            System.out.println("getIndexOfGlobalBestParticle error: no particle in population");
            return -1;
        }
        int index = 0;
        double[] gbestFitness = (double[]) getPopulation().getEAIndividual(0).getData(partBestFitKey);
        for (int i = 1; i < getPopulation().size(); ++i) {
            AbstractEAIndividual indy = getPopulation().getEAIndividual(i);
            double[] currentBestFitness = (double[]) indy.getData(partBestFitKey);
            if (AbstractEAIndividual.isDominatingFitness(currentBestFitness, gbestFitness)) {
                gbestFitness = currentBestFitness;
                index = i;
            }
        }
        return index;
    }

/**********************************************************************************************************************
 * updateGCPSOMember
 */
    /**
     * @tested junit
     * updates: gbestParticleIndex,gbestParticleHasChanged,numOfSuccesses,numOfFailures,gbestParticle,rho
     */
    protected void updateGCPSOMember() {
        int index = getIndexOfGlobalBestParticle();

        /** gbestParticleIndex,gbestParticleHasChanged */
        // check if the gbestParticle changed in the last optimization loop
        if (index != gbestParticleIndex) {
            gbestParticleHasChanged = true;
            gbestParticleIndex = index;
        } else {
            gbestParticleHasChanged = false;
        }

        /**  numOfSuccesses,numOfFailures */
        // check if the gbestParticle improved over the last iteration
        if (gbestParticle == null) { // no previous gbest on first call available
            AbstractEAIndividual gbestParticleCurrent = (AbstractEAIndividual) getPopulation().getEAIndividual(gbestParticleIndex);
            gbestParticle = (AbstractEAIndividual) gbestParticleCurrent.clone();
        }
        AbstractEAIndividual gbestParticleOld = gbestParticle;
        double[] gbestParticleFitnessOld = (double[]) gbestParticleOld.getData(partBestFitKey);
        AbstractEAIndividual gbestParticleCurrent = (AbstractEAIndividual) getPopulation().getEAIndividual(gbestParticleIndex);
        double[] gbestParticleFitnessCurrent = (double[]) gbestParticleCurrent.getData(partBestFitKey);

//		if (gbestParticleHasChanged && false){  // reset rho on change?
//			numOfFailures = 0;
//			numOfSuccesses = 0;
//			setRho(1);
//		} else {
        if (AbstractEAIndividual.isDominatingFitnessNotEqual(gbestParticleFitnessCurrent, gbestParticleFitnessOld)) {
            ++numOfSuccesses;
            numOfFailures = 0;
        } else {
            ++numOfFailures;
            numOfSuccesses = 0;
        }
//		}

        /** gbestParticle */
        gbestParticle = (AbstractEAIndividual) gbestParticleCurrent.clone();

        /** rho */
        if (numOfSuccesses > getSc()) {
            setRho(getRhoIncreaseFactor() * getRho());
            //System.out.println("rho increased");
        }
        //System.out.println(getRhoIncreaseFactor());
        if (numOfFailures > getFc()) {
            setRho(getRhoDecreaseFactor() * getRho());
            //System.out.println("rho decreased");
        }
        //System.out.println(getRhoDecreaseFactor());
    }

    /**
     * *******************************************************************************************************************
     * getter, setter
     */
    public void setGcpso(boolean gcpso) {
        this.gcpso = gcpso;
    }

    public boolean isGcpso() {
        return gcpso;
    }

    public String gcpsoTipText() {
        return "deactivate to use the standard PSO by Kennedy and Eberhart";
    }

    public void SetSc(int sc) {
        this.sc = sc;
    }

    public int getSc() {
        return sc;
    }

    public void SetFc(int fc) {
        this.fc = fc;
    }

    public int getFc() {
        return fc;
    }

    public void SetRhoIncreaseFactor(double rhoIncreaseFactor) {
        this.rhoIncreaseFactor = rhoIncreaseFactor;
    }

    public double getRhoIncreaseFactor() {
        return rhoIncreaseFactor;
    }

    public void SetRhoDecreaseFactor(double rhoDecreaseFactor) {
        this.rhoDecreaseFactor = rhoDecreaseFactor;
    }

    public double getRhoDecreaseFactor() {
        return rhoDecreaseFactor;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public String rhoTipText() {
        return "controls the initial radius of a random search in an area surrounding the global best position of the swarm";
    }

    public double getRho() {
        return rho;
    }
}
