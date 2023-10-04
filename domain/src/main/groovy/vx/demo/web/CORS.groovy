package vx.demo.web;

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * If specified, the {@link CorsHandler} is applied to the main endpoint in dev-mode.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( [ ElementType.TYPE ] )
@interface CORS {
  /**
   * health check port number
   */
  String value()
}
