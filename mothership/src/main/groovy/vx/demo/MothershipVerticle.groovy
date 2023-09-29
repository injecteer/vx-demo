package vx.demo

import static groovy.transform.TypeCheckingMode.SKIP
import static io.vertx.core.json.JsonObject.mapFrom

import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.Status
import vx.demo.web.HealthCheckOnly
import vx.demo.web.WebVerticle

@TypeChecked
@HealthCheckOnly( 8099 )
class MothershipVerticle extends WebVerticle {

  final Map<Class,?> VERTICLES = [
    (TimeVerticle):[ config:[ nonStandalone:true ] ],
    (WeatherVerticle):[ worker:true, config:[ nonStandalone:true ] ],
    (DemoVerticle):[ config:[ nonStandalone:true ] ],
  ]
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    List<Future> futs = VERTICLES.collect ( clazz, opts ) -> vertx.deployVerticle clazz, opts as DeploymentOptions
    
    Future.all futs onComplete{ 
      log.info "${futs.size()} verticles started successfully"
      startPromise.handle it
    }
  }
  
  @Override
  @TypeChecked( SKIP )
  Map<String, Handler<Promise<Status>>> healthChecks() {
    [ health:{ Promise<Status> prom ->
      List<Future<Message<JsonObject>>> futs = VERTICLES.keySet().collect{ vertx.eventBus().<JsonObject>request "health-$it.simpleName", null }
      
      Future.join futs onComplete{ AsyncResult ar ->
        List<Map> data = futs.findResults{ it.succeeded() ? it.result().body().map : null }
        List checks = data*.checks.flatten()
        String status = 'UP' == data*.status.unique().first() ? 'OK' : 'KO'
        prom.complete Status."$status"( mapFrom( [ checks:checks ] ) )
      }
    } as Handler<Promise<Status>> ]
  }
}
