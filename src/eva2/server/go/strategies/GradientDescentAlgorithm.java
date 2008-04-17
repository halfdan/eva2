package eva2.server.go.strategies;

import java.util.*;

import eva2.server.go.InterfacePopulationChangedEventListener;
import eva2.server.go.individuals.AbstractEAIndividual;
import eva2.server.go.individuals.InterfaceDataTypeDouble;
import eva2.server.go.populations.Population;
import eva2.server.go.problems.F1Problem;
import eva2.server.go.problems.InterfaceFirstOrderDerivableProblem;
import eva2.server.go.problems.InterfaceOptimizationProblem;



/** A gradient descent algorithm by hannes planatscher don't expect any
 * descriptions here... *big sigh*
 * <p>Title: The JavaEvA</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class GradientDescentAlgorithm implements InterfaceOptimizer, java.io.Serializable {

  private InterfaceOptimizationProblem m_Problem;

  InterfaceDataTypeDouble m_Best, m_Test;
  private int           iterations = 1;
  private double        localnminus = 0.5;
  private double        localnplus = 1.1;
  boolean               recovery = false;
  private int           recoverylocksteps = 5;
  private double        recoverythreshold = 100000;
  boolean               localstepsizeadaption = true;
  boolean               globalstepsizeadaption = true;
  private double        globalinitstepsize = 1;
  double                globalmaxstepsize = 3.0;
  double                globalminstepsize = 0.1;
  boolean               manhattan = false;
  double                localmaxstepsize = 10;
  double                localminstepsize = 0.1;
  private boolean       momentumterm = false;
  transient private InterfacePopulationChangedEventListener m_Listener;
  public double         maximumabsolutechange = 0.2;
  Hashtable             indyhash;

  // These variables are necessary for the more complex LectureGUI enviroment
  transient private String m_Identifier = "";
  private Population m_Population;
  private InterfaceDataTypeDouble InterfaceDataTypeDouble;

  public void initByPopulation(Population pop, boolean reset) {
    this.setPopulation((Population) pop.clone());
    if (reset) this.getPopulation().init();
    this.m_Problem.evaluate(this.getPopulation());
    this.firePropertyChangedEvent("NextGenerationPerformed");
    //System.out.println("initByPopulation() called");
    indyhash = new Hashtable();
  }

  public GradientDescentAlgorithm() {
    indyhash = new Hashtable();
    this.m_Population = new Population();
    this.m_Population.setPopulationSize(1);
  }


  public Object clone() {
    /**@todo Implement this javaeva.server.oa.go.Strategies.InterfaceOptimizer method*/
    throw new java.lang.UnsupportedOperationException("Method clone() not yet implemented.");
  }

  public String getName() {
    return "GradientDescentAlgorithm";
  }


  public void init() {
    //System.out.println("init() called ");
    indyhash = new Hashtable();
    this.m_Problem.initPopulation(this.m_Population);
    this.m_Problem.evaluate(this.m_Population);
  }


  public double signum(double val) {
    return (val < 0) ? -1 : 1;
  }

  public void optimize() {
   //  System.out.println("opt. called");
    AbstractEAIndividual indy;
      if ((this.indyhash == null) || (this.indyhash.size() <1)) init();

    for (int i = 0; i < this.m_Population.size(); i++) {
      indy = ((AbstractEAIndividual)this.m_Population.get(i));
      if (!indyhash.containsKey(indy)) {
        //System.out.println("new indy to hash");
        Hashtable history = new Hashtable();
        int[] lock = new int[((InterfaceDataTypeDouble) indy).getDoubleData().length];
        double[] wstepsize = new double[((InterfaceDataTypeDouble) indy).getDoubleData().length];
        for (int li = 0; li < lock.length; li++) lock[li] = 0;
        for (int li = 0; li < lock.length; li++) wstepsize[li] = 1.0;
        double fitness = 0;
        history.put("lock", lock);
        history.put("lastfitness", new Double(fitness));
        history.put("stepsize", new Double(globalinitstepsize));
        history.put("wstepsize", wstepsize);
        indyhash.put(indy, history);
      } else {
        //System.out.println("indy already in hash");
      }
     }
    // System.out.println("hashtable built");
    for (int i = 0; i < this.m_Population.size(); i++) {

      indy = ((AbstractEAIndividual)this.m_Population.get(i));
      double[][] range = ((InterfaceDataTypeDouble) indy).getDoubleRange();
      double[] params = ((InterfaceDataTypeDouble) indy).getDoubleData();

      int[] lock = (int[]) ((Hashtable) indyhash.get(indy)).get("lock");
      double indystepsize = ((Double) ((Hashtable) indyhash.get(indy)).get("stepsize")).doubleValue();
   //   System.out.println("indystepsize" + indystepsize);

      if ((this.m_Problem instanceof InterfaceFirstOrderDerivableProblem) && (indy instanceof InterfaceDataTypeDouble)) {
        Hashtable history = (Hashtable) indyhash.get(indy);
        for (int iterations = 0; iterations < this.iterations; iterations++) {

          double[] oldgradient = (double[]) history.get("gradient");
          double[] wstepsize = (double[]) history.get("wstepsize");
          double[] oldchange = null;

          double[] gradient = ((InterfaceFirstOrderDerivableProblem) m_Problem).getFirstOrderGradients(params);
          if ((oldgradient != null) && (wstepsize != null)) {
            for (int li = 0; li < wstepsize.length; li++) {
              double prod = gradient[li] * oldgradient[li];
              if (prod < 0) {
                wstepsize[li] = localnminus * wstepsize[li];
              } else if (prod > 0) {
                wstepsize[li] = localnplus * wstepsize[li];
              }
              wstepsize[li] = (wstepsize[li] < localminstepsize) ? localminstepsize : wstepsize[li];
              wstepsize[li] = (wstepsize[li] > localmaxstepsize) ? localmaxstepsize : wstepsize[li];

              //System.out.println("wstepsize "+ li + " " + wstepsize[li]);
            }

          }
          double[] newparams = new double[params.length];
          history.put("gradient", gradient);
          double[] change = new double[params.length];
          if (history.containsKey("changes")) {
            oldchange =(double[]) history.get("changes");
          }
          boolean dograddesc = (this.momentumterm) && (oldchange != null);

          for (int j = 0; j < newparams.length; j++) {
            if (lock[j] == 0) {
              double tempstepsize = 1;
              if (this.localstepsizeadaption) tempstepsize = tempstepsize *wstepsize[j];
              if (this.globalstepsizeadaption) tempstepsize = tempstepsize *indystepsize;
              double wchange = signum(tempstepsize * gradient[j]) * Math.min(maximumabsolutechange,Math.abs(tempstepsize * gradient[j])); //indystepsize * gradient[j];
              if (this.manhattan) wchange = this.signum(wchange) * tempstepsize;
              if (dograddesc)  {
                wchange = wchange + this.momentumweigth * oldchange[j];
              }
              newparams[j] = params[j] - wchange;
              if (newparams[j] < range[j][0]) newparams[j] = range[j][0];
              if (newparams[j] > range[j][1]) newparams[j] = range[j][1];
//              for (int g = 0; g < newparams.length; g++) {
//                System.out.println("Param " + g +": " + newparams[g]);
//              }
              change[j] += wchange;
            } else {
              lock[j]--;
            }
          }
          params = newparams;

          history.put("changes", change);

        }

        ((InterfaceDataTypeDouble) indy).SetDoubleDataLamarkian(params);

      }
    }

    this.m_Problem.evaluate(this.m_Population);

    if (this.recovery) {
      for (int i = 0; i < this.m_Population.size(); i++) {
        indy = ((AbstractEAIndividual)this.m_Population.get(i));
        Hashtable history = (Hashtable) indyhash.get(indy);
        if (indy.getFitness()[0] > recoverythreshold) {
          System.out.println("Gradient Descent: Fitness critical:" + indy.getFitness()[0]);
          ((InterfaceDataTypeDouble) indy).SetDoubleData((double[]) history.get("params"));
          double[] changes = (double[]) history.get("changes");
          int[] lock = (int[]) history.get("lock");

          int indexmaxchange = 0;
          double maxchangeval = Double.NEGATIVE_INFINITY;
          for (int j = 0; j < changes.length; j++) {
            if ((changes[j] > maxchangeval) && (lock[j] == 0)) {
              indexmaxchange = j;
              maxchangeval = changes[j];
            }
          }
          lock[indexmaxchange] = recoverylocksteps;
          history.put("lock", lock);
        } else {
        }
      }
      this.m_Problem.evaluate(this.m_Population);
    }

    if (this.globalstepsizeadaption) {

      //System.out.println("gsa main");
      for (int i = 0; i < this.m_Population.size(); i++) {
        indy = ((AbstractEAIndividual)this.m_Population.get(i));
        Hashtable history = (Hashtable) indyhash.get(indy);
        if (history == null) break;
        if (history.get("lastfitness") != null) {
          double lastfit = ((Double) history.get("lastfitness")).doubleValue();
          double indystepsize = ((Double) history.get("stepsize")).doubleValue();

            if (lastfit < indy.getFitness()[0]) {
              indystepsize *= 0.5;
            } else {
              indystepsize *= 1.1;
            }
//System.out.println("newstepsize" + indystepsize);
          indystepsize = (indystepsize > globalmaxstepsize) ? globalmaxstepsize : indystepsize;
          indystepsize = (indystepsize < globalminstepsize) ? globalminstepsize : indystepsize;
          history.put("stepsize", new Double(indystepsize));
        }

//System.out.println("newstepsize in bounds" + indystepsize);
        history.put("lastfitness", new Double(indy.getFitness()[0]));
      }

    }


    this.firePropertyChangedEvent("NextGenerationPerformed");
  }

  private double momentumweigth = 0.1;

  protected void firePropertyChangedEvent(String name) {
    if (this.m_Listener != null)this.m_Listener.registerPopulationStateChanged(this, name);
  }

  public Population getPopulation() {
    return this.m_Population;
  }
  
  public Population getAllSolutions() {
  	return getPopulation();
  }

  public void setPopulation(Population pop) {
    Hashtable newindyhash = new Hashtable();
    for (int i = 0; i < pop.size(); i++) {
      if (indyhash.contains(pop.get(i))) newindyhash.put(pop.get(i), indyhash.get(pop.get(i)));
    }
    indyhash = newindyhash;
    this.m_Population = pop;
  }


  /** This method allows you to set an identifier for the algorithm
   * @param name      The indenifier
   */
  public void SetIdentifier(String name) {
    this.m_Identifier = name;
  }

  public String getIdentifier() {
    return this.m_Identifier;
  }

  public void SetProblem(InterfaceOptimizationProblem problem) {

    m_Problem = problem;
  }

  public InterfaceOptimizationProblem getProblem() {
    return m_Problem;
  }

  public String getStringRepresentation() {
    return "GradientDescentAlgorithm";
  }


  public void addPopulationChangedEventListener(InterfacePopulationChangedEventListener ea) {
    this.m_Listener = ea;
  }

  public static void main(String[] args) {
    GradientDescentAlgorithm program = new GradientDescentAlgorithm();
    InterfaceOptimizationProblem problem = new F1Problem();
    program.SetProblem(problem);
    program.init();
    for (int i = 0; i < 100; i++) {
      program.optimize();
      System.out.println(program.getPopulation().getBestFitness()[0]);
    }
    double[] res = ((InterfaceDataTypeDouble) program.getPopulation().getBestIndividual()).getDoubleData();
    for (int i = 0; i < res.length; i++) {
      System.out.print(res[i] + " ");
    }
  }



  public void freeWilly() {

  }


  public double getGlobalMaxstepsize() {
    return globalmaxstepsize;
  }

  public void setGlobalMaxstepsize(double p) {
    globalmaxstepsize = p;
  }


  public double getGlobalMinstepsize() {
    return globalminstepsize;
  }

  public void setGlobalMinstepsize(double p) {
    globalminstepsize = p;
  }

  public int getRecoveryLocksteps() {
    return recoverylocksteps;
  }

  public void setRecoveryLocksteps(int locksteps) {
    this.recoverylocksteps = locksteps;
  }

  public double getGlobalInitstepsize() {
    return globalinitstepsize;
  }

  public void setGlobalInitstepsize(double initstepsize) {
    this.globalinitstepsize = initstepsize;
  }
  public boolean isLocalStepsizeadaption() {
    return localstepsizeadaption;
  }
  public void setLocalStepsizeadaption(boolean stepsizeadaption) {
    this.localstepsizeadaption = stepsizeadaption;
  }
  public boolean isRecovery() {
    return recovery;
  }
  public void setRecovery(boolean recovery) {
    this.recovery = recovery;
  }
  public double getLocalNplus() {
    return localnplus;
  }
  public double getLocalNminus() {
    return localnminus;
  }
  public void setLocalNplus(double nplus) {
    this.localnplus = nplus;
  }
  public void setLocalNminus(double nminus) {
    this.localnminus = nminus;
  }
  public int getIterations() {
    return iterations;
  }
  public void setIterations(int iterations) {
    this.iterations = iterations;
  }
  public boolean isGlobalstepsizeadaption() {
    return globalstepsizeadaption;
  }
  public void setGlobalstepsizeadaption(boolean globalstepsizeadaption) {
    this.globalstepsizeadaption = globalstepsizeadaption;
  }
  public double getLocalminstepsize() {
    return localminstepsize;
  }
  public double getLocalmaxstepsize() {
    return localmaxstepsize;
  }
  public void setLocalminstepsize(double localminstepsize) {
    this.localminstepsize = localminstepsize;
  }
  public void setLocalmaxstepsize(double localmaxstepsize) {
    this.localmaxstepsize = localmaxstepsize;
  }
  public boolean isManhattan() {
    return manhattan;
  }



  public boolean isMomentumTerm() {
    return momentumterm;
  }

  public double getMomentumweigth() {
    return momentumweigth;
  }

  public void setManhattan(boolean manhattan) {
    this.manhattan = manhattan;
  }



  public void setMomentumTerm(boolean momentum) {
    this.momentumterm = momentum;
  }

  public void setMomentumweigth(double momentumweigth) {
    this.momentumweigth = momentumweigth;
  }

  public void setPopulationSize(int p) {
    this.getPopulation().setPopulationSize(p);
  }

  public int GetPopulationSize() {
    return this.getPopulation().getPopulationSize();
  }

  public double getRecoverythreshold() {
    return recoverythreshold;
  }
  public void setRecoverythreshold(double recoverythreshold) {
    this.recoverythreshold = recoverythreshold;
  }

  public double getMaximumabsolutechange() {
    return maximumabsolutechange;
  }


  public void setMaximumabsolutechange(double maximumabsolutechange) {
    this.maximumabsolutechange = maximumabsolutechange;
  }

}
