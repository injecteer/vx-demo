package vx.demo.web

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

trait Controller {
  
  static final String JSON = 'application/json'
  
  void ok( RoutingContext rc, o = null, int code = 200 ) {
    respond rc, o, code
  }
  
  void err( RoutingContext rc, o = null, int code = 400 ) {
    respond rc, o, code
  }
  
  void notFound( RoutingContext rc, o = null ) {
    respond rc, o, 404
  }
  
  void respond( RoutingContext rc, o, int code = 200 ) {
    rc.response().setStatusCode Math.max( 0, code )
    if( !o ){ 
      rc.json ''
      return
    }
    switch( o ){
      case Enum:
        o = [ body:o.name() ]
        break
      case Number:
      case String:
        o = [ body:o ]
        break
      case Throwable:
        o = [ error:o.message ]
        break
      case JsonObject:
        break
      default:
        o = JsonObject.mapFrom o
    }
    rc.json o as JsonObject
  }

}
