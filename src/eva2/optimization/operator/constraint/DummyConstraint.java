package eva2.optimization.operator.constraint;

import eva2.gui.editor.GenericObjectEditor;
import eva2.util.annotation.Description;

/**
 * This constraint is always satisfied.
 */
@Description("This constraint is always fulfilled.")
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
}
