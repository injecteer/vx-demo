package vx.demo

import static groovy.transform.TypeCheckingMode.SKIP
import static io.vertx.core.json.JsonObject.mapFrom

import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.FaviconHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import vx.demo.web.WebVerticle

@TypeChecked
class MothershipVerticle extends WebVerticle {

  final int HTTP = 8099
  
  private final Map<String,Tuple2<Class,DeploymentOptions>> verticles = [:]
  
  private final Map<String,String> states = [:]
  
  private final Map CFG = [ config:[ nonStandalone:true ] ]
  
  MothershipVerticle() {
    getClass().getResourceAsStream( '/verticles.txt' ).eachLine{ 
      verticles[ it ] = new Tuple2<>( Class.forName( it ), CFG as DeploymentOptions )
      states[ it ] = null as String
    }
    log.info "found ${verticles.size()} verticles"
  }
  
  @Override
  void start( Promise startPromise ) throws Exception {
    start()
    
    enableCORS 'http://localhost:3011'
    
    router.route().handler BodyHandler.create()
    router.route().handler ResponseContentTypeHandler.create()
    router.route().handler FaviconHandler.create( vertx, '/webroot/favicon.ico' )
    router.route().failureHandler{
      log.error "${it.request().method()}:${it.normalizedPath()}", it.failure()
      it.next()
    }
    router.route().failureHandler ErrorHandler.create( vertx, WebEnvironment.development() )
    
    router.route '/ws/*' subRouter SockJSHandler.create( vertx ).socketHandler( this::sockJS )
    
    router.get '/static/*' handler StaticHandler.create().setDefaultContentEncoding( 'UTF-8' )
    
    Buffer indexHtml = Buffer.buffer getClass().getResource( '/index.html' ).text
    router.get '/*' handler{ it.end indexHtml }
    
    vertx.createHttpServer().requestHandler router listen HTTP, startPromise
  }
  
  void sockJS( SockJSSocket socket ) {
    writeStates socket // initial state

    socket.handler{ req ->
      Map cmd = req.toJsonObject().map
      
      String verticle = cmd.verticle
      Tuple2 tuple = verticles[ verticle ]
      
      log.info "<< $cmd"
      
      switch( cmd.command ){
        case 'start':
          vertx.deployVerticle( tuple.v1, tuple.v2 ){
            states[ verticle ] = it.succeeded() ? it.result() : null
            if( it.succeeded() )
              writeStates socket
            else
              writeErr socket, verticle, it.cause()
          }
          break
          
        case 'stop':
          vertx.undeploy( states[ verticle ] ){
            states[ verticle ] = (String)null
            writeStates socket
          }
          break
          
        case 'health':
        case 'health-all':
          String clazz = 'health' == cmd.command ? tuple.v1.simpleName : MothershipVerticle.simpleName
          vertx.eventBus().<JsonObject>request( "health-$clazz", null ){
            if( it.succeeded() )
              socket.write mapFrom( type:'info', body:it.result().body() ).toBuffer()
            else
              writeErr socket, verticle, it.cause()
          }
          break
      }
    }    
  }
  
  private void writeStates( SockJSSocket socket ) {
    socket.write mapFrom( type:'content', body:states ).toBuffer()
  }
  
  private void writeErr( SockJSSocket socket, String verticle, Throwable t ) {
    socket.write mapFrom( type:'error', verticle:verticle, body:t.message ).toBuffer()
  }
  
  @Override
  @TypeChecked( SKIP )
  Map<String, Handler<Promise<Status>>> healthChecks() {
    [ health:{ Promise<Status> prom ->
      List<Future<Message<JsonObject>>> futs = states.findResults{ k, v -> v ? vertx.eventBus().<JsonObject>request( "health-${verticles[ k ].v1.simpleName}", null ) : null }
      
      Future.join futs onComplete{ AsyncResult ar ->
        List<Map> data = futs.findResults{ it.succeeded() ? it.result().body().map : null }
        List checks = data*.checks.flatten()
        String status = 'UP' == data*.status.unique().first() ? 'OK' : 'KO'
        prom.complete Status."$status"( mapFrom( [ checks:checks ] ) )
      }
    } as Handler<Promise<Status>> ]
  }
}
