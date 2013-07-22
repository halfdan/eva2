package eva2.gui;
/*
 * Title:        EvA2
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

import java.io.File;
import javax.swing.*;

public class JExtFileChooser extends JFileChooser {
    private boolean overwriteWarning = true;

    public void setOverwriteWarning(boolean value) {
        overwriteWarning = value;
    }

    public boolean getOverwriteWarning() {
        return overwriteWarning;
    }

    @Override
    public void approveSelection() {
        if (getDialogType() == JFileChooser.SAVE_DIALOG && overwriteWarning) {
            File f = getSelectedFile();

            if (f != null && f.exists()) {
                if (JOptionPane.showConfirmDialog(this, "Die Datei " + f.getPath() + " existiert bereits.\nSoll sie �berschrieben werden?", "Achtung", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }

        super.approveSelection();
    }
}
