package vx.demo

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
import vx.demo.web.CORS
import vx.demo.web.WebVerticle

@TypeChecked
@CORS( 'http://localhost:3011' )
class MothershipVerticle extends WebVerticle {

  final int HTTP = 8099
  
  private final Map<String,Tuple3<Class,String,String>> verticles = [:].asSynchronized()
  
  private final Map NON_SA = [ config:[ nonStandalone:true ] ]
  
  MothershipVerticle() {
    getClass().getResourceAsStream( '/verticles.txt' ).splitEachLine( ' >> ' ){ 
      verticles[ it[ 0 ] ] = new Tuple3<Class,String,String>( Class.forName( it[ 0 ] ), null, it[ 1 ] ) 
    }
    log.info "found ${verticles.size()} verticles"
  }
  
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
      Tuple3 tuple = verticles[ verticle ]
      
      log.info "<< $cmd"
      try{
        switch( cmd.command ){
          case 'start':
            vertx.deployVerticle( tuple.v1, ( (Map)cmd.options ?: [:] ) + NON_SA as DeploymentOptions ){
              if( it.succeeded() ){
                verticles[ verticle ] = new Tuple3<Class,String,String>( tuple.v1, it.result(), tuple.v3 )
                writeStates socket
              }else
                writeErr socket, verticle, it.cause()
            }
            break
            
          case 'start-all':
            Map<String, Future<String>> futs = ((Map)cmd.verticles).collectEntries{ cls, v ->
              Tuple3<Class,String,String> t = verticles[ cls ]
              DeploymentOptions o = ( ( (Map)v ?: [:] ) + NON_SA ) as DeploymentOptions
              t.v2 ? Collections.emptyMap() : [ cls, vertx.deployVerticle( t.v1, o ) ] 
            }
            
            Future.join futs.values().toList() onComplete{
              List err = []
              futs.each{ cls, v -> 
                if( v.succeeded() )
                  verticles[ cls ] = new Tuple3<Class,String,String>( verticles[ cls ].v1, v.result(), verticles[ cls ].v3 )
                else
                  err << cls + ': ' + v.cause().message  
              }
              writeStates socket
              if( err ) writeErr socket, 'all', new Exception( err.join( '\n' ) ) 
            }
            break
            
          case 'stop':
            vertx.undeploy( verticles[ verticle ].v2 ){
              if( it.succeeded() ){
                verticles[ verticle ] = new Tuple3<Class,String,String>( tuple.v1, null, tuple.v3 )
                writeStates socket
              }else
                writeErr socket, verticle, it.cause()
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
      }catch( Throwable e ){
        writeErr socket, verticle, e
      }
    }    
  }
  
  private void writeStates( SockJSSocket socket ) {
    socket.write mapFrom( type:'content', body:verticles.collectEntries{ k, v -> [ k, [ v.v2, v.v3 ] ] } ).toBuffer()
  }
  
  private void writeErr( SockJSSocket socket, String verticle, Throwable t ) {
    socket.write mapFrom( type:'error', verticle:verticle, body:t.message ).toBuffer()
  }
  
  @Override
  Map<String, Handler<Promise<Status>>> healthChecks() {
    [ health:{ Promise<Status> prom ->
      List<Future<Message<JsonObject>>> futs = verticles.findResults{ k, v -> 
        v.v2 ? vertx.eventBus().<JsonObject>request( "health-$v.v1.simpleName", null ) : null 
      }.asList()
      
      Future.join futs onComplete{ AsyncResult ar ->
        List<Map> data = futs.findResults{ it.succeeded() ? it.result().body().map : null }.toList()
        List checks = data*.checks.flatten()
        JsonObject json = mapFrom( checks:checks )
        prom.complete !data || 'UP' == data*.status.unique().first() ? Status.OK( json ) : Status.KO( json )
      }
    } as Handler<Promise<Status>> ]
  }
}
