package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 201 $
 *            $Date: 2007-10-25 16:11:23 +0200 (Thu, 25 Oct 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

/**
 *
 */
public class JClassTree extends JDialog {
//    private ESPara m_Para;
    JScrollPane jScrollPane1 = new JScrollPane();
    JTree treeView_ = new JTree();
    JPanel treeControlsPanel = new JPanel();
    JButton expandButton = new JButton();
    JButton collapseButton = new JButton();
    JButton editButton = new JButton();
    JButton cancelButton = new JButton();
    // Used for addNotify check.
    private boolean fComponentsAdjusted = false;
   /**
     *  Default constructor.
     */
    public JClassTree() {
        getContentPane().setLayout(new BorderLayout(0, 0));
        setVisible(false);
        setSize(405, 362);
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane1.setOpaque(true);
        getContentPane().add(BorderLayout.CENTER, jScrollPane1);
        treeView_ = new JTree();
        treeView_.setBounds(0, 0, 402, 324);
        treeView_.setFont(new Font("Dialog", Font.PLAIN, 12));
        treeView_.setBackground(java.awt.Color.white);
        jScrollPane1.getViewport().add(treeView_);
        treeControlsPanel = new javax.swing.JPanel();
        treeControlsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        treeControlsPanel.setFont(new Font("Dialog", Font.PLAIN, 12));
        treeControlsPanel.setForeground(java.awt.Color.black);
        treeControlsPanel.setBackground(new java.awt.Color(204, 204, 204));
        getContentPane().add(BorderLayout.SOUTH, treeControlsPanel);
        expandButton = new javax.swing.JButton();
        expandButton.setText("Expand All");
        expandButton.setActionCommand("Expand All");
        expandButton.setFont(new Font("Dialog", Font.BOLD, 12));
        expandButton.setBackground(new java.awt.Color(204, 204, 204));
        treeControlsPanel.add(expandButton);
        collapseButton = new javax.swing.JButton();
        collapseButton.setText("Collapse All");
        collapseButton.setActionCommand("Collapse All");
        collapseButton.setFont(new Font("Dialog", Font.BOLD, 12));
        collapseButton.setBackground(new java.awt.Color(204, 204, 204));
        treeControlsPanel.add(collapseButton);
        editButton = new javax.swing.JButton();
        editButton.setText("Edit Selected");
        editButton.setActionCommand("Edit Selected");
        editButton.setFont(new Font("Dialog", Font.BOLD, 12));
        editButton.setBackground(new java.awt.Color(204, 204, 204));
        treeControlsPanel.add(editButton);
        cancelButton = new javax.swing.JButton();
        cancelButton.setText("Close");
        cancelButton.setActionCommand("Close");
        cancelButton.setFont(new Font("Dialog", Font.BOLD, 12));
        cancelButton.setBackground(new java.awt.Color(204, 204, 204));
        treeControlsPanel.add(cancelButton);
        setTitle("Class View");

        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        SymAction lSymAction = new SymAction();
        expandButton.addActionListener(lSymAction);
        collapseButton.addActionListener(lSymAction);
        editButton.addActionListener(lSymAction);
        cancelButton.addActionListener(lSymAction);

        MouseListener ml =
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int selRow = treeView_.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = treeView_.getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        if ((e.getClickCount() == 2) ||
                                ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)) {
                            doubleClick(selRow, selPath);
                        }
                    }
                }
            };
        treeView_.addMouseListener(ml);
        //
        Insets inset = new Insets(0, 5, 0, 5);
        expandButton.setMargin(inset);
        collapseButton.setMargin(inset);
        editButton.setMargin(inset);
        cancelButton.setMargin(inset);
    }


    /**
     *  Description of the Method
     *
     *@param  selRow   Description of the Parameter
     *@param  selPath  Description of the Parameter
     */
    void doubleClick(int selRow, TreePath selPath) {
        System.out.println("row " + selRow + " selected");
        Object[] objs = selPath.getPath();
        Object thing = ((DefaultMutableTreeNode) objs[objs.length - 1]).getUserObject();
        showProperties(thing);
    }


    /**
     *  Constructor for the JClassTree object
     *
     *@param  sTitle  Description of the Parameter
     */
    public JClassTree(String sTitle) {
        this();
        setTitle(sTitle);
    }


    /**
     *  Make the dialog visible.
     *
     *@param  b  The new visible value
     */
    public void setVisible(boolean b) {
        if (b) {
            setLocation(50, 50);
        }
        super.setVisible(b);
    }



    /**
     *  Adds a feature to the Notify attribute of the JClassTree object
     */
    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension d = getSize();
        Insets in = getInsets();

        super.addNotify();

        if (fComponentsAdjusted) {
            return;
        }
        // Adjust components according to the insets
        setSize(in.left + in.right + d.width, in.top + in.bottom + d.height);
        Component components[] = getContentPane().getComponents();
        for (int i = 0; i < components.length; i++) {
            Point p = components[i].getLocation();
            p.translate(in.left, in.top);
            components[i].setLocation(p);
        }
        fComponentsAdjusted = true;
    }


    /**
     *  The main program for the JClassTree class
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {
        JClassTree Main = new JClassTree();
        Main.setVisible(true);
    }


    /**
     *  Description of the Class
     *
     *@author     ulmerh
     *@created    20. Januar 2003
     */
    class SymWindow extends WindowAdapter {
        /**
         *  Description of the Method
         *
         *@param  event  Description of the Parameter
         */
        public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == JClassTree.this) {
                JClassTree_WindowClosing(event);
            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    void JClassTree_WindowClosing(WindowEvent event) {
        setVisible(false);
        // hide the Frame
    }


    /**
     *  Description of the Class
     *
     *@author     ulmerh
     *@created    20. Januar 2003
     */
    class SymAction implements ActionListener {
        /**
         *  Description of the Method
         *
         *@param  event  Description of the Parameter
         */
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == expandButton) {
                expandButton_actionPerformed(event);
            } else if (object == collapseButton) {
                collapseButton_actionPerformed(event);
            } else if (object == editButton) {
                editButton_actionPerformed(event);
            } else if (object == cancelButton) {
                cancelButton_actionPerformed(event);
            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    void expandButton_actionPerformed(java.awt.event.ActionEvent event) {
        expandTree();
    }


    /**
     *  Expand the tree to show all nodes.
     */
    public void expandTree() {
        int row = 0;
        while (row < treeView_.getRowCount()) {
            if (treeView_.isCollapsed(row)) {
                treeView_.expandRow(row);
            }
            row++;
        }
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    void collapseButton_actionPerformed(java.awt.event.ActionEvent event) {
        treeView_.collapseRow(0);
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    void editButton_actionPerformed(java.awt.event.ActionEvent event) {
        Object[] objs = treeView_.getSelectionPath().getPath();
        showProperties(((DefaultMutableTreeNode) objs[objs.length - 1]).getUserObject());
    }


    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    void cancelButton_actionPerformed(java.awt.event.ActionEvent event) {
        setVisible(false);
    }


    /**
     *  Set the <code>JPane</code> for which the <code>JTree</code> is to be
     *  built.
     *
     *@param  obj  Description of the Parameter
     */
//  public void setPara(JPane Para) {
//    Enumeration layers;
//    Layer ly;
//    LayerChild child;
//    Graph graph;
//    Axis axis;
//    CartesianRenderer rend;
//    LineAttribute attr;
//    GridAttribute gattr;
//    PointAttribute pattr;
//    //
//    DefaultTreeModel treeModel;
//    DefaultMutableTreeNode node;
//    DefaultMutableTreeNode paneNode;
//
//    DefaultMutableTreeNode layerNode;
//    DefaultMutableTreeNode childNode;
//    DefaultMutableTreeNode graphNode;
//    DefaultMutableTreeNode attrNode;
//    DefaultMutableTreeNode gattrNode;
//    DefaultMutableTreeNode pattrNode;
//    DefaultMutableTreeNode axisNode;
//    DefaultMutableTreeNode titleNode;
//
//    m_Para = Para;
//    paneNode = new DefaultMutableTreeNode(m_Para);
//    treeModel = new DefaultTreeModel(paneNode);
//    treeView_.setModel(treeModel);
//    //
//    Component[] comps = pane_.getComponents();
//    for(int il=0; il < comps.length; il++) {
//      if(comps[il] instanceof Layer) {
//        ly = (Layer)comps[il];
//      } else {
//        continue;
//      }
//      String name, className;
//      layerNode = new DefaultMutableTreeNode(ly);
//      treeModel.insertNodeInto(layerNode, paneNode, treeModel.getChildCount(paneNode));
//      for(Enumeration childs = ly.childElements(); childs.hasMoreElements();) {
//        child = (LayerChild)childs.nextElement();
//        className = child.getClass().getName();
//        name = className.substring(className.lastIndexOf(".")+1);
//        childNode = new DefaultMutableTreeNode(child);
//        treeModel.insertNodeInto(childNode,layerNode,treeModel.getChildCount(layerNode));
//      }
//      graph = ly.getGraph();
//      className = graph.getClass().getName();
//      name = className.substring(className.lastIndexOf(".")+1);
//      if(graph instanceof CartesianGraph) {
//        graphNode = new DefaultMutableTreeNode(graph);
//        treeModel.insertNodeInto(graphNode,layerNode,treeModel.getChildCount(layerNode));
//        rend = ((CartesianGraph)graph).getRenderer();
//        if(rend instanceof LineCartesianRenderer) {
//          attr = (LineAttribute)((LineCartesianRenderer)rend).getAttribute();
//          if(attr != null) {
//            className = attr.getClass().getName();
//            name = className.substring(className.lastIndexOf(".")+1);
//            attrNode = new DefaultMutableTreeNode(attr);
//            treeModel.insertNodeInto(attrNode,graphNode,treeModel.getChildCount(graphNode));
//          }
//        } else if(rend instanceof GridCartesianRenderer) {
//          gattr = (GridAttribute)((GridCartesianRenderer)rend).getAttribute();
//          if(gattr != null) {
//            className = gattr.getClass().getName();
//            name = className.substring(className.lastIndexOf(".")+1);
//            gattrNode = new DefaultMutableTreeNode(gattr);
//            treeModel.insertNodeInto(gattrNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//          }
//        } else if(rend instanceof PointCartesianRenderer) {
//          pattr = (PointAttribute)((PointCartesianRenderer)rend).getAttribute();
//          if(pattr != null) {
//            className = pattr.getClass().getName();
//            name = className.substring(className.lastIndexOf(".")+1);
//            pattrNode = new DefaultMutableTreeNode(pattr);
//            treeModel.insertNodeInto(pattrNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//          }
//        }
//        for(Enumeration axes = ((CartesianGraph)graph).xAxisElements();
//            axes.hasMoreElements();) {
//          axis = (Axis)axes.nextElement();
//          className = axis.getClass().getName();
//          name = className.substring(className.lastIndexOf(".")+1);
//          if(axis instanceof SpaceAxis) {
//            axisNode = new DefaultMutableTreeNode(axis);
//            treeModel.insertNodeInto(axisNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//            SGLabel title = axis.getTitle();
//            if(title != null) {
//              titleNode = new DefaultMutableTreeNode(title);
//              treeModel.insertNodeInto(titleNode,
//                                       axisNode,
//                                       treeModel.getChildCount(axisNode));
//            }
//          } else { // not a SpaceAxis
//            axisNode = new DefaultMutableTreeNode(axis);
//            treeModel.insertNodeInto(axisNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//          }
//        }
//        for(Enumeration axes = ((CartesianGraph)graph).yAxisElements();
//            axes.hasMoreElements();) {
//          axis = (Axis)axes.nextElement();
//          className = axis.getClass().getName();
//          name = className.substring(className.lastIndexOf(".")+1);
//          if(axis instanceof SpaceAxis) {
//            axisNode = new DefaultMutableTreeNode(axis);
//            treeModel.insertNodeInto(axisNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//            SGLabel title = axis.getTitle();
//            if(title != null) {
//              titleNode = new DefaultMutableTreeNode(title);
//              treeModel.insertNodeInto(titleNode,
//                                       axisNode,
//                                       treeModel.getChildCount(axisNode));
//            }
//          } else { // not a SpaceAxis
//            axisNode = new DefaultMutableTreeNode(axis);
//            treeModel.insertNodeInto(axisNode,
//                                     graphNode,
//                                     treeModel.getChildCount(graphNode));
//          }
//        }
//      } else {  // not a CartesianGraph
//        graphNode = new DefaultMutableTreeNode(graph);
//        treeModel.insertNodeInto(graphNode,
//                                 layerNode,
//                                 treeModel.getChildCount(layerNode));
//      }
//    } // for layers
//    int row=0;
//    while(row < treeView_.getRowCount()) {
//      if(treeView_.isCollapsed(row)) {
//        treeView_.expandRow(row);
//      }
//      row++;
//    }
//  }

    void showProperties(Object obj) {
        System.out.println("showProperties obj=" + obj.getClass());
//    if(obj instanceof SGLabel) {
//      if(sg_ == (SGLabelDialog) null)
//	sg_ = new SGLabelDialog();
//
//      sg_.setSGLabel((SGLabel) obj, pane_);
//      if(!sg_.isShowing())
//        sg_.setVisible(true);
//    }
//    else
//      if(obj instanceof Logo) {
//        if(lg_ == null) {
//        lg_ = new LogoDialog();
//      }
//      lg_.setLogo((Logo) obj, pane_);
//      if(!lg_.isShowing())
//        lg_.setVisible(true);
//    }
    }


    /**
     *  Gets the frame attribute of the JClassTree object
     *
     *@return    The frame value
     */
    private Frame getFrame() {
        Container theFrame = this;
        do {
            theFrame = theFrame.getParent();
        } while ((theFrame != null) && !(theFrame instanceof Frame));
        if (theFrame == null) {
            theFrame = new Frame();
        }
        return (Frame) theFrame;
    }
}
