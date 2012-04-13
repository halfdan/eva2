package eva2.gui;
/*
 * Title:        EvA2
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 199 $
 *            $Date: 2007-10-23 16:58:12 +0200 (Tue, 23 Oct 2007) $
 *            $Author: mkron $
 */
import eva2.EvAInfo;
import eva2.tools.BasicResourceLoader;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
/**
 *
 */
public class HtmlDemo {
  private JEditorPane m_html;
  private String m_name;
  /**
   *
   */
  public HtmlDemo(String name) {
    m_name = name;
  }
  /**
   *
   */
  public JEditorPane getPane() {
    return m_html;
  }
    /**
    *
    */
    public static void main(String[] args) {
        HtmlDemo demo = new HtmlDemo("ES.html");
        demo.show();
    }
    
    public boolean resourceExists() {
    	URL url = ClassLoader.getSystemResource("resources/"+m_name);
    	return (url != null);
    }
    
    public static boolean resourceExists(String mname) {
    	URL url = ClassLoader.getSystemResource("resources/"+mname);
    	return (url != null);
    }
    /**
    *
    */
    public void show() {
        try {
            URL url = null;
            url = this.getClass().getClassLoader().getSystemResource("resources/"+m_name);

            try {
                m_html = new JEditorPane(url);
            } catch (java.io.IOException ioe) {
                url = this.getClass().getClassLoader().getSystemResource("resources/Default.html");
                m_html = new JEditorPane(url);
            }
            //m_html = new JEditorPane(htmlDescription);
            m_html.setEditable(false);
            m_html.addHyperlinkListener(createHyperLinkListener());

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e);
            return;
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            e.printStackTrace();
            return;
        }
        JFrame          frame       = new JFrame (m_name);
        BasicResourceLoader  loader      = BasicResourceLoader.instance();
        byte[]          bytes       = loader.getBytesFromResourceLocation(EvAInfo.iconLocation, true);
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(bytes));
        JScrollPane     scroller    = new JScrollPane();
        JViewport       vp          = scroller.getViewport();
        vp.add(m_html);
        scroller.setPreferredSize( new Dimension(600,500) );
        frame.getContentPane().add(scroller, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

  /**
   *
   */
  public HyperlinkListener createHyperLinkListener() {
    return new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if (e instanceof HTMLFrameHyperlinkEvent) {
              ((HTMLDocument)m_html.getDocument()).processHTMLFrameHyperlinkEvent(
                  (HTMLFrameHyperlinkEvent)e);
          } else {
              try {
                  m_html.setPage(e.getURL());
              } catch (IOException ioe) {
                  System.out.println("IOE: " + ioe);
              }
          }
        }
      }
  };
}
}
