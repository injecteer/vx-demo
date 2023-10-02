package vx.demo.backoffice

import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.context.support.GenericApplicationContext

import groovy.transform.TypeChecked
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.FaviconHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import vx.demo.web.WebVerticle

@TypeChecked
class BackofficeVerticle extends WebVerticle {

  static final int HTTP = 8097
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    enableCORS 'http://localhost:3010'
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    router.route().handler FaviconHandler.create( vertx, '/webroot/favicon.ico' )
    router.route().failureHandler{
      log.error "${it.request().method()}:${it.normalizedPath()}", it.failure()
      it.next()
    }
    router.route().failureHandler ErrorHandler.create( vertx, WebEnvironment.development() )
    
    Router api = Router.router vertx
    initSpringCtx api
    router.route '/api/*' subRouter api
    
    router.get '/static/*' handler StaticHandler.create().setDefaultContentEncoding( 'UTF-8' )
    
    Buffer indexHtml = Buffer.buffer getClass().getResource( '/index.html' ).text
    router.get '/*' handler{ it.end indexHtml }
    
    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
  }

  private void initSpringCtx( Router api ) {
    ApplicationContext parent = new GenericApplicationContext()
    
    parent.beanFactory.with{
      registerSingleton 'vertx', vertx
      registerSingleton 'mainRouter', router
      registerSingleton 'router', api
      registerSingleton 'cfg', config
    }
    parent.refresh()
    log.info "parent -> $parent.beanFactory.singletonNames"

    ApplicationContext context = new ClassPathXmlApplicationContext( [ 'ctx.xml' ] as String[], parent )

    log.info "beans:\n${context.beanDefinitionNames.join( '\n' )}"
  }
  
}
