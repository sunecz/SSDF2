package sune.util.ssdf2;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target   (PARAMETER)
public @interface SSDNamedArg {
	
    String value();
}