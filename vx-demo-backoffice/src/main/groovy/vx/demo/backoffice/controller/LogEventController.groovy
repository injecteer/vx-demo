package vx.demo.backoffice.controller

import org.springframework.context.MessageSource

import io.vertx.ext.web.Router
import vx.demo.authorization.Grant
import vx.demo.domain.LogEvent
import vx.demo.domain2.Permission
import vx.demo.web.CRUDController

@Grant( [ Permission.kunde ] )
class LogEventController extends CRUDController<LogEvent> {
  
  LogEventController( Router router, MessageSource messageSource ){
    super( router, messageSource, LogEvent )
  }
  
}
