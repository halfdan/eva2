package wsi.ra.math;
/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:40 $
 *            $Author: ulmerh $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.util.Random;
/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/
/**
 *
 */
public class RNG extends Random {
  private static Random random;
  private static long randomSeed;
  /**
   *
   */
  static {
    randomSeed=System.currentTimeMillis();
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setseed(long x) {
    randomSeed=x;
    if (x==0)
      randomSeed=System.currentTimeMillis();
    if (x==999)
      return;
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setRandomseed() {
    randomSeed=System.currentTimeMillis();
    random=new Random(randomSeed);
  }
  /**
   *
   */
  public static void setRandom(Random base_random) {
    random=base_random;
  }
  /**
   *
   */
  public static void setRandomSeed(long new_seed){
    randomSeed=new_seed;
    random.setSeed(randomSeed);
  }
  /**
   *
   */
  public static long getRandomSeed() {
    return randomSeed;
  }
  /**
   *
   */
  public static int randomInt() {
    return randomInt(0,1);
  }
  public static int randomInt(int lo,int hi) {
    return (Math.abs(random.nextInt())%(hi-lo+1))+lo;
  }
  /**
   *
   */
  public static long randomLong() {
    return randomLong(0,1);
  }
  /**
   *
   */
  public static long randomLong(long lo,long hi) {
    return (Math.abs(random.nextLong())%(hi-lo+1))+lo;
  }
  /**
   *
   */
  public static float randomFloat() {
    return random.nextFloat();
  }
  /**
   *
   */
  public static float randomFloat(float lo,float hi) {
    return (hi-lo)*random.nextFloat()+lo;
  }
  /**
   *
   */
  public static double randomDouble() {
    return random.nextDouble();
  }
  /**
   *
   */
  public static double randomDouble(double lo,double hi) {
    return (hi-lo)*random.nextDouble()+lo;
  }
  /**
   *
   */
  public static double[] randomDoubleArray(double[] lo,double[] hi) {
    double[] xin = new double[lo.length];
    for (int i=0;i<lo.length;i++)
      xin[i] = (hi[i]-lo[i])*random.nextDouble()+lo[i];
    return xin;
  }
  /**
   *
   */
  public static double[] randomDoubleArray(double lo,double hi,int size) {
    double[] xin = new double[size];
    for (int i=0;i<size;i++)
      xin[i] = (hi-lo)*random.nextDouble()+lo;
    return xin;
  }

  /**
   *
   */
  public static double[] randomDoubleArray(double[] lo,double[] hi,double[] xin) {
     for (int i=0;i<lo.length;i++)
      xin[i] = (hi[i]-lo[i])*random.nextDouble()+lo[i];
    return xin;
  }
  /**
   *
   */
  public static boolean randomBoolean() {
     return (randomInt()==1);
  }
  /**
   *
   */
  public static int randomBit() {
     return randomInt();
  }
  /**
   *
   */
  public static boolean flipCoin(double p) {
     return (randomDouble()<p ? true : false);
  }
  /**
   *
   */
  public static float gaussianFloat(float dev) {
     return (float)random.nextGaussian()*dev;
  }
  /**
   *
   */
  public static double gaussianDouble(double dev) {
     return random.nextGaussian()*dev;
  }
  /**
   *
   */
  public static float exponentialFloat(float mean) {
     return (float)(-mean*Math.log(randomDouble()));
  }
  /**
   *
   */
  public static double exponentialDouble(double mean) {
     return -mean*Math.log(randomDouble());
  }
}

