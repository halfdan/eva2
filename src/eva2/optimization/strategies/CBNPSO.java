package eva2.optimization.strategies;

import eva2.optimization.go.PopulationInterface;
import eva2.optimization.enums.PSOTopologyEnum;
import eva2.optimization.operators.cluster.ClusteringDensityBased;
import eva2.optimization.operators.distancemetric.IndividualDataMetric;
import eva2.optimization.operators.paramcontrol.CbpsoFitnessThresholdBasedAdaption;
import eva2.optimization.operators.paramcontrol.LinearParamAdaption;
import eva2.optimization.operators.paramcontrol.ParamAdaption;
import eva2.optimization.operators.paramcontrol.SinusoidalParamAdaption;
import eva2.optimization.populations.Population;
import eva2.optimization.populations.SolutionSet;
import eva2.optimization.problems.AbstractProblemDouble;
import eva2.optimization.problems.InterfaceInterestingHistogram;
import eva2.optimization.problems.InterfaceOptimizationProblem;
import eva2.optimization.problems.InterfaceProblemDouble;
import eva2.tools.EVAERROR;
import eva2.tools.ToolBox;
import java.io.Serializable;

public class CBNPSO extends ClusterBasedNichingEA implements Serializable {
	private boolean forceUpperClustDist = true;
	
	/**
	 * Standard constructur with fixed frequency (no fitness threshold based frequency adaption)
	 */
	public CBNPSO() {
		this(false);
	}
	
	public CBNPSO(boolean threshAdaption) {
		this(10, 15, 0.001, 1e-10, 15, 100, threshAdaption);
//		super();
//		setDifferentiationCA(new ClusteringDensityBased(getClusterDiffDist(), 10, new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
//		setMergingCA(new ClusteringDensityBased(0.001, 3, new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
//		setEpsilonBound(1e-10);
//		setHaltingWindow(15);
//		setMaxSpeciesSize(15);
//		setOptimizer(new ParticleSwarmOptimization(100, 2.05, 2.05, PSOTopologyEnum.grid, 2));
//		ParamAdaption[] defAdpt = new ParamAdaption[]{getDefaultSinusoidalAdaption()};
//		setParameterControl(defAdpt);
//		if (threshAdaption) addParameterControl(getDefaultThreshAdaption());
//		setPopulationSize(100);
	}
	
	public CBNPSO(int minSpecSize, int maxSpecSize, double sigmaMerge, double epsilonConv, int haltingWindow, int popSize, boolean threshAdaption) {
		super();
		setDifferentiationCA(new ClusteringDensityBased(getClusterDiffDist(), minSpecSize, new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
		setMergingCA(new ClusteringDensityBased(sigmaMerge, 3, new IndividualDataMetric(ParticleSwarmOptimization.partBestPosKey)));
		setEpsilonBound(epsilonConv);
		setHaltingWindow(haltingWindow);
		setMaxSpeciesSize(maxSpecSize);
		setOptimizer(new ParticleSwarmOptimization(popSize, 2.05, 2.05, PSOTopologyEnum.grid, 2));
		ParamAdaption[] defAdpt = new ParamAdaption[]{getDefaultSinusoidalAdaption()};
		setParameterControl(defAdpt);
		if (threshAdaption) {
                addParameterControl(getDefaultThreshAdaption());
            }
		setPopulationSize(popSize);
	}
	
	private ParamAdaption getDefaultSinusoidalAdaption() {
		return new SinusoidalParamAdaption(0.1, 1., 10000, 0, "clusterDiffDist");
	}

	private ParamAdaption getDefaultThreshAdaption() {
		return new CbpsoFitnessThresholdBasedAdaption();
	}
	
	@Override
	public void setProblem(InterfaceOptimizationProblem problem) {
		super.setProblem(problem);
		if (problem instanceof AbstractProblemDouble) {
			AbstractProblemDouble dblProb = ((AbstractProblemDouble)problem);
			adaptMinMaxSwarmSizeByDim(dblProb);
		}
	}

	/**
	 * Return the period of the sinusoidal sigma adaption or -1 if not applicable.
	 * 
	 * @param p
	 */
	public int getSigmaAdaptionPeriod() {
		ParamAdaption[] prmAd = getParameterControl();
		for (int i=0; i<prmAd.length; i++) {
			if (prmAd[i] instanceof SinusoidalParamAdaption) {
				if (prmAd[i].getControlledParam().equals("clusterDiffDist")) {
                                return ((SinusoidalParamAdaption)prmAd[i]).getIterationPeriod();
                            }
			}
		}
		return -1;
	}
	
	/**
	 * Set the period of the sinusoidal sigma adaption, if a fitting ParamAdaption
	 * instance is found. Otherwise, nothing happens. 
	 * @param p
	 */
	public void setSigmaAdaptionPeriod(int p) {
		ParamAdaption[] prmAd = getParameterControl();
		for (int i=0; i<prmAd.length; i++) {
			if (prmAd[i] instanceof SinusoidalParamAdaption) {
				if (prmAd[i].getControlledParam().equals("clusterDiffDist")) {
					((SinusoidalParamAdaption)prmAd[i]).setIterationPeriod(p);
					return;
				}
			}
		}
		System.err.println("Error: unable to set adaption period " + p + ", no sinusoidal adaption instance found.");
	}
	
	public void setSigmaAdaptionShift(int s) {
		ParamAdaption[] prmAd = getParameterControl();
		for (int i=0; i<prmAd.length; i++) {
			if (prmAd[i] instanceof SinusoidalParamAdaption) {
				if (prmAd[i].getControlledParam().equals("clusterDiffDist")) {
					((SinusoidalParamAdaption)prmAd[i]).setInitialShift(s);
					return;
				}
			}
		}
		System.err.println("Error: unable to set adaption shift " + s + ", no sinusoidal adaption instance found.");
	}
	
	private void adaptMinMaxSwarmSizeByDim(AbstractProblemDouble dblProb) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		super.init();
		if (getProblem() instanceof InterfaceProblemDouble) {
			if (isForceUpperClustDist()) {
                        setUpperBoundClustDiff((InterfaceProblemDouble)getProblem());
                    }
		} else {
                System.err.println("Can set upper bound of clustering parameter only for AbstractProblemDouble types, not for " + getProblem().getClass().getName());
            }
	}
	
	@Override
	public String getName() {
		if (getParameterControl().length>0) {
			String addInfo="adpt";
			if (getParameterControl()[0] instanceof SinusoidalParamAdaption) {
                        addInfo="SinT"+((SinusoidalParamAdaption)getParameterControl()[0]).getIterationPeriod();
                    }
			else if (getParameterControl()[0] instanceof LinearParamAdaption) {
                        addInfo="Lin"+((LinearParamAdaption)getParameterControl()[0]).getStartV()+"-"+((LinearParamAdaption)getParameterControl()[0]).getEndV();
                    }
			return "CBN-PSO-"+addInfo;
		} else {
                return "CBN-PSO";
            }
	}
	
	public static String globalInfo() {
		return "A CBN-EA variant employing PSO and dynamic variation of the clustering parameter by default.";
	}

	public void setForceUpperClustDist(boolean forceUpperClustDist) {
		this.forceUpperClustDist = forceUpperClustDist;
	}
	public boolean isForceUpperClustDist() {
		return forceUpperClustDist;
	}
	public String forceUpperClustDistTipText() {
		return "Activate to force cluster distance to be maximal corresponding to the CBN-PSO settings."; 
	}
	
	/**
	 * Return the ratio of interesting solutions per archived solutions, which
	 * is in [0,1] if any solutions have been identified, or -1 if the archive
	 * is empty.
	 * 
	 * @param cbpso
	 * @param pop
	 * @param iteration
	 * @param maxIteration
	 * @return
	 */
	public double getInterestingSolutionRatio() {
		InterfaceOptimizationProblem prob = getProblem();
		double fitThres = 100; 
		if (prob instanceof InterfaceInterestingHistogram) {
			fitThres = ((InterfaceInterestingHistogram)prob).getHistogram().getUpperBound();
		} else {
                EVAERROR.errorMsgOnce("Warning, problem does not define a fitness threshold!");
            }
		
		SolutionSet solSet = getAllSolutions();
		Population archived = solSet.getSolutions();
		Population interesting = archived.filterByFitness(fitThres, 0);
		
//		Population archived = getArchivedSolutions();
//		Population interesting = archived.filterByFitness(fitThres, 0);
		
		if (archived.size()>0) {
			return ((double)interesting.size())/((double)archived.size());
		} else {
                return -1;
            }
	}

	@Override
	public String[] getAdditionalDataHeader() {
		String[] addVals = {"interestingRatio"};
		if (getCurrentPeriod()>=0) {
                addVals = new String[]{"interestingRatio", "adaptPeriod"};
            } 
		return ToolBox.appendArrays(super.getAdditionalDataHeader(), addVals);
	}

	@Override	
	public String[] getAdditionalDataInfo() {
		String[] addVals = {"Ratio of interesting solutions within all archived solutions"};
		if (getCurrentPeriod()>=0) {
                addVals = new String[]{"Ratio of interesting solutions within all archived solutions", 
     "Current sigma adaptation period"};
            } 
		return ToolBox.appendArrays(super.getAdditionalDataInfo(), addVals);
	}
	
	/**
	 * Retrieve the current period of the sinusoidal sigma adaption (in case it is controlled by a threshold adaption)
	 * or -1 if this does not apply.
	 *  
	 * @return
	 */
	private int getCurrentPeriod() {
		ParamAdaption[] adaptors = super.getParameterControl();
		SinusoidalParamAdaption sinA=null;
		CbpsoFitnessThresholdBasedAdaption ftA=null;
		if (adaptors!=null) {
                for (int i = 0; i<adaptors.length; i++) {
                    if (adaptors[i] instanceof SinusoidalParamAdaption) {
                        sinA = (SinusoidalParamAdaption)adaptors[i];
                    } else if (adaptors[i] instanceof CbpsoFitnessThresholdBasedAdaption) {
                        ftA = (CbpsoFitnessThresholdBasedAdaption)adaptors[i];
                    }
                }
            }
		if (sinA!=null && (ftA!=null)) {
                return sinA.getIterationPeriod();
            }
		else {
                return -1;
            }
	}

	@Override
	public Object[] getAdditionalDataValue(PopulationInterface pop) {
		Object[] addVals = null;
		double freq = getCurrentPeriod();
		if (freq>=0) {
                addVals = new Object[]{getInterestingSolutionRatio(), freq};
            }
		else {
                addVals = new Object[]{getInterestingSolutionRatio()};
            }
		return ToolBox.appendArrays(super.getAdditionalDataValue(pop), addVals);
	}
}
