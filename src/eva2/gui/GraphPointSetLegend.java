package eva2.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import eva2.tools.Pair;
import eva2.tools.chart2d.SlimRect;

/**
 * A class representing the legend of a plot. It is created from a list of
 * GraphPointSets as used in FunctionArea. Painting is done in FunctionArea. As
 * an alternative, an own frame could be created.
 * 
 * @author mkron
 * 
 */
public class GraphPointSetLegend {
	SortedSet<Pair<String, Color>> legendEntries;

	/**
	 * 
	 * @author draeger
	 *
	 */
	private static class PairComp implements Comparator<Pair<String, Color>> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Pair<String, Color> o1, Pair<String, Color> o2) {
			return o1.car().compareTo(o2.car());
		}
		
	}
	
	private static final PairComp comperator = new PairComp(); 

	/**
	 * 
	 * @param pointSetContainer
	 */
	public GraphPointSetLegend(List<GraphPointSet> pointSetContainer) {
		legendEntries = new TreeSet<Pair<String, Color>>(comperator);
		for (int i = 0; i < pointSetContainer.size(); i++) {
			GraphPointSet pointset = pointSetContainer.get(i);
			legendEntries.add(new Pair<String, Color>(pointset.getInfoString(),
					pointset.getColor()));
		}
	}

	/**
	 * Add the legend labels to a container.
	 * 
	 * @param comp
	 */
	public void addToContainer(JComponent comp) {
		for (Pair<String, Color> legendEntry : legendEntries) {
			JLabel label = new JLabel(legendEntry.head);
			label.setForeground(legendEntry.tail);
			comp.add(label);
		}
	}

	/**
	 * 
	 * @param bgCol
	 * @param pointSetContainer
	 * @return
	 */
	public static JPanel makeLegendPanel(Color bgCol,
			ArrayList<GraphPointSet> pointSetContainer) {
		JPanel pan = new JPanel();
		pan.setBackground(bgCol);
		pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
		GraphPointSetLegend lBox = new GraphPointSetLegend(pointSetContainer);
		lBox.addToContainer(pan);
		return pan;
	}

	/**
	 * 
	 * @param bgCol
	 * @param pointSetContainer
	 * @return
	 */
	public static JFrame makeLegendFrame(Color bgCol,
			ArrayList<GraphPointSet> pointSetContainer) {
		JFrame frame = new JFrame("Legend");
		// LegendBox lBox = new LegendBox(bgCol, pointSetContainer);
		frame.add(makeLegendPanel(bgCol, pointSetContainer));
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	/**
	 * 
	 * @param component
	 */
	public void paintIn(JComponent component) {
		Graphics g = component.getGraphics();
		FontMetrics fm = g.getFontMetrics();
		int yOffs = 5 + fm.getHeight();
		int xOffs = 0;
		Color origCol = g.getColor();

		for (Pair<String, Color> legendEntry : legendEntries) {
			g.setColor(legendEntry.tail);
			Rectangle2D rect = fm.getStringBounds(legendEntry.head, g);
			xOffs = (int) (component.getWidth() - rect.getWidth() - 5);
			g.drawString(legendEntry.head, xOffs, yOffs);
			yOffs += (5 + rect.getHeight());
		}
		g.setColor(origCol);
	}

	// public void paintIn(Graphics g, Dimension dim) {
	// paintIn(g, dim.width);
	// }
	//	
	// public void paintIn(Graphics g, Rectangle rect) {
	// paintIn(g, rect.width);
	// }
	//	
	// public void paintIn(Graphics g, DRectangle rect) {
	// paintIn(g, (int)rect.width);
	// }

	/**
	 * 
	 */
	public void paintIn(Graphics g, SlimRect rect) {
		paintIn(g, (int) rect.getX(), (int) rect.getY(), (int) rect.getX()
				+ (int) rect.getWidth());
	}

	/**
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param maxX
	 */
	private void paintIn(Graphics g, int x, int y, int maxX) {
		FontMetrics fm = g.getFontMetrics();
		// System.out.println("In LegendBox.paintIn!");
		int yOffs = 5 + y + fm.getHeight();
		int xOffs = x;
		Color origCol = g.getColor();
		// avoid that an entry with identical label and color occurs multiple
		// times.
		for (Pair<String, Color> legendEntry : legendEntries) {
			// System.out.println(legendEntries[i].toString() + "\tcontaines: "
			// + set.contains(legendEntries[i]));
			g.setColor(legendEntry.tail);
			Rectangle2D stringBounds = fm.getStringBounds(
					legendEntry.head, g);
			xOffs = (int) (maxX - stringBounds.getWidth() - 5);
			g.drawString(legendEntry.head, xOffs, yOffs);
			// g.drawString(legendEntries[i].head, 80, 80);
			yOffs += (5 + stringBounds.getHeight());
		}
		g.setColor(origCol);
	}
}
