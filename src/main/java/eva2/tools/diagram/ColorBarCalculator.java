/**
 *  Filename: $RCSfile: ColorBarCalculator.java,v $
 *  Purpose:  Calculates the color values for a legend-style color bar.
 *  Language: Java
 *  Compiler: JDK 1.2
 *  Authors:  Badreddin Abolmaali, Fred Rapp, Simon Wiest
 *  Version:  $Revision: 1.1.1.1 $
 *            $Date: 2003/07/03 14:59:41 $
 *            $Author: ulmerh $
 *  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
 */

package eva2.tools.diagram;

import java.awt.*;

/**
 * Calculates the color values for a legend-style color bar.
 */
public class ColorBarCalculator {
    static public final int BLUE_TO_RED = 0;
    static public final int GREY_SCALE = 1;
    static public final int BLUE_SCALE = 2;
    static public final int ALL_COLORS = 3;
    // GREY_SCALE returns luminance values in [0.1;0.9], GREY_EXTENDED_SCALE in [0.0;1.0]
    static public final int GREY_EXTENDED_SCALE = 4;

    private int color_scale = BLUE_TO_RED;
    private boolean inverseScale = false;

    public ColorBarCalculator(int color_scale) {
        this.color_scale = color_scale;
    }

    public void setColorScale(int color_scale) {
        this.color_scale = color_scale;
    }

    /**
     * Returns color for the given float-value, which must be in the range from 0 to 1.
     * Warning: Creates new color object, better use the method 'getRGB' if possible.
     *
     * @param value Value to be converted into RGB color.
     */
    public Color getColor(float value) {
        return new Color(getRGB(value));
    }

    /**
     * Returns color RGB-value for the given float-value, which must be in the range from 0 to 1.
     *
     * @param value Value to be converted into RGB color.
     */
    public int getRGB(float value) {
        int rgbValue = 0;
        if (inverseScale) {
            value = 1 - value;
        }
        switch (color_scale) {
            case BLUE_TO_RED:
                float hue = value * (value + value * 0.8F) / 2.65F - 1F;
                rgbValue = Color.HSBtoRGB(hue, 0.6F, 1F);
                break;

            case GREY_SCALE:
                rgbValue = Color.HSBtoRGB(0F, 0F, (value * 0.8F) + 0.1F);
                break;

            case BLUE_SCALE:
                int rg = (int) (value * 0.95F * 256);
                int b = (int) ((value / 2.0F + 0.45F) * 256);
                rgbValue = rg * 0x10000 + rg * 0x100 + b;
                break;

            case ALL_COLORS:
                rgbValue = Color.HSBtoRGB(value, 0.6F, 1F);
                break;

            case GREY_EXTENDED_SCALE:
                rgbValue = Color.HSBtoRGB(0F, 0F, value);
                break;
        }
        return rgbValue;
    }

    /**
     * Reverts color scale (e.g. black will be white and vice versa).
     *
     * @param isInverse Color scale is inverted, if {@code isInverse} is set to true.
     */
    public void setInverseScale(boolean isInverse) {
        inverseScale = isInverse;
    }

    /**
     * Returns current scale mode.
     *
     * @return {@code true} if scale is inverted, else {@code false}.
     */
    public boolean isInverseScale() {
        return inverseScale;
    }

    /**
     * Returns color for the given float-value, which must be in the range from 0 to 1.
     * Warning: Creates new color object, better use the method 'getRGB' if possible.
     */
    static public Color getDefaultColor(float value) {
        return new Color(getDefaultRGB(value));
    }

    /**
     * Returns color RGB-value for the given float-value, which must be in the range from 0 to 1.
     */
    static public int getDefaultRGB(float value) {
        float hue = value * (value + value * 0.8F) / 2.65F - 1F;
        return Color.HSBtoRGB(hue, 0.6F, 1F);
    }
}