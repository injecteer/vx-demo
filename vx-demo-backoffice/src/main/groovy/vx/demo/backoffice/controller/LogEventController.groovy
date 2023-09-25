package vx.demo.backoffice.controller

import org.springframework.context.MessageSource

import grails.gorm.transactions.Transactional
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import vx.demo.authorization.Grant
import vx.demo.domain.LogEvent
import vx.demo.domain2.Permission
import vx.demo.web.CRUDController

@Grant( [ Permission.kunde ] )
class LogEventController extends CRUDController<LogEvent> {
  
  LogEventController( Router router, MessageSource messageSource ){
    super( router, messageSource, LogEvent )
  }
 
  @Transactional
  @Override
  void list( RoutingContext rc ) {
    Map params = params rc
    int offset = params.offset?.toInteger() ?: 0
    int max = params.max?.toInteger() ?: 20
    def list = LogEvent.list( offset:offset, max:max, sort:'id', order:'desc' )
    ok rc, [ list:list, count:clazz.count() ]
  }
   
}
