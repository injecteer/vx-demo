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
import vx.demo.web.CORS
import vx.demo.web.WebVerticle

@TypeChecked
@CORS( 'http://localhost:3010' )
class BackofficeVerticle extends WebVerticle {

  static final int HTTP = 8097
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    router.route().handler FaviconHandler.create( vertx, '/webroot/favicon.ico' )
    router.route().failureHandler{
      log.error "${it.request().method()}:${it.normalizedPath()}", it.failure()
      it.next()
    }
    router.route().failureHandler ErrorHandler.create( vertx, WebEnvironment.development() )
    
    initSpringCtx()
    
    router.get '/static/*' handler StaticHandler.create().setDefaultContentEncoding( 'UTF-8' )
    
    Buffer indexHtml = Buffer.buffer getClass().getResource( '/index.html' ).text
    router.get '/*' handler{ it.end indexHtml }
    
    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
  }

  private void initSpringCtx() {
    Router api = Router.router vertx
    
    ApplicationContext parent = new GenericApplicationContext()
    
    parent.beanFactory.with{
      registerSingleton 'vertx', vertx
      registerSingleton 'mainRouter', router
      registerSingleton 'router', api
      registerSingleton 'cfg', config
    }
    parent.refresh()

    var context = new ClassPathXmlApplicationContext( [ 'ctx.xml' ] as String[], parent )
    
    log.info "Spring context initialized with ${context.beanDefinitionNames.size()} beans"
    
    router.route '/api/*' subRouter api
  }
  
}
