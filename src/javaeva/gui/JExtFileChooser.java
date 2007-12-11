package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import javax.swing.*;
import java.io.File;

public class JExtFileChooser extends JFileChooser{
  private boolean overwriteWarning = true;

  public void setOverwriteWarning(boolean value){
    overwriteWarning = value;
  }

  public boolean getOverwriteWarning(){
    return overwriteWarning;
  }

  public void approveSelection(){
    if(getDialogType() == JFileChooser.SAVE_DIALOG && overwriteWarning){
      File f = getSelectedFile();

      if(f != null && f.exists())
       if(JOptionPane.showConfirmDialog(this, "Die Datei " + f.getPath() + " existiert bereits.\nSoll sie überschrieben werden?", "Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) return;
    }

    super.approveSelection();
  }
}
