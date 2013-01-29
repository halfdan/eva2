package eva2.server.go.operators.paramcontrol;

import eva2.server.go.populations.Population;
import eva2.server.go.strategies.ParticleSwarmOptimization;
import eva2.tools.math.Mathematics;
import java.io.Serializable;

/**
 * After the ANTS 08 paper by Yasuda et al., this implements an activity feedback control mechanism.
 * The inertia of the PSO is made dependent on time and the target activity of the swarm, where activity
 * here is simply the average relative velocity of the particles.
 * The target activity decreases with time, and if the current activity is too low (high), the 
 * inertia is increased (decreased) so that the activity approximates the target activity.
 * 
 * The original authors used an absolute velocity measure and FIPS which is not implemented in EvA2 so far.
 * However, after some initial tests, this version seems 
 * to work ok, although it depends on the defined target activity. I am not convinced that in general it is
 * easier to define than a constant constriction factor for the standard constricted PSO.
 * Still, the possibility to control the convergence behaviour based on time is nice, and it works quite good on F6, for example.
 *  
 * @author mkron
 *
 */
public class PSOActivityFeedbackControl implements ParamAdaption, Serializable {
	private double minInert=0.5;
	private double maxInert=1;
	private double startAct=0.17;
	private double endAct=0.01;
	private double deltaInertness = 0.1;
	private static boolean TRACE=false;
	private boolean exponentialSchedule = true;

	private static String target = "inertnessOrChi";
	
	public PSOActivityFeedbackControl() {};
			
	public PSOActivityFeedbackControl(
			PSOActivityFeedbackControl o) {
		minInert = o.minInert;
		maxInert = o.maxInert;
		startAct = o.startAct;
		endAct = o.endAct;
		deltaInertness = o.deltaInertness;
	}

    @Override
	public Object clone() {
		return new PSOActivityFeedbackControl(this);
	}

    @Override
	public Object calcValue(Object obj, Population pop, int iteration, int maxIteration) {
		if (obj instanceof ParticleSwarmOptimization) {
			ParticleSwarmOptimization pso = (ParticleSwarmOptimization)obj;
			
			double currentAct = calculateActivity(pso);
			double currentInertness = pso.getInertnessOrChi();
			Double val = calcNewInertness(currentInertness, currentAct, desiredActivity(iteration, maxIteration));
			return val;
		} else {
			System.err.println("Cant control this object type!!");
			return null;
		}
	}

    @Override
	public String getControlledParam() {
		return target;
	}

	private double calcNewInertness(double currentInertness, double currentAct,
			double desiredActivity) {
		if (TRACE) System.out.println("Activity was " + currentAct + ", desired: " + desiredActivity);
		if (currentAct < desiredActivity) { // increase inertness
			return Math.min(maxInert, currentInertness + deltaInertness);
		} else if (currentAct > desiredActivity) { // too high act, so decrease inertness
			return Math.max(minInert, currentInertness - deltaInertness);
		} else return currentInertness;
	}

	private double desiredActivity(int iteration, int maxIteration) {
		if (exponentialSchedule) return startAct*Math.pow(endAct/startAct, iteration/(double)maxIteration);
		else return Mathematics.linearInterpolation(iteration, 0, maxIteration, startAct, endAct);
	}

	private double calculateActivity(ParticleSwarmOptimization pso) {
		return pso.getPopulationAvgNormedVelocity(pso.getPopulation());
	}

	public double getMinInertness() {
		return minInert;
	}
	public void setMinInertness(double minInert) {
		this.minInert = minInert;
	}
	public String minInertnessTipText() {
		return "The minimum inertness value to be used.";
	}
	
	public double getMaxInertness() {
		return maxInert;
	}
	public void setMaxInertness(double maxInert) {
		this.maxInert = maxInert;
	}
	public String maxInertnessTipText() {
		return "The maximum inertness value to be used.";
	}
	
	public double getInitActivity() {
		return startAct;
	}
	public void setInitActivity(double startAct) {
		this.startAct = startAct;
	}
	public String initActivityTipText() {
		return "The initial target activity (relative to the range).";
	}

	public double getFinalActivity() {
		return endAct;
	}
	public void setFinalActivity(double endAct) {
		this.endAct = endAct;
		if (endAct==0 && isExponentialSchedule()) System.err.println("Warning: zero final activity will not work with exponential schedule, set it to small epsilon!");
	}
	public String finalActivityTipText() {
		return "The final target activity (relative to the range), should be close to zero.";
	}
	
	public double getDeltaInertness() {
		return deltaInertness;
	}
	public void setDeltaInertness(double deltaInertness) {
		this.deltaInertness = deltaInertness;
	}
	public String deltaInertnessTipText() {
		return "The additive change of the inertness in each adaption step.";
	}
	
	public static String globalInfo() {
		return "Controls the inertness factor based on the average velocity.";
	}

    @Override
	public void finish(Object obj, Population pop) {}

    @Override
	public void init(Object obj, Population pop, Object[] initialValues) {}
	
	public boolean isExponentialSchedule() {
		return exponentialSchedule;
	}

	public void setExponentialSchedule(boolean exponentialSchedule) {
		this.exponentialSchedule = exponentialSchedule;
		if (getFinalActivity()==0) System.err.println("Warning: zero final activity will not work with exponential schedule, set it to small epsilon!");
	}

	public String exponentialScheduleTipText() {
		return "Use linear or exponential activity decrease schedule.";
	}
}
