package vx.demo

import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.FaviconHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import vx.demo.web.Controller
import vx.demo.web.HealthCheckOnly
import vx.demo.web.WebVerticle

@TypeChecked
class DemoVerticle extends WebVerticle implements Controller {

  static final int HTTP = 8090
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    router.route().handler FaviconHandler.create( vertx )
    router.route().failureHandler ErrorHandler.create( vertx, WebEnvironment.development() )
    
    // log and pass
    router.route '/api/*' handler {
      log.info "trace < ${it.request().method()}:${it.normalizedPath()}"
      it.next()
    }
    
    router.get '/api/time/:zone' produces JSON handler this::time
    router.get '/api/time' produces JSON handler this.&time
    
    router.get '/*' handler StaticHandler.create().setCachingEnabled( false ).setDefaultContentEncoding( 'UTF-8' )

    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
  }
  
  void time( RoutingContext rc ) {
    String body = rc.pathParam( 'zone' ) ?: 'GMT'
    vertx.eventBus().request( 'time', body ){ AsyncResult<Message<String>> res ->
      if( res.succeeded() ){
        ok rc, res.result().body()
      }else{
        ReplyException e = (ReplyException)res.cause()
        err rc, [ type:e.failureType, why:e.message ], e.failureCode()
      }
    }
  }
}
