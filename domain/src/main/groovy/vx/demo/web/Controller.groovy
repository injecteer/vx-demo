package vx.demo.web

import org.grails.datastore.gorm.GormEntity
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Contains convenience methods for response rendering
 */
trait Controller {
  
  MessageSource messageSource
  
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
  
  List errors2messages( GormEntity o ) {
    o.errors.fieldErrors.collect{ FieldError fe ->
      fe.codes.findResult{ String c -> messageSource?.getMessage c, fe.arguments, null, Locale.default } ?: fe.codes.last()
    }
  }
}
