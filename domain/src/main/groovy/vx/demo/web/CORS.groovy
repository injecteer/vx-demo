package vx.demo.web;

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * If specified, the {@link io.vertx.ext.web.handler.CorsHandler CorsHandler} is added to the main endpoint in dev-mode.<br/>
 * The {@link value} represents origin, like <code>http://localhost:8888</code>.<br/>
 * See also {@link io.vertx.ext.web.handler.CorsHandler#addOrigin addOrigin()}
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( [ ElementType.TYPE ] )
@interface CORS {
  String value()
}
