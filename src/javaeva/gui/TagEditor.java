package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 14 $
 *            $Date: 2006-12-18 16:32:23 +0100 (Mon, 18 Dec 2006) $
 *            $Author: marcekro $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javaeva.tools.SelectedTag;
import javaeva.tools.Tag;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Toolkit;
import java.beans.*;
import javax.swing.*;
import wsi.ra.tool.BasicResourceLoader;
import javaeva.client.EvAClient;
/*==========================================================================*
* CLASS DECLARATION
*==========================================================================*/
/**
 *
 */
public class TagEditor extends PropertyEditorSupport {
  /**
   * Returns a description of the property value as java source.
   *
   * @return a value of type 'String'
   */
  public String getJavaInitializationString() {

    SelectedTag s = (SelectedTag)getValue();
    Tag [] tags = s.getTags();
    String result = "new SelectedTag("
      + s.getSelectedTag().getID()
      + ", {\n";
    for (int i = 0; i < tags.length; i++) {
      result += "new Tag(" + tags[i].getID()
	+ ",\"" + tags[i].getString()
	+ "\")";
      if (i < tags.length - 1) {
	result += ',';
      }
      result += '\n';
    }
    return result + "})";
  }

  /**
   * Gets the current value as text.
   *
   * @return a value of type 'String'
   */
  public String getAsText() {
    SelectedTag s = (SelectedTag)getValue();
    return s.getSelectedTag().getString();
  }

  /**
   * Sets the current property value as text.
   *
   * @param text the text of the selected tag.
   * @exception java.lang.IllegalArgumentException if an error occurs
   */
  public void setAsText(String text) throws java.lang.IllegalArgumentException {
    SelectedTag s = (SelectedTag)getValue();
    Tag [] tags = s.getTags();
    try {
      for (int i = 0; i < tags.length; i++) {
	if (text.equals(tags[i].getString())) {
	  setValue(new SelectedTag(tags[i].getID(), tags));
	  return;
	}
      }
    } catch (Exception ex) {
      throw new java.lang.IllegalArgumentException(text);
    }
  }

  /**
   * Gets the list of tags that can be selected from.
   *
   * @return an array of string tags.
   */
  public String[] getTags() {

    SelectedTag s = (SelectedTag)getValue();
    Tag [] tags = s.getTags();
    String [] result = new String [tags.length];
    for (int i = 0; i < tags.length; i++) {
      result[i] = tags[i].getString
      ();
    }
    return result;
  }

  /**
   * Tests out the selectedtag editor from the command line.
   *
   * @param args ignored
   */
  public static void main(String [] args) {
    try {
      PropertyEditorManager.registerEditor(SelectedTag.class,TagEditor.class);
      Tag [] tags =  {
	new Tag(1, "First option"),
	new Tag(2, "Second option"),
	new Tag(3, "Third option"),
	new Tag(4, "Fourth option"),
	new Tag(5, "Fifth option"),
      };
      SelectedTag initial = new SelectedTag(1, tags);
      TagEditor ce = new TagEditor();
      ce.setValue(initial);
      PropertyValueSelector ps = new PropertyValueSelector(ce);
      JFrame f = new JFrame();
      BasicResourceLoader  loader  = BasicResourceLoader.instance();
      byte[] bytes   = loader.getBytesFromResourceLocation(EvAClient.iconLocation);
      try {
          f.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
      } catch (java.lang.NullPointerException e) {
        System.out.println("Could not find JavaEvA icon, please move rescoure folder to working directory!");
      }
      f.addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
	  System.exit(0);
	}
      });
      f.getContentPane().setLayout(new BorderLayout());
      f.getContentPane().add(ps, BorderLayout.CENTER);
      f.pack();
      f.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}

