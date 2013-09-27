package eva2.optimization.operator.constraint;

import eva2.gui.editor.GenericObjectEditor;

/**
 * This constraint is always satisfied.
 *
 * @author mkron
 */
public class DummyConstraint extends AbstractConstraint {

    @Override
    public Object clone() {
        return new DummyConstraint();
    }

    @Override
    public void hideHideable() {
        GenericObjectEditor.setHideAllProperties(this.getClass(), true);
    }

    @Override
    protected double getRawViolationValue(double[] indyX) {
        return 0;
    }

    public static String globalInfo() {
        return "This constraint is always fulfilled.";
    }
}