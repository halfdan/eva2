package eva2.tools.chart2d;

import eva2.tools.math.Mathematics;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * ScaledBorder puts a border around Components
 * ( especially around DrawingAreas ) with scaled and labeled axes.
 */
public class ScaledBorder implements Border {
    /**
     * length of the distance markers on the axes in pixels
     */
    int markerLength = 2;

    /**
     * length in pixels of the arrows at the ends of the axes
     */
    int arrowLength = 10;

    /**
     * a flag if the arrows should be visible
     */
    public boolean showArrows = true;

    /**
     * distance between the x-values in digits
     */
    int xValue2value = 2;

    /**
     * distance between y-label and y-values in digit width
     */
    int yLabel2values = 1;

    /**
     * distance between y-values and y-axis markers in parts of the digit width
     */
    int yValues2marker = 2;

    /**
     * distance between values and arrows in pixels
     */
    int xValues2arrow = 10,
            yValues2arrow = 10;

    /**
     * distance between arrows and outer border
     */
    int axis2border = 4;

    /**
     * distance between labels and the border in pixels
     */
    public int xLabel2border = 6,
            yLabel2border = 6;

    /**
     * the size of the source rectangle
     * that means before the values are mdified by scale functions
     */
    SlimRect srcRect = null;

    /**
     * the minimal increment of the scales
     */
    public double minimalIncrement;

    /**
     * the  displayed labels
     */
    public String xLabel, yLabel;

    /**
     * foreground and background colors
     */
    public Color foreground, background;

    /**
     * the border which is shown around the scaled border
     */
    Border outerBorder;

    /**
     * flag if the outer border should be displayed
     */
    boolean showOuterBorder = true;

    /**
     * scale functions if, for example, an logarithmic function is needed instead
     * of a linear.
     */
    public DFunction xScale, yScale;

    /**
     * Formatters of the x- and y-axis numbers
     *
     * @see java.text.NumberFormat
     */
    private NumberFormat formatX = new DecimalFormat(),
            formatY = new DecimalFormat();

    /**
     * Possible patterns for the number formats used by a border.
     */
    private String[] decPatterns = {"#,##0.###", "00.###E0"}; // standard decimal or scientific exponential

    /**
     * Internal states of which decPatterns to switch to next.
     */
    private int nextYPattern = 1;
    private int nextXPattern = 1;

    private double srcDX = -1, srcDY = -1;

    private boolean doRefresh,
            autoScaleX = true,
            autoScaleY = true;

    private Insets oldInsets;

    private DMeasures m;

    /**
     * constructor creates a default ScaledBorder inside of a lowered BevelBorder
     */
    public ScaledBorder() {
        this(
                BorderFactory.createBevelBorder(
                        BevelBorder.LOWERED,
                        Color.white,
                        Color.lightGray,
                        Color.black,
                        Color.lightGray
                )
        );
    }

    /**
     * constructor creates a new <code>ScaledBorder</code>
     * surrounded by the specified <code>Border</code>
     */
    public ScaledBorder(Border outer) {
        outerBorder = outer;
        m = new DMeasures(this);
    }

    /**
     * method tells the border to calculate the differences between displayed
     * x-values by itself
     */
    public void setAutoScaleX() {
        autoScaleX = true;
    }

    /**
     * method tells the border to calculate the differences between displayed
     * y-values by itself
     */
    public void setAutoScaleY() {
        autoScaleY = true;
    }

    /**
     * method sets the differences between source x-values of the displayed values
     *
     * If scale functions are used there might be a difference between the shown values
     * and the source values
     */
    public void setSrcdX(double dX) {
        autoScaleX = false;
        srcDX = dX;
    }

    /**
     * method sets the differences between source y-values of the displayed values
     *
     * If scale functions are used there might be a difference between the shown values
     * and the source values
     */
    public void setSrcdY(double dY) {
        autoScaleY = false;
        srcDY = dY;
    }

    /**
     * Returns the inner rectangle in pixel coordinates.
     *
     * @param c
     * @return
     */
    public SlimRect getInnerRect(Component c) {
        Insets inner_insets = getBorderInsets(c);
        Dimension d = c.getSize();
        int width = d.width - inner_insets.left - inner_insets.right;
        int height = d.height - inner_insets.top - inner_insets.bottom;

        SlimRect rect = new SlimRect(inner_insets.left, inner_insets.top, width, height);
        return rect;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        //  Here one might know how much of the graph is taken by the border only and possibly switch to exponential numbering?
        if (foreground == null) {
            foreground = c.getForeground();
        }
        if (background == null) {
            background = c.getBackground();
        }
        Color oldColor = g.getColor();
        g.setColor(background);
        g.fillRect(x, y, width, height);
        g.setColor(oldColor);

        Insets outerInsets = new Insets(0, 0, 0, 0);// insets of the outer border
        if (showOuterBorder) {
            outerBorder.paintBorder(c, g, x, y, width, height);
            outerInsets = outerBorder.getBorderInsets(c);
        }

        doRefresh = true;
        Insets innerInsets = getBorderInsets(c);

        Dimension d = c.getSize(),
                cd = new Dimension(d.width - innerInsets.left - innerInsets.right,
                        d.height - innerInsets.top - innerInsets.bottom);

        FontMetrics fm = g.getFontMetrics();
        int fontAsc = fm.getAscent();
        doRefresh = false;

        m.update(c, innerInsets);

        // axes
        g.setColor(foreground);
        g.drawLine(innerInsets.left, innerInsets.top,
                innerInsets.left, innerInsets.top + cd.height);
        g.drawLine(innerInsets.left, innerInsets.top + cd.height,
                innerInsets.left + cd.width, innerInsets.top + cd.height);

        if (showArrows) {
            g.drawLine(innerInsets.left, innerInsets.top,
                    innerInsets.left, innerInsets.top - yValues2arrow);
            g.drawLine(innerInsets.left - markerLength, innerInsets.top - yValues2arrow,
                    innerInsets.left, innerInsets.top - yValues2arrow - arrowLength);
            g.drawLine(innerInsets.left + markerLength, innerInsets.top - yValues2arrow,
                    innerInsets.left, innerInsets.top - yValues2arrow - arrowLength);
            g.drawLine(innerInsets.left - markerLength, innerInsets.top - yValues2arrow,
                    innerInsets.left + markerLength, innerInsets.top - yValues2arrow);

            g.drawLine(innerInsets.left + cd.width, innerInsets.top + cd.height,
                    innerInsets.left + cd.width + xValues2arrow, innerInsets.top + cd.height);
            g.drawLine(innerInsets.left + cd.width + xValues2arrow,
                    innerInsets.top + cd.height - markerLength,
                    innerInsets.left + cd.width + xValues2arrow + arrowLength,
                    innerInsets.top + cd.height);
            g.drawLine(innerInsets.left + cd.width + xValues2arrow,
                    innerInsets.top + cd.height + markerLength,
                    innerInsets.left + cd.width + xValues2arrow + arrowLength,
                    innerInsets.top + cd.height);
            g.drawLine(innerInsets.left + cd.width + xValues2arrow,
                    innerInsets.top + cd.height - markerLength,
                    innerInsets.left + cd.width + xValues2arrow,
                    innerInsets.top + cd.height + markerLength);
        }

        if (yLabel != null) {
            Dimension yld = new Dimension(fm.getAscent() + fm.getDescent(), fm.stringWidth(yLabel));
            AffineTransform T = new AffineTransform(0, 1, 1, 0, 0, 0);
            Font old = g.getFont(), f = old.deriveFont(T);
            g.setFont(f);
            g.drawString(yLabel, yLabel2border + fm.getAscent(), innerInsets.top + (cd.height + yld.height) / 2);
            g.setFont(old);
        }

        if (xLabel != null) {
            g.drawString(
                    xLabel, innerInsets.left + (cd.width - fm.stringWidth(xLabel)) / 2,
                    d.height - outerInsets.bottom - xLabel2border - fm.getDescent());
        }

        if (srcRect.x == 0 && srcRect.y == 0) {
            int v2m = fm.stringWidth("0") / yValues2marker;
            g.drawString("0", innerInsets.left - fm.stringWidth("0") - v2m - markerLength,
                    innerInsets.top + cd.height + fontAsc);
            g.drawLine(innerInsets.left, innerInsets.top + cd.height + fm.getAscent(),
                    innerInsets.left, innerInsets.top + cd.height);
            g.drawLine(innerInsets.left, innerInsets.top + cd.height,
                    innerInsets.left - fm.stringWidth("0") - v2m - markerLength,
                    innerInsets.top + cd.height);
        }

        drawYValues(g, innerInsets);
        drawXValues(g, innerInsets, cd);

        g.setColor(oldColor);
    }

    /**
     * The scaling of the y-axis is defined here.
     *
     * @param g
     * @param insets
     */
    private void drawYValues(Graphics g, Insets insets) {
        FontMetrics fm = g.getFontMetrics();
        int fontAsc = fm.getAscent(), v2m = fm.stringWidth("0") / yValues2marker;

        double startVal = Mathematics.firstMultipleAbove(srcRect.y, srcDY);

        double v, scaledV, minx = srcRect.x;
        if (xScale != null) {
            minx = xScale.getImageOf(minx);
        }
        v = startVal;
        while (v <= srcRect.y + srcRect.height) {
            if (yScale != null) {
                scaledV = yScale.getImageOf(v);
            } else {
                scaledV = v;
            }
            String text = formatY.format(scaledV);
            try {
                scaledV = formatY.parse(text).doubleValue();
            } catch (java.text.ParseException ex) {
            }
            Point p = m.getPoint(minx, scaledV);
            if (p != null) {
                g.drawString(text,
                        insets.left - fm.stringWidth(text) - v2m - markerLength,
                        p.y + fontAsc / 2);
                g.drawLine(insets.left - markerLength, p.y, insets.left, p.y);
            }
            if (v + srcDY <= v) {
                v *= 1.01;
            }
            v += srcDY;
        }
    }

    public double getSrcdY(FontMetrics fm, Dimension cd) {
        return getSrcdY(fm.getHeight(), cd.height);
    }

    public double getSrcdY(int fontMetricsHeight, int componentHeight) {
        if ((!doRefresh && srcDY != -1) || !autoScaleY) {
            return srcDY;
        }
        int max = componentHeight / fontMetricsHeight;
        if (Double.isInfinite(srcRect.height) || Double.isInfinite(srcRect.width)) {
            System.err.println("Error, infinite value in ScaledBorder:getSrcdY !!");
        }
        double minsrc_dY = 2 * srcRect.height / (double) max; // die 2 einfach mal so eingesetzt   <--------------------------
        srcDY = aBitBigger(minsrc_dY);
        if (srcDY < minimalIncrement) {
            srcDY = minimalIncrement;
        }
        return srcDY;
    }

    private void drawXValues(Graphics g, Insets insets, Dimension cd) {
        FontMetrics fm = g.getFontMetrics();
        double mx = cd.width / srcRect.width;
        int n, labelX,
                xnull = insets.left + (int) (-srcRect.x * mx);

        n = (int) (srcRect.x / srcDX);
        if (n * srcDX < srcRect.x || (srcRect.x == 0 && srcRect.y == 0)) {
            n++;
        }

        int fontAsc = fm.getAscent(), xLineY = insets.top + cd.height;
        labelX = xnull + (int) (n * srcDX * mx);
        while (n * srcDX <= srcRect.x + srcRect.width) {
            double v = n * srcDX;
            if (xScale != null) {
                v = xScale.getImageOf(v);
            }
            String text = formatX.format(v);
            try {
                v = formatX.parse(text).doubleValue();
            } catch (java.text.ParseException ex) {
            }
            int strW = fm.stringWidth(text);
            g.drawString(text, labelX - strW / 2, xLineY + fontAsc);
            g.drawLine(labelX, xLineY, labelX, xLineY + markerLength);
            n++;
            labelX = xnull + (int) (n * srcDX * mx);
        }
    }

    public double getSrcdX(FontMetrics fm, Dimension cd) {
        if ((!doRefresh && srcDX != -1) || !autoScaleX) {
            return srcDX;
        }
        int digit_width = fm.stringWidth("0"),
                max = cd.width / (digit_width * (xValue2value + 1));
        srcDX = srcRect.width / (double) max;
        int n, labelX, olsrc_dX;

        boolean ok = false;
        while (!ok) {
            srcDX = aBitBigger(srcDX);

            n = (int) (srcRect.x / srcDX);
            if (n * srcDX < srcRect.x) {
                n++;
            }

            olsrc_dX = 0;

            boolean suits = true, first = true;
            while (suits && n * srcDX <= srcRect.x + srcRect.width) {
                double v = n * srcDX;
                if (xScale != null) {
                    v = xScale.getImageOf(v);
                }
                String text = formatX.format(v);
                int strW = fm.stringWidth(text);
                labelX = (int) (((n * srcDX - srcRect.x) / srcRect.width) * cd.width) - strW / 2;
                if (!first && labelX <= olsrc_dX + digit_width * xValue2value) {
                    suits = false;
                } else {
                    olsrc_dX = labelX + strW;
                    n++;
                }
                first = false;
            }
            ok = suits;
        }
        if (srcDX < minimalIncrement) {
            srcDX = minimalIncrement;
        }
        return srcDX;
    }

    /**
     * method returns to a certain minimal value the next higher value which can be
     * displayed, which looks a bit nicer
     * it returns values like ... 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, ...
     *
     * @param min the double value next to which the displayable value should be found
     * @return the displayable value
     */
    public static double aBitBigger(double min) {
        if (min <= 0 || Double.isInfinite(min) || Double.isNaN(min)) {
            return 1;
        }
        double d = 1;
        if (min < d) {
            while (d * .5 > min) {
                d *= .5;
                if (d * .4 > min) {
                    d *= .4;
                }
                if (d * .5 > min) {
                    d *= .5;
                }
            }
        } else {
            while (d <= min) {
                d *= 2;
                if (d <= min) {
                    d *= 2.5;
                }
                if (d <= min) {
                    d *= 2;
                }
            }
        }
        return d;
    }

    @Override
    public boolean isBorderOpaque() {
        return outerBorder.isBorderOpaque();
    }

    /**
     * Toggle between different decimal patterns on the axes. Basically shifts
     * to the next pattern specified for this border type.
     *
     * @param xOrY if true toggle for the x axis otherwise for the y axis
     */
    public void toggleDecPattern(boolean xOrY) {
        int current;
        if (xOrY) {
            current = nextXPattern;
            nextXPattern = (nextXPattern + 1) % decPatterns.length;
        } else {
            current = nextYPattern;
            nextYPattern = (nextYPattern + 1) % decPatterns.length;
        }
        applyPattern(xOrY, decPatterns[current]);
    }

    /**
     * Switch between different decimal patterns on the axes.
     * The next index gives the index within the pattern list of this border type
     * to switch to.
     *
     * @param xOrY if true toggle for the x axis otherwise for the y axis
     * @param next index of the pattern to switch to
     */
    protected void setNextPattern(boolean xOrY, int next) {
        if (xOrY) {
            nextXPattern = next;
        } else {
            nextYPattern = next;
        }
        toggleDecPattern(xOrY);
    }

    /**
     * Set the standard decimal number pattern for the x or y axis.
     *
     * @param xOrY if true, toggle for the x axis otherwise for the y axis
     */
    public void setStandardPattern(boolean xOrY) {
        setNextPattern(xOrY, 0);
    }

    /**
     * Set the exponential number pattern for the x or y axis.
     *
     * @param xOrY if true, toggle for the x axis otherwise for the y axis
     */
    public void setScientificPattern(boolean xOrY) {
        setNextPattern(xOrY, 1);
    }

    /**
     * Apply a decimal format pattern to x (bXorY true) or y (bXorY false) axis.
     *
     * @param bXorY
     * @param pattern
     */
    public void applyPattern(boolean bXorY, String pattern) {
        if (bXorY) {
            ((java.text.DecimalFormat) formatX).applyPattern(pattern);
        } else {
            ((java.text.DecimalFormat) formatY).applyPattern(pattern);
        }
    }

    /**
     * This measures the space required for numberings on x and y axis and returns
     * it. Depends on the decimal format applied and the FontMetrics of the
     * current Graphics instance.
     */
    @Override
    public Insets getBorderInsets(Component c) {
        if (!doRefresh && oldInsets != null) {
            return oldInsets;
        }

        Graphics g = c.getGraphics();

        Insets insets = new Insets(0, 0, 0, 0);
        if (showOuterBorder) {
            insets = outerBorder.getBorderInsets(c);
        }

        if (g == null) {
            return insets;
        }

        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight(),
                digit_width = fm.stringWidth("0");

        if (c instanceof DArea) {
            DArea area = (DArea) c;
            DMeasures m = area.getDMeasures();
            srcRect = m.getSourceOf(area.getSlimRectangle());
            xScale = area.getDMeasures().xScale;
            yScale = area.getDMeasures().yScale;
        }

        // left:
        if (yLabel != null) {
            insets.left += fm.getAscent() + fm.getDescent();
        }
        insets.left += yLabel2values * digit_width;
        getSrcdY(fm, c.getSize());
        double start, n, inc;
        int maxWidth = 0;
        start = srcDY * (int) (srcRect.y / srcDY);
        n = start;
        if (n < srcRect.y) {
            n += srcDY;
        }

        if (((srcRect.y + srcRect.height) - start) / srcDY > 20) {
            inc = ((srcRect.y + srcRect.height) - start) / 20.;
        } else {
            inc = srcDY;
        }
        if ((n + inc) == n) {
            System.err.println("Warning, too small increase step size!");
        }
        for (; n <= srcRect.y + srcRect.height; n += inc) {
            // TODO here might be a bug for mean values
            double v = n;
            if (yScale != null) {
                v = yScale.getImageOf(v);
            }
            int w = fm.stringWidth(formatY.format(v));
            if (w > maxWidth) {
                maxWidth = w;
            }
            // avoid nearly endless loop for large srcRect.y value and small srcDY
        }

        insets.left += 1 + yLabel2border + maxWidth + digit_width / yValues2marker + markerLength;

        // bottom:
        insets.bottom += 1 + fontHeight + xLabel2border;
        if (xLabel != null) {
            insets.bottom += fontHeight;
        }

        // top:
        if (showArrows) {
            insets.top += yValues2arrow + arrowLength;
        }
        insets.top += axis2border;

        // right:
        if (showArrows) {
            insets.right += xValues2arrow + arrowLength;
        }
        insets.right += axis2border;
        getSrcdX(fm, c.getSize());
        int k = (int) (srcRect.x + srcRect.width / srcDX);
        if (k < 0) {
            k++;
        }
        int w = fm.stringWidth(formatX.format(k * srcDX));
        if (w / 2 > insets.right) {
            insets.right = w / 2;
        }

        oldInsets = insets;
        return insets;
    }
}
