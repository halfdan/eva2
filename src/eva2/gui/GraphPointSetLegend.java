package eva2.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import eva2.tools.Pair;
import eva2.tools.chart2d.SlimRect;

/**
 * A class representing the legend of a plot. It is created from a list of GraphPointSets as
 * used in FunctionArea. Painting is done in FunctionArea. As an alternative, an own frame
 * could be created.
 * 
 * @author mkron
 *
 */
public class GraphPointSetLegend  {
	Pair<String,Color>[] legendEntries;
	
	public GraphPointSetLegend(List<GraphPointSet> pointSetContainer) {
		legendEntries = new Pair[pointSetContainer.size()];
		for (int i = 0; i < pointSetContainer.size(); i++) {
			GraphPointSet pointset = pointSetContainer.get(i);
			legendEntries[i] = new Pair<String,Color>(pointset.getInfoString(), pointset.getColor());
		}		
	}

	/**
	 * Add the legend labels to a container.
	 * @param comp
	 */
	public void addToContainer(JComponent comp) {
		for (int i = 0; i < legendEntries.length; i++) {
			JLabel label = new JLabel(legendEntries[i].head);
			label.setForeground(legendEntries[i].tail);
			comp.add(label);
		}
	}
	
	public static JPanel makeLegendPanel(Color bgCol, ArrayList<GraphPointSet> pointSetContainer) {
		JPanel pan = new JPanel();
		pan.setBackground(bgCol);
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		GraphPointSetLegend lBox=new GraphPointSetLegend(pointSetContainer);
		lBox.addToContainer(pan);
		return pan;
	}

	public static JFrame makeLegendFrame(Color bgCol, ArrayList<GraphPointSet> pointSetContainer) {
		JFrame frame = new JFrame("Legend");
//		LegendBox lBox = new LegendBox(bgCol, pointSetContainer);
		frame.add(makeLegendPanel(bgCol, pointSetContainer));
		frame.pack();
		frame.setVisible(true);	
		return frame;
	}

	public void paintIn(JComponent component) {
		Graphics g = component.getGraphics();
		FontMetrics fm = g.getFontMetrics();
		int yOffs=5+fm.getHeight();
		int xOffs=0;
		Color origCol = g.getColor();
		
		for (int i=0; i<legendEntries.length; i++) {
			g.setColor(legendEntries[i].tail);
			Rectangle2D rect = fm.getStringBounds(legendEntries[i].head, g);
			xOffs = (int) (component.getWidth()-rect.getWidth()-5);
			g.drawString(legendEntries[i].head, xOffs, yOffs);
			yOffs+=(5+rect.getHeight());
		}
		g.setColor(origCol);
	}
	
//	public void paintIn(Graphics g, Dimension dim) {
//		paintIn(g, dim.width);
//	}
//	
//	public void paintIn(Graphics g, Rectangle rect) {
//		paintIn(g, rect.width);
//	}
//	
//	public void paintIn(Graphics g, DRectangle rect) {
//		paintIn(g, (int)rect.width);
//	}
	
	public void paintIn(Graphics g, SlimRect rect) {
		paintIn(g, (int)rect.getX(), (int)rect.getY(), (int)rect.getX()+(int)rect.getWidth());
	}
	
	private void paintIn(Graphics g, int x, int y, int maxX) {
		FontMetrics fm = g.getFontMetrics();
//		System.out.println("In LegendBox.paintIn!");
		int yOffs=5+y+fm.getHeight();
		int xOffs=x;
		Color origCol = g.getColor();
		
		for (int i=0; i<legendEntries.length; i++) {
			g.setColor(legendEntries[i].tail);
			Rectangle2D stringBounds = fm.getStringBounds(legendEntries[i].head, g);
			xOffs = (int) (maxX-stringBounds.getWidth()-5);
			g.drawString(legendEntries[i].head, xOffs, yOffs);
//			g.drawString(legendEntries[i].head, 80, 80);
			yOffs+=(5+stringBounds.getHeight());
		}
		g.setColor(origCol);
	}
}
