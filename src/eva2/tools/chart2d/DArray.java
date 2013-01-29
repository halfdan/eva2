package eva2.tools.chart2d;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

/**
 * This class stores an array of double values.
 * It can be used as data stack for an DPointSet object.
 * For that use, it is important to tell the DPoitSet object, when the data has
 * been modified.
 */
public class DArray implements DIntDoubleMap{
  private int initial_capacity, size;
  private double capacity_multiplier = 2, max, min, minPositive=-1;
  private double[] value;

  /**
   * Constructor for an DArray object with default values for the initial
   * capacity (5) and the capacity multiplier (2).
   */
  public DArray(){
    this(5, 2);
  }

  /**
   * Constructor for an DArray object with default value for the capacity
   * multiplier (2).
   *
   * @param initial_capacity the initial capacity of the array
   */
  public DArray(int initial_capacity){
    this(initial_capacity, 2);
  }

  /**
   * Constructor for an DArray object
   *
   * @param initial_capacity the initial capacity of the array
   * @param capacity_multiplier the capacity multiplier when the array overflows
   */
  public DArray(int initial_capacity, double capacity_multiplier){
    if( initial_capacity < 1 ) throw
      new IllegalArgumentException("The initial capacity has to be at least 1");
    if( capacity_multiplier <= 1 ) throw
      new IllegalArgumentException("The capacity multiplier has to be bigger than 1");
    this.initial_capacity = initial_capacity;
    value = new double[initial_capacity];
    this.capacity_multiplier = capacity_multiplier;
  }

  /**
   * method sets the image value to the given source value
   *
   * @param source the source value
   * @param image  the image value
   */
    @Override
  public boolean setImage(int source, double image){
    if(source<0 || source>=size) throw
      new ArrayIndexOutOfBoundsException(source);
    boolean min_max_changed = false, restore = false;
    if( image < min ){ min = image; min_max_changed = true; }
    else if( image > max ){ max = image; min_max_changed = true; }
    if( value[source] == min || value[source] == max || (value[source]==minPositive)) restore = true;
    value[source] = image;
    if( restore ) min_max_changed = restore() || min_max_changed;
    return min_max_changed;
  }

  /**
   * method returns the image value of the given source value
   *
   * @param source the source value
   * @return the image value
   */
//  public double getImage(int source){
//    if(source<0 || source>=size) throw
//      new ArrayIndexOutOfBoundsException(source);
//    return value[source];
//  }
    @Override
    public double getImage(int source){
    if(source<0) new ArrayIndexOutOfBoundsException(source);
    if(source>=size && size > 1) return value[size-1];
    return value[source];
  }

  /**
   * the given image value becomes the image of (highest source value + 1)
   *
   * @param image the new image value
   * @return <code>true<\code> when the minmal or the maximal image value
   *          has been changed by this method call, else it returns
   *          <code>false</code> @see #getMinImageValue(), #getMaxImageValue()
   */
    @Override
  public boolean addImage(double image){
    if( size >= value.length ){
      int new_length = (int)(value.length * capacity_multiplier);
      if( !(new_length > value.length) ) new_length++;
      double[] new_val = new double[new_length];
      System.arraycopy(value,0,new_val,0,value.length);
      value = new_val;
    }
    boolean min_max_changed = false;
    if( size == 0 ){ min = image; max = image; min_max_changed = true; if (image>0) minPositive=image;}
    else {
      if ((image >0) && ((image<minPositive) || (minPositive<0))) { minPositive=image; min_max_changed=true;}
      if( image > max ) { max = image; min_max_changed = true; }
      else if( image < min ) { min = image; min_max_changed = true; }
    }
    value[size++] = image;
    return min_max_changed;
  }

  /**
   * method returns the number of (source,image)-pairs stored in the array
   *
   * @return the size of the source value set
   */
    @Override
  public int getSize(){
    return size;
  }

  /**
   * this method checks if the minimal and the maximal image value has changed
   *
   * @return <code>true</code> if one of them changed
   */
    @Override
  public boolean restore(){
    if( size == 0 ) return false;
    double old_min = min, old_max = max;
    min = value[0];
    max = value[0];
    minPositive=Double.POSITIVE_INFINITY;
    for( int i=1; i<size; i++ ) {
      if (value[i]>0 && (value[i]<minPositive)) minPositive=value[i];
      if( value[i] < min ) min = value[i];
      else if( value[i] > max ) max = value[i];
    }
    if (Double.isInfinite(minPositive)) minPositive=-1;
    return (old_min != min) || (old_max != max);
  }

  /**
   * throws all information away
   */
    @Override
  public void reset(){
    size = 0;
    value = new double[initial_capacity];
  }

  /**
   * returns the minimal image value
   *
   * @return the minimal image value
   */
    @Override
  public double getMinImageValue(){
    if( size == 0 ) throw
      new IllegalArgumentException("DArray is empty. No minimal value exists");
    return min;
  }

  /**
   * Return the minimal positive image value or the maximal value if none is positive. 
   * @return
   */
    @Override
  public double getMinPositiveImageValue() {
	  if ( size==0) throw
	  	new IllegalArgumentException("DArray is empty. No minimal value exists");
	  return (minPositive < 0) ? max : minPositive;
  }
  /**
   * returns the maxmal image value
   *
   * @return the maxmal image value
   */
    @Override
  public double getMaxImageValue(){
    if( size == 0 ) throw
      new IllegalArgumentException("DArray is empty. No maximal value exists");
    return max;
  }

  /**
   * method checks if the given object is equal to this DArray object.
   * It looks for differences in the stored image values
   *
   * @param o the object to compare with
   * @return <code>true</code> when the object is an DArray object, containing
   *          the same values
   */
    @Override
  public boolean equals(Object o){
    if( !(o instanceof DArray) ) return false;
    DArray comp = (DArray)o;
    if( comp.size != size ) return false;
    if( comp.max != max ) return false;
    if( comp.min != min ) return false;
    if( comp.minPositive != minPositive) return false;
    for( int i=0; i<size; i++ ) {
          if( comp.value[i] != value[i] ) return false;
      }
    return true;
  }

    @Override
  public String toString(){
    String text = "eva2.tools.chart2d.DArray[size:"+size;
    if( size < 11 )
      for( int i=0; i<size; i++ ) {
          text += ", "+value[i];
      }
    text += "]";
    return text;
  }

  public double[] toArray(double[] v){
    if( v == null || v.length < size ) v = new double[size];
    System.arraycopy(value,0,v,0,size);
    return v;
  }
}