package vx.demo.backoffice

import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource

import groovy.transform.TypeChecked
import io.vertx.core.Promise
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.FaviconHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import vx.demo.backoffice.controller.ConsoleController
import vx.demo.backoffice.controller.LogEventController
import vx.demo.backoffice.controller.SecurityController
import vx.demo.backoffice.controller.UserController
import vx.demo.web.WebVerticle

@TypeChecked
class BackofficeVerticle extends WebVerticle {

  static final int HTTP = 8097
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    MessageSource messageSource = new ResourceBundleMessageSource( defaultEncoding:'UTF-8', basename:'i18n.messages' )
    
    enableCORS 'http://localhost:3010'
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    router.route().handler FaviconHandler.create( vertx )
    router.route().failureHandler{
      log.error "${it.request().method()}:${it.normalizedPath()}", it.failure()
      it.next()
    }
    router.route().failureHandler ErrorHandler.create( vertx, WebEnvironment.development() )
    
    new SecurityController( vertx, router, (Map)config.security, messageSource )
    
    new LogEventController( router, messageSource )
    new UserController( router, messageSource )
    new ConsoleController( router )
    
    router.get '/*' handler StaticHandler.create().setDefaultContentEncoding( 'UTF-8' )

    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
  }
  
}
