package vx.demo.web

import static io.vertx.core.http.HttpMethod.*
import static io.vertx.core.json.JsonObject.mapFrom

import org.apache.log4j.Logger

import com.fasterxml.jackson.databind.SerializationFeature

import groovy.transform.TypeChecked
import groovy.yaml.YamlSlurper
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpMethod
import io.vertx.core.impl.launcher.commands.VersionCommand
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.Router
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.CorsHandler
import vx.demo.gorm.Bootstrap

@TypeChecked
class WebVerticle extends AbstractVerticle {
  
  static final Set<HttpMethod> CORS_METHODS = [ GET, POST, PUT, DELETE, OPTIONS ] as Set
  
  static final Set<String> CORS_HEADERS = [ 'Content-Type', 'Authorization', 'Access-Control-Allow-Headers', 'Access-Control-Allow-Method', 'Access-Control-Allow-Origin', 'Access-Control-Allow-Credentials' ] as Set

  protected Map config
  
  protected boolean isStandalone = false

  protected Logger log
  
  protected Router router
  
  WebVerticle() {
    log = Logger.getLogger getClass()
  }
  
  @Override
  void start() throws Exception {
    config = (Map)new YamlSlurper().parse( getClass().getResourceAsStream( '/application.yml' ) )

    Bootstrap.init( (Map)config )
    
    router = Router.router vertx
    
    var cors = getClass().getAnnotationsByType CORS
    if( cors ) enableCORS cors.first().value()
      
    DatabindCodec.mapper().configure SerializationFeature.FAIL_ON_EMPTY_BEANS, false
    
    isStandalone = !config().map.nonStandalone
    
    int hcPort = 0
    
    Map<String,Handler<Promise<Status>>> healthChecks2register = healthChecks()
    HealthChecks hc = HealthChecks.create vertx
    healthChecks2register.each{ k, h -> hc.register k, h }
    router.get '/health' handler HealthCheckHandler.createWithHealthChecks( hc )
    
    if( isStandalone ){
      if( 1 == context.instanceCount || Thread.currentThread().name.endsWith( 'thread-2' ) ){
        DatagramSocket socket = new DatagramSocket()
        socket.connect InetAddress.getByName( '8.8.8.8' ), 10002
        
        log.info getClass().getResource( '/banner.txt' ).text
        log.info "Java version   :: ${Runtime.version()}"
        log.info "Groovy version :: $GroovySystem.version"
        log.info "Vert.X version :: $VersionCommand.version"
        log.info "Environment    :: ${WebEnvironment.mode()}"
        log.info "IP-Address     :: $socket.localAddress.hostAddress"
      }
      
      var hco = getClass().getAnnotationsByType HealthCheckOnly
      hcPort = hco ? hco.first().value() : 0
      if( hcPort ){
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
  
  @Override
  void stop() throws Exception {
    super.stop()
    log.info 'stopped'
  }
  
  /**
   * Initializes CORS Handling for dev-mode only
   * @param origin
   */
  protected void enableCORS( String origin, Router rtr = router ){
    if( !WebEnvironment.development() ) return
    CorsHandler cors = CorsHandler.create().addOrigin origin allowCredentials true allowedMethods CORS_METHODS allowedHeaders CORS_HEADERS exposedHeaders CORS_HEADERS
    rtr.route().order( -200 ).handler cors
    log.info "CORS enabled -> $origin" 
  }
  
  protected Map<String,Handler<Promise<Status>>> healthChecks() {
    [ (getClass().simpleName):{ it.complete Status.OK() } as Handler<Promise<Status>> ]
  }
}
