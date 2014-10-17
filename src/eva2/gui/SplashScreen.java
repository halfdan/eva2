package eva2.gui;


import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import java.awt.*;


class SplashScreen extends JWindow {

    private static final long serialVersionUID = 1281793825850423095L;
    private String imgLocation;

    public SplashScreen(String imgLoc) {
        imgLocation = imgLoc;
        BasicResourceLoader loader = BasicResourceLoader.instance();
        byte[] bytes = loader.getBytesFromResourceLocation(imgLocation, true);
        ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().createImage(bytes));
        JLabel splashLabel = new JLabel(ii);

        this.add(splashLabel);
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width / 2 - this.getSize().width / 2, screenSize.height / 2 - this.getSize().height / 2);
        setAlwaysOnTop(true);
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

        this.setVisible(true);
    }
}