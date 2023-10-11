package vx.demo

import static io.vertx.core.json.JsonObject.mapFrom

import org.slf4j.LoggerFactory

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.encoder.Encoder
import groovy.transform.TypeChecked
import io.vertx.ext.web.handler.sockjs.SockJSSocket

@TypeChecked
class SocketAppender extends AppenderBase<ILoggingEvent> {
  
  private static final String PATTERN = '%d{HH:mm:ss.SSS} %-5level %logger{30} - %msg%n'

  private static final Logger logbackLogger = (Logger)LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME )
  
  static {
    logbackLogger.level = Level.INFO
    logbackLogger.additive = false
  }
  
  private SockJSSocket socket
  
  private Encoder<ILoggingEvent> encoder
  
  private static SocketAppender me
  
  @Override
  void append( ILoggingEvent event ) {
    String res = new String( encoder.encode( event ) )
    socket.write mapFrom( type:'log', body:res ).toBuffer()
  }

  static void enable( SockJSSocket socket ) {
    LoggerContext ctx = (LoggerContext) LoggerFactory.ILoggerFactory
    PatternLayoutEncoder encoder = new PatternLayoutEncoder( pattern:PATTERN, context:ctx )
    encoder.start()
    
    me = new SocketAppender( socket:socket, encoder:encoder, context:ctx )
    me.start()
  
    logbackLogger.addAppender me
  }
  
  static void disable() {
    logbackLogger.detachAppender me
    me = null
  }
}
