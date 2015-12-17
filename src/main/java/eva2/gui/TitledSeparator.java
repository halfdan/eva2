package eva2.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by halfdan on 17/12/15.
 */
public final class TitledSeparator extends JPanel {
    public TitledSeparator(String title) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbConstraints = new GridBagConstraints();
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;

        add(new JLabel("<html><b>" + title), gbConstraints);

        gbConstraints.gridx = 1;
        gbConstraints.gridy = 0;
        gbConstraints.weightx = 1.0;
        gbConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(new JSeparator(JSeparator.HORIZONTAL), gbConstraints);
    }
}
