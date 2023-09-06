package vx.demo.web

import groovy.transform.TypeChecked
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

@TypeChecked
trait Controller {
  
  static final String JSON = 'application/json'
  
  void ok( RoutingContext rc, o, int code = 200 ) {
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
      rc.json null
      return
    }
    JsonObject jo
    switch( o ) {
    case Enum:
      jo = [ body:((Enum)o).name() ] as JsonObject
      break
    case Number:
    case String:
      jo = [ body:o ] as JsonObject
      break
    case Throwable:
      jo = [ error:((Throwable)o).message ] as JsonObject
      break
    case JsonObject:
      jo = (JsonObject)o
      break
    default:
      jo = JsonObject.mapFrom o
    }
    rc.json jo
  }

}
