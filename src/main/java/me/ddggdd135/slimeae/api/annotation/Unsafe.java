package me.ddggdd135.slimeae.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.TypeQualifierValidator;
import javax.annotation.meta.When;

@TypeQualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Unsafe {
    When when() default When.ALWAYS;

    class Checker implements TypeQualifierValidator<Unsafe> {

        @Nonnull
        public When forConstantValue(@Nonnull Unsafe qualifierArgument, Object value) {
            if (value == null) return When.NEVER;
            return When.ALWAYS;
        }
    }
}
