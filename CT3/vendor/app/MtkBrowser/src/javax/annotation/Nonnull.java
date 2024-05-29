package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.When;
@TypeQualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
/* loaded from: b.zip:javax/annotation/Nonnull.class */
public @interface Nonnull {
    When when() default When.ALWAYS;
}
