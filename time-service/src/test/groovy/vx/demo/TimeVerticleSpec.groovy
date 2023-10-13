package vx.demo

import static io.vertx.core.json.JsonObject.mapFrom
import org.springframework.context.MessageSource
import org.springframework.context.support.ResourceBundleMessageSource

import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.common.WebEnvironment
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import io.vertx.ext.web.handler.FaviconHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import vx.demo.web.Controller
import vx.demo.web.WebVerticle

import groovy.json.JsonSlurper
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.ext.healthchecks.Status
import vx.demo.domain.LogEvent
import vx.demo.gorm.StatisticsService
import vx.demo.test.VertxSpecification

class TimeVerticleSpec extends VertxSpecification {

  def 'send valid timeZone over event-bus and get formatted time string back'() {
    given:
    String timeZone = 'Europe/Berlin'
    
    when:
    String res
    boolean success
    vertx.eventBus().request( 'time', mapFrom( zone:timeZone ) ){
      success = it.succeeded()
      res = it.result()?.body()
    }
    
    then:
    await{
      success == true
      res == new Date().format( 'dd.MM.yyy EEE HH:mm', TimeZone.getTimeZone( timeZone ) )
    }
  }
  
  def 'send invalid timeZone over event-bus and get failure back'() {
    when: 'send bad EB request'
    String res
    boolean failure
    vertx.eventBus().request( 'time', mapFrom( zone:'INVALID!' ) ){
      failure = it.failed()
      res = it.cause().message
    }
    
    then: 'collect and assert data'
    await{
      failure == true 
      res == 'invalid time zone!'
    }
  }
  
  def 'send requests over event-bus -> the statistics should be growing'() {
    given: 'collect initial stats'
    def ss = new StatisticsService( 'time' )
    Status initial = ss.collectStats()
    
    when: 'send 4 good + 6 bad requests'
    def futs = []
    4.times{ futs << vertx.eventBus().request( 'time', mapFrom( zone:'GMT' ) ) }
    6.times{ futs << vertx.eventBus().request( 'time', mapFrom( zone:'INVALID!' ) ) }

    and:
    Status result
    Future.join futs onComplete{ result = ss.collectStats() }
        
    then: 'wait for completion and check that stats numbers grew'
    await{
      result.data.map.ok == initial.data.map.ok + 4
      result.data.map.err == initial.data.map.err + 6
    }
  }
  
  def 'health monitoring endpoint should provide statistics'() {
    setup: 'clear stats'
    LogEvent.withTransaction{
      LogEvent.executeUpdate 'delete from LogEvent'
    }
    def ss = new StatisticsService( 'time' )
    
    when: 'send 3 good + 1 bad requests over EB'
    def futs = []
    3.times{ futs << vertx.eventBus().request( 'time', mapFrom( zone:'GMT' ) ) }
    1.times{ futs << vertx.eventBus().request( 'time', mapFrom( zone:'INVALID!' ) ) }
    
    and:
    Map status
    Future.join futs onComplete{ 
      status = new JsonSlurper().parseText new URL( 'http://localhost:8092/health' ).text
    }
    
    then: 'wait for completion and check status'
    await{
      status.status == 'UP'
      status.checks[ 0 ].id == 'TimeVerticle'
      status.checks[ 0 ].data.err == 1
      status.checks[ 0 ].data.ok == 3
    }
  }

}
