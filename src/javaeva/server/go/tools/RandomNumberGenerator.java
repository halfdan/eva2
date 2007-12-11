package javaeva.server.go.tools;

import java.util.Random;
import java.util.ArrayList;

public class RandomNumberGenerator extends Random {
  private static Random random;
  private static long randomSeed;
  //private static int counter =0;
  /**
   *
   */
  static {
    randomSeed=System.currentTimeMillis();
    //randomSeed=(long)100.0;
    //System.out.println("randomSeed ="+ randomSeed);
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setseed(long x) {
    //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!seeeeed"+x+"counter"+counter);
    //counter=0;
    randomSeed=x;
    if (x==0)
      randomSeed=System.currentTimeMillis();
//    if (x==999) // ??? removed (MK)
//      return;
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setRandomseed() {
    //counter++;
    randomSeed=System.currentTimeMillis();
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setRandom(Random base_random) {
  //counter++;
    random=base_random;
  }
  /**
   *
   */
  public static void setRandomSeed(long new_seed){
     //counter++;
    randomSeed=new_seed;
    random.setSeed(randomSeed);
  }
  /**
   *
   */
  public static long getRandomSeed() {
     //counter++;
    return randomSeed;
  }
  
  /**
   * Returns 0 or 1 evenly distributed.
   */
  public static int randomInt() {
     //counter++;
    return randomInt(0,1);
  }

  /**
  * Returns an evenly distributes int value between zero and
  * upperLim-1.
  * @param upperLim upper exclusive limit of the random int
  */
 public static int randomInt(int upperLim) {
    //counter++;
   return randomInt(0,upperLim-1);
 }
 
  /** This method returns a evenly distributed int value.
   * The boundarys are included.
   * @param lo         Lower bound.
   * @param hi         Upper bound.
   * @return int
   */
  public static int randomInt(int lo,int hi) {
     //counter++;
      int result = (Math.abs(random.nextInt())%(hi-lo+1))+lo;
      if (result < lo) {
          System.out.println("Result "+result+" < " +lo+"!?");
          result = lo;
      }
      if (result > hi) {
          System.out.println("Result "+result+" > " +hi+"!?");
          result = hi;
      }
    return result;
  }

    /** This method returns a random permutation of n int values
     * @param length        The number of int values
     * @return The permutation [0-length-1]
     */
    public static int[] randomPermutation(int length) {
        boolean[]   validList   = new boolean[length];
        int[]       result      = new int[length];
        int         index;
        for (int i = 0; i < validList.length; i++) validList[i] = true;
        for (int i = 0; i < result.length; i++) {
            index = randomInt(0, length-1);
            while (!validList[index]) {
                index++;
                if (index == length) index = 0;
            }
            validList[index] = false;
            result[i] = index;
        }
        return result;
    }
  /**
   *
   */
  public static long randomLong() {
    // counter++;
    return randomLong(0,1);
  }
  /**
   *
   */
  public static long randomLong(long lo,long hi) {
     //counter++;
    return (Math.abs(random.nextLong())%(hi-lo+1))+lo;
  }
  /**
   *
   */
  public static float randomFloat() {
     //counter++;
    return random.nextFloat();
  }
  /**
   *
   */
  public static float randomFloat(float lo,float hi) {
     //counter++;
    return (hi-lo)*random.nextFloat()+lo;
  }
  /**
   *
   */
  public static double randomDouble() {
   //counter++;
    return random.nextDouble();
  }
  /**
   *
   */
  public static double randomDouble(double lo,double hi) {
   //counter++;
    return (hi-lo)*random.nextDouble()+lo;
  }
  /**
   *
   */
  public static double[] randomDoubleArray(double[] lo,double[] hi) {
   //counter++;
    double[] xin = new double[lo.length];
    for (int i=0;i<lo.length;i++)
      xin[i] = (hi[i]-lo[i])*random.nextDouble()+lo[i];
    return xin;
  }
  /**
   *
   */
  public static double[] randomDoubleArray(double lo,double hi,int size) {
   //counter++;
    double[] xin = new double[size];
    for (int i=0;i<size;i++)
      xin[i] = (hi-lo)*random.nextDouble()+lo;
    return xin;
  }

  /**
   *
   */
  public static double[] randomDoubleArray(double[] lo,double[] hi,double[] xin) {
     //counter++;
     for (int i=0;i<lo.length;i++)
      xin[i] = (hi[i]-lo[i])*random.nextDouble()+lo[i];
    return xin;
  }
  /**
   *
   */
  public static boolean randomBoolean() {
     //counter++;
     return (randomInt()==1);
  }
  /**
   *
   */
  public static int randomBit() {
     //counter++;
     return randomInt();
  }
  /**
   *
   */
  public static boolean flipCoin(double p) {
     //counter++;
     return (randomDouble()<p ? true : false);
  }
  /**
   *
   */
  public static float gaussianFloat(float dev) {
     //counter++;
     return (float)random.nextGaussian()*dev;
  }
  /**
   *
   */
  public static double gaussianDouble(double dev) {
     //counter++;
     return random.nextGaussian()*dev;
  }
  /**
   *
   */
  public static float exponentialFloat(float mean) {
     //counter++;
     return (float)(-mean*Math.log(randomDouble()));
  }
  /**
   *
   */
  public static double exponentialDouble(double mean) {
     //counter++;
     return -mean*Math.log(randomDouble());
  }
  
  /**
   * Returns a vector denoting a random point around the center
   * - inside a hypersphere of uniform distribution if nonUnif=0,
   * - inside a hypersphere of non-uniform distribution if nonUnif > 0,
   * - inside a D-Gaussian if nonUnif < 0.
   * For case 2, the nonUnif parameter is used as standard deviation (instead of 1/D), the parameter
   * is not further used in the other two cases.
   * Original code by Maurice Clerc, from the TRIBES package
   * 
   * @param center	center point of the distribution
   * @param radius	radius of the distribution
   * @param nonUnif		kind of distribution 
   * 
   **/
  public static double[] randHypersphere(double[] center, double radius, double nonUnif) {
	  double[] x = new double[center.length];
	  int 	j;
	  double  xLen, r;
	  int D=center.length;

//	  ----------------------------------- Step 1.  Direction
	  xLen = 0;
	  for (j=0; j<D; j++) {
		  r = gaussianDouble(1);
		  x[j] = r;
		  xLen += x[j]*x[j];
	  }

	  xLen=Math.sqrt(xLen);

	  //----------------------------------- Step 2.   Random radius

	  r=randomDouble();
	  if (nonUnif < 0) r = gaussianDouble(r/2); // D-Gaussian
	  else if (nonUnif > 0) r = Math.pow(r,nonUnif);  // non-uniform hypersphere
	  else r=Math.pow(r,1./D); // Real hypersphere

	  for (j=0;j<D;j++) {
		  x[j] = center[j]+radius*r*x[j]/xLen;
	  }
	  return x;
  }
}

