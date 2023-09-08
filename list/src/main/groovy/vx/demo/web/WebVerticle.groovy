package vx.demo.web

import static io.vertx.core.json.JsonObject.mapFrom

import org.apache.log4j.Logger

import com.fasterxml.jackson.databind.SerializationFeature

import groovy.transform.TypeChecked
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.impl.launcher.commands.VersionCommand
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment

@TypeChecked
class WebVerticle extends AbstractVerticle {

  protected boolean isStandalone = false

  protected Logger log
  
  protected Router router
  
  WebVerticle() {
    log = Logger.getLogger getClass()
  }
  
  @Override
  void start() throws Exception {
    router = Router.router vertx
    
    DatabindCodec.mapper().configure SerializationFeature.FAIL_ON_EMPTY_BEANS, false
    
    isStandalone = !config().map.nonStandalone
    
    int hcPort = 0
    
    Map<String,Handler<Promise<Status>>> healthChecks2register = healthChecks()
    HealthChecks hc = HealthChecks.create vertx
    healthChecks2register.each{ k, h -> hc.register k, h }
    
    if( isStandalone ){
      if( 1 == context.instanceCount || Thread.currentThread().name.endsWith( 'thread-2' ) ){
        log.info getClass().getResource( '/banner.txt' ).text
        log.info "Java version   :: ${Runtime.version()}"
        log.info "Groovy version :: $GroovySystem.version"
        log.info "Vert.X version :: $VersionCommand.version"
        log.info "Environment    :: ${WebEnvironment.mode()}"
      }
      
      var hoc = getClass().getAnnotationsByType HealthCheckOnly
      hcPort = hoc ? hoc.first().value() : 0
      if( hcPort ){
        router.get '/health' handler HealthCheckHandler.createWithHealthChecks( hc )
        vertx.createHttpServer().requestHandler router listen hcPort
        log.info "registered ${healthChecks2register.size()} health check(-s) :$hcPort"
      }
    }
    
    if( !hcPort )
      vertx.eventBus().consumer( "health-${getClass().simpleName}" ){ Message msg ->
        hc.checkStatus{ it.succeeded() ? msg.reply( mapFrom( it.result().toJson() ) ) : msg.fail( 0, it.cause().message ) }
      }
    log.info 'starting ...'
  }
  
  protected Map<String,Handler<Promise<Status>>> healthChecks() {
    [ (getClass().simpleName):{ it.complete Status.OK() } as Handler<Promise<Status>> ]
  }
}
