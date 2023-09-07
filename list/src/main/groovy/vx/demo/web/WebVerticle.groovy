package vx.demo.web

import org.apache.log4j.Logger

import groovy.transform.TypeChecked
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.impl.launcher.commands.VersionCommand
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
    
    isStandalone = !config().map.nonStandalone
    if( isStandalone ){
      if( 1 == context.instanceCount || Thread.currentThread().name.endsWith( 'thread-2' ) ){
        log.info getClass().getResource( '/banner.txt' ).text
        log.info "Java version   :: ${Runtime.version()}"
        log.info "Groovy version :: $GroovySystem.version"
        log.info "Vert.X version :: $VersionCommand.version"
        log.info "Environment    :: ${WebEnvironment.mode()}"
      }
      
      HealthChecks hc = HealthChecks.create vertx
      healthChecks().each{ k, h -> hc.register k, h }
      
      var hoc = getClass().getAnnotationsByType HealthCheckOnly
      int hcPort = hoc ? hoc.first().value() : 0
      if( hcPort ){
        router.get '/health' handler HealthCheckHandler.createWithHealthChecks( hc )
        vertx.createHttpServer().requestHandler router listen hcPort
        log.info "started health check :$hcPort"
      }
    }
    
    log.info 'starting ...'
  }
  
  Map<String,Handler<Promise<Status>>> healthChecks() {
    [ (getClass().simpleName):{ it.complete Status.OK() } as Handler<Promise<Status>> ]
  }
}
