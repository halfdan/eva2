package eva2.gui;

import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 */
public class HtmlDemo {
    private JEditorPane htmlEditorPane;
    private String name;

    /**
     *
     */
    public HtmlDemo(String name) {
        this.name = name;
    }

    /**
     *
     */
    public JEditorPane getPane() {
        return htmlEditorPane;
    }

    /**
     *
     */
    public static void main(String[] args) {
        HtmlDemo demo = new HtmlDemo("ES.html");
        demo.show();
    }

    public boolean resourceExists() {
        URL url = ClassLoader.getSystemResource("html/" + name);
        return (url != null);
    }

    public static boolean resourceExists(String mname) {
        URL url = ClassLoader.getSystemResource("html/" + mname);
        return (url != null);
    }

    /**
     *
     */
    public void show() {
        try {
            URL url = ClassLoader.getSystemResource("html/" + name);

            try {
                htmlEditorPane = new JEditorPane(url);
            } catch (java.io.IOException ioe) {
                url = ClassLoader.getSystemResource("html/Default.html");
                htmlEditorPane = new JEditorPane(url);
            }
            htmlEditorPane.setEditable(false);
            htmlEditorPane.addHyperlinkListener(createHyperLinkListener());

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e);
            return;
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            e.printStackTrace();
            return;
        }
        JFrame frame = new JFrame(name);
        BasicResourceLoader loader = BasicResourceLoader.getInstance();
        byte[] bytes = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
        JScrollPane scroller = new JScrollPane();
        JViewport vp = scroller.getViewport();
        vp.add(htmlEditorPane);
        scroller.setPreferredSize(new Dimension(600, 500));
        frame.getContentPane().add(scroller, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     *
     */
    public HyperlinkListener createHyperLinkListener() {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (e instanceof HTMLFrameHyperlinkEvent) {
                        ((HTMLDocument) htmlEditorPane.getDocument()).processHTMLFrameHyperlinkEvent(
                                (HTMLFrameHyperlinkEvent) e);
                    } else {
                        try {
                            htmlEditorPane.setPage(e.getURL());
                        } catch (IOException ioe) {
                            System.out.println("IOE: " + ioe);
                        }
                    }
                }
            }
        };
    }
}
