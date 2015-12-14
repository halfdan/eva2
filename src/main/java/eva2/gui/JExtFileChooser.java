package eva2.gui;

import javax.swing.*;
import java.io.File;

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
