package vx.demo.web;

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Specifies that the verticle has no http endpoints, but healthcheck
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( [ ElementType.TYPE ] )
@interface HealthCheckOnly {
  /**
   * health check port number
   */
  int value()
}
