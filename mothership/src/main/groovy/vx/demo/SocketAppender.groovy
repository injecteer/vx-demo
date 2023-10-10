package vx.demo

import static io.vertx.core.json.JsonObject.mapFrom

import org.slf4j.LoggerFactory

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.vertx.ext.web.handler.sockjs.SockJSSocket

class SocketAppender extends AppenderBase<ILoggingEvent> {

  private SockJSSocket socket
  
  private static final LoggerContext ctx = LoggerFactory.ILoggerFactory
  
  private static final Logger log = ctx.getLogger 'ROOT'

  private static SocketAppender me
  
  static {
    log.level = Level.INFO
  }
  
  @Override
  void append( ILoggingEvent event ) {
    socket.write mapFrom( type:'log', body:event.formattedMessage ).toBuffer()
  }

  static void enable( SockJSSocket socket ) {
    me = new SocketAppender( socket:socket )
    me.context = ctx
    me.start()
    log.addAppender me
  }
  
  static void disable() {
    log.detachAppender me
  }
}
