package vx.demo

import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Promise
import vx.demo.web.WebVerticle

@TypeChecked
class MothershipVerticle extends WebVerticle {

  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    List<Future> futs = [ 
      (TimeVerticle):[ instances:2, config:[ nonStandalone:true ] ], 
      (DemoVerticle):[ config:[ nonStandalone:true ] ] 
    ].collect ( clazz, opts ) -> vertx.deployVerticle clazz, opts as DeploymentOptions
    
    Future.all futs onComplete{ 
      log.info "${futs.size()} verticles started successfully"
      startPromise.handle it
    } 
  }
}
