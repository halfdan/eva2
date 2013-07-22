package eva2.gui;

/*
 * Title: EvA2
 * Description: The main client class of the EvA framework.
 * Copyright: Copyright (c) 2008
 * Company: University of Tuebingen, Computer
 * Architecture 
 * 
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version: $Revision: 322 $ $Date: 2007-12-11 17:24:07 +0100 (Tue, 11 Dec 2007)$
 * $Author: mkron $  
 */

import eva2.tools.BasicResourceLoader;

import java.awt.*;
import javax.swing.*;


class SplashScreen extends Frame {

    private static final long serialVersionUID = 1281793825850423095L;
    private String imgLocation;

    public SplashScreen(String imgLoc) {
        imgLocation = imgLoc;
    }

    /**
     * Show the splash screen to the end user.
     * <p/>
     * <P>Once this method returns, the splash screen is realized, which means
     * that almost all work on the splash screen should proceed through the
     * event dispatch thread. In particular, any call to
     * <code>dispose</code> for the splash screen must be performed in the event
     * dispatch thread.
     */
    public void splash() {
        JWindow splashWindow = new JWindow(this);
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(imgLocation, true);
        ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
        JLabel splashLabel = new JLabel(ii);

        splashWindow.add(splashLabel);
        splashWindow.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        splashWindow.setLocation(screenSize.width / 2 - splashWindow.getSize().width / 2, screenSize.height / 2 - splashWindow.getSize().height / 2);
        splashWindow.setVisible(true);
    }
}