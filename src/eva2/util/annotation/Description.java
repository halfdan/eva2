package eva2.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used in OptimizationEditorPanel to display Tooltips
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    String text();
}
