package vx.demo.rmi

import io.vertx.core.Promise
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import vx.demo.web.WebVerticle

/**
 * 
 */
class RMIVerticle extends WebVerticle {

  final int HTTP = 8095
  
  @Override
  void start( Promise startPromise ) throws Exception {
    super.start()
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    
    new RMIController( router )
    
    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
    
  }
}
