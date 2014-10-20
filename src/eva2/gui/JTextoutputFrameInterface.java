package eva2.gui;

import eva2.optimization.stat.InterfaceTextListener;

/*
 *
 */
public interface JTextoutputFrameInterface extends InterfaceTextListener {

    /**
     * Set the show property to define whether the Output Frame should be shown.
     *
     * @param bShow Whether the frame should be shown or not
     */
    void setShow(boolean bShow);
}