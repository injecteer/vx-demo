package vx.demo

import org.springframework.context.MessageSource

import grails.gorm.transactions.Transactional
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import vx.demo.domain.LogEvent
import vx.demo.web.Controller

@Transactional
class LogEventController implements Controller {

  LogEventController( Router router, MessageSource messageSource ) {
    this.messageSource = messageSource
    router.get '/api/logEventIds' produces JSON handler this.&ids
    router.get '/api/logEvent/:id' produces JSON handler this.&details
    router.post '/api/logEvent' consumes JSON produces JSON handler this.&create
    router.delete '/api/logEvent/:id' handler this.&delete
  }
  
  void ids( RoutingContext rc ) {
    ok rc, [ list:LogEvent.list( max:100 )*.id ]
  }
  
  void details( RoutingContext rc ) {
    String id = rc.pathParam 'id'
    LogEvent le = id ? LogEvent.read( id ) : null
    if( le )
      ok rc, le
    else
      notFound rc
  }
  
  void create( RoutingContext rc ) {
    LogEvent le = new LogEvent( rc.body().asJsonObject().map )
    if( le.save() )
      ok rc, le
    else
      err rc, [ errors:errors2messages( le ) ], 400
  }
  
  void delete( RoutingContext rc ) {
    String id = rc.pathParam 'id'
    LogEvent le = id ? LogEvent.get( id ) : null
    if( le ){
      le.delete()
      ok rc
    }else
      notFound rc
  }
  
}
