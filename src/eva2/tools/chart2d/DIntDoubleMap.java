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
 * This interface is used by DPointSet to store values as images of integer
 * values which can be displayed in a DArea. All modifications of the values can
 * only be shown by the DArea, when the DPointSet.repaint method is
 * called. This should be done in the implementation of the #setImage and
 * #addImage method. To get the functionality of the DArea, the methods
 * #getMaxImageValue and #getMinImageValue are necessary.
 */
public interface DIntDoubleMap {

    /**
     * implementation should change the map in that way, that from now the
     * image of index is v
     *
     * @param source the preimage of the image
     * @param image  the new image of the source value
     * @return <code>true<\code> when the minmal or the maximal image value
     *         has been changed by this method call, else it returns
     *         <code>false</code> @see #getMinImageValue(), #getMaxImageValue()
     */
    boolean setImage(int source, double image);

    /**
     * implementation should return the image of source
     *
     * @param source the preimage
     * @return the image value
     */
    double getImage(int source);

    /**
     * the image of the highest source value + 1 should be the given image value
     *
     * @param image the new image value
     * @return <code>true<\code> when the minmal or the maximal image value
     *         has been changed by this method call, else it returns
     *         <code>false</code> @see #getMinImageValue(), #getMaxImageValue()
     */
    boolean addImage(double image);

    /**
     * implementation should return the number of source values
     *
     * @return the number of source values
     */
    int getSize();

    /**
     * returns the maximal image value
     *
     * @return the maximal image value
     * @throw an IllegalArgumentException when it has no image values
     */
    double getMaxImageValue();

    /**
     * returns the minimal image value
     *
     * @return the minmal image value
     * @throw an IllegalArgumentException when it has no image values
     */
    double getMinImageValue();

    /**
     * returns the minimal image value
     *
     * @return the minmal image value
     * @throw an IllegalArgumentException when it has no image values
     */
    double getMinPositiveImageValue();

    /**
     * checks the minimal and the maximal image values and returns <code>true</code>
     * when at least one of them has changed
     *
     * @return <code>true</code> when either the maximal image value or the
     *         minmal image value has changed
     */
    boolean restore();

    /**
     * method removes all image values and sets the size to 0
     */
    void reset();
}