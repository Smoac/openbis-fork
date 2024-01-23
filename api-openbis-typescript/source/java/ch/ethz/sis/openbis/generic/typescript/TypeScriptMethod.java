package ch.ethz.sis.openbis.generic.typescript;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TypeScriptMethod
{
    public boolean ignore() default false;

    public boolean async() default true;

    public boolean sessionToken() default true;
}
