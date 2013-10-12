/**
 *  Filename: $RCSfile: DComponent.java,v $
 *  Purpose:
 *  Language: Java
 *  Compiler: JDK 1.3
 *  Authors:  Fabian Hennecke
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:41 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package eva2.tools.chart2d;

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/

import java.awt.*;

/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 * <code>DComponent</code> is the mother of all objects which can be displayed
 * by a <code>DArea</code> object, even when it would be also enough to
 * implement the <code>DElement</code> interface to an class
 * <p/>
 * DComponent is abstract because the paint method has to be overridden
 */
public abstract class DComponent implements DElement {
    /**
     * the color of the component
     */
    protected Color color;

    /**
     * the rectangle in which the component lies
     */
    protected DRectangle rectangle;

    /**
     * the parent of the component which is responsible for repainting
     */
    protected DParent parent;


    private boolean visible = true;


    /**
     * this border respresents the additional space around the clip of the
     * graphics context, which is calculated by the union of all DRectangles of
     * the components. For example it is used by DPointIcons or DLabels.
     */
    private DBorder border = new DBorder();


    /**
     * this constructor is necessary to avoid infinite loops in constructing
     * DRectangles
     */
    DComponent(boolean is_rect) {
    }

    public DComponent() {
        rectangle = DRectangle.getEmpty();
    }

    /**
     * returns the rectangle in which the object lies
     */
    @Override
    public DRectangle getRectangle() {
        return (DRectangle) rectangle.clone();
    }


    /**
     * method sets a certain border around the contained rectangle
     *
     * @param b the new DBorder
     */
    @Override
    public void setDBorder(DBorder b) {
        if (parent != null) {
            if (border.insert(b)) {
                parent.addDBorder(b);
                repaint();
            } else {
                border = b;
                parent.restoreBorder();
            }
        } else {
            border = b;
        }
    }

    /**
     * method returns the current border around the rectangle
     *
     * @return the DBorder of the DComponent
     */
    @Override
    public DBorder getDBorder() {
        return border;
    }

    /**
     * sets the parent of the component, which should take care of painting the
     * component to the right time
     */
    @Override
    public void setDParent(DParent parent) {
        if (this.parent != null && this.parent != parent) {
            this.parent.removeDElement(this);
            this.parent.repaint(getRectangle());
        }
        this.parent = parent;
    }

    /**
     * returns the parent of the component
     */
    @Override
    public DParent getDParent() {
        return parent;
    }

    /**
     * invoces the parent to repaint the rectangle in which the component lies
     */
    @Override
    public void repaint() {
        //System.out.println("DComponent.repaint()");
        if (parent != null) {
            parent.repaint(getRectangle());
        }
    }

    /**
     * sets the color of the component
     */
    @Override
    public void setColor(Color color) {
        if (this.color == null || !this.color.equals(color)) {
            this.color = color;
            repaint();
        }
    }

    /**
     * returns the color of the component
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * sets the component visible or not
     */
    @Override
    public void setVisible(boolean aFlag) {
        boolean changed = (aFlag != visible);
        visible = aFlag;
        if (changed) {
            repaint();
        }
    }

    /**
     * returns if the component should be visible when the parent shows the right
     * area
     */
    @Override
    public boolean isVisible() {
        return visible;
    }

}

/****************************************************************************
 * END OF FILE
 ****************************************************************************/
