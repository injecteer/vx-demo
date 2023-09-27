package vx.demo.backoffice.controller

import org.springframework.context.MessageSource

import grails.gorm.transactions.Transactional
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import vx.demo.authorization.Grant
import vx.demo.domain.LogEvent
import vx.demo.domain2.Permission
import vx.demo.domain2.User
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
    
    boolean my = params.my?.toBoolean()
    boolean successful = params.successful?.toBoolean()
    List whats = params.whats as List
    
    User u = getUser rc
    
    def list = LogEvent.createCriteria().list{
      if( my ) user{ eq 'id', u.id }
      if( successful ) eq 'success', true
      if( whats ) 'in' 'what', whats
      
      order 'id', 'desc'
      firstResult params.offset?.toInteger() ?: 0
      maxResults params.max?.toInteger() ?: 20
    }
    
    int count = LogEvent.createCriteria().count{
      if( my ) user{ eq 'id', u.id }
      if( successful ) eq 'success', true
      if( whats ) 'in' 'what', whats
    }
    
    ok rc, [ list:list, count:count ]
  }
   
}
