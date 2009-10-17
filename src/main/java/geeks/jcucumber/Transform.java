package geeks.jcucumber;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for methods that indicates it transforms arguments.
 * @see <a href="http://www.engineyard.com/blog/2009/cucumber-step-argument-transforms/">Step Argument Transforms</a>
 * Once Cuke4Duke provides this annotation, this one will be removed.
 * @author pabstec
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transform {
  /**
   * The regular expression to match against for the target method to be used.
   * @return the regular expression string
   */
  public abstract String value();
}