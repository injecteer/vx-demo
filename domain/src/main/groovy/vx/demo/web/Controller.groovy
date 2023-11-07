package vx.demo.web

import org.grails.datastore.gorm.GormEntity
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import vx.demo.domain2.User

/**
 * Contains convenience methods for response rendering
 */
trait Controller {
  
  MessageSource messageSource

  static final String PLAINTEXT = 'text/plain'
    
  static final String JSON = 'application/json'
  
  static Map params( RoutingContext rc ) {
    Map res = [:]
    Closure setter = { k, v -> res[ k ] = v }
    rc.pathParams()?.each setter
    rc.queryParams()?.entries()?.each setter
    HttpServerRequest req = rc.request()
    req.formAttributes()?.entries()?.each setter
    try{
      JsonObject body = rc.body()?.asJsonObject()
      if( body ) res += body.map
    }catch( Exception ignore ){}
    res
  }
  
  void ok( RoutingContext rc, o = null, int code = 200 ) {
    respond rc, o, code
  }
  
  void err( RoutingContext rc, o = null, int code = 400 ) {
    respond rc, o, code
  }
  
  void notFound( RoutingContext rc, o = null ) {
    respond rc, o, 404
  }

  void noAuth( RoutingContext rc, o = null ) {
    respond rc, o, 401
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
        StringWriter sw
        if( 418 == code ){
          sw = new StringWriter()
          o.printStackTrace new PrintWriter( sw )
        } 
        o = [ error:o.message ]
        if( sw ) o.stackTrace = sw.toString()
        break
      case JsonObject:
        break
      case Collection: 
        o = JsonObject.mapFrom( list:new JsonArray( o ) )
        break
      case { it.class.array }:
        o = JsonObject.mapFrom( list:JsonArray.of( o ) )
        break
      default:
        o = JsonObject.mapFrom o
    }
    rc.json o as JsonObject
  }
  
  User getUser( RoutingContext rc ) {
    Map principal = rc.user().principal().map
    new User( id:principal.id, permissions:principal.permissions as List<String> )
  }
  
  User loadUser( RoutingContext rc ) {
    User.get rc.user().principal().map.id
  }
  
  Map<String,String> errors2messagesMap( GormEntity o ) {
    o.errors.fieldErrors.collectEntries{ FieldError fe ->
      [ fe.field, fe.codes.findResult{ String c -> messageSource?.getMessage c, fe.arguments, null, Locale.default } ?: fe.codes.last() ]
    }
  }
  
  List<String> errors2messages( GormEntity o ) {
    errors2messagesMap( o ).values().toList()
  }
}
