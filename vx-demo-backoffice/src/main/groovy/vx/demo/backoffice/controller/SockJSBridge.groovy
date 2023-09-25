package vx.demo.backoffice.controller

import static io.vertx.ext.bridge.BridgeEventType.*

import java.util.concurrent.ConcurrentHashMap

import groovy.util.logging.Log4j
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.auth.User
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket

@Log4j
class SockJSBridge implements Handler<BridgeEvent> {
  
  static final SockJSBridgeOptions SOCKJS_OPTS = new SockJSBridgeOptions( pingTimeout:90_000 )
    .addInboundPermitted( new PermittedOptions().setAddressRegex( '[a-zA-Z0-9\\.]+' ) )
    .addOutboundPermitted( new PermittedOptions().setAddressRegex( '[a-zA-Z0-9\\.]+' ) )
  
  SecurityController securityController
  
  Map<Long,List<String>> registeredUsers = new ConcurrentHashMap<>()
  
  SockJSBridge( Vertx vertx, Router router, SecurityController securityController ) {
    this.securityController = securityController
    
    Router sjh = SockJSHandler.create vertx bridge SOCKJS_OPTS, this
    
    router.route '/eventbus/*' subRouter sjh 
  }
  
  @Override
  void handle( BridgeEvent event ) {
    if( SOCKET_PING != event.type() ) log.info "got type ${event.type()} -> ${event.rawMessage}"
    
    SockJSSocket socket = event.socket()

    Map msg = event.rawMessage?.map
    String address = msg?.address
    
    try{
      long id = socket.webUser()?.principal()?.map?.id
      
      switch( event.type() ){
        case REGISTER:
          if( id )
            registeredUsers[ id ] << address
          else
            securityController.checkJwt( msg.headers.authorization ){
              if( it.succeeded() ){
                User user = it.result()
                socket.routingContext().user = user
                id = user.principal().map.id
                registeredUsers[ id ] = [ address ]
                event.complete true
              }else{
                log.warn "auth fail ${it.cause()}"
                socket.end Buffer.buffer( 'Bad authorization' )
              }
            }
          break
          
        case UNREGISTER:
          if( id ){
            registeredUsers[ id ]?.remove address
            if( !registeredUsers[ id ] ) registeredUsers.remove id
          }
          event.complete true
          break
          
        case SOCKET_CLOSED:
          if( id ) registeredUsers.remove id
          event.complete true
          break
          
        default:
          event.complete true
      }
    }catch( Throwable t ){
      log.error 'oops', t 
      socket.end Buffer.buffer( t.message )
    }
  }
  
}
