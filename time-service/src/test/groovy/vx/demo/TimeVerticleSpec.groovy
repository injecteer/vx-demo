package vx.demo

import io.vertx.core.Future
import io.vertx.ext.healthchecks.Status
import vx.demo.gorm.StatisticsService
import vx.demo.test.VertxSpecification

class TimeVerticleSpec extends VertxSpecification {

  def 'send valid timeZone over event-bus and get formatted time string back'() {
    given:
    String timeZone = 'Europe/Berlin'
    String res
    boolean success
    
    when:
    vertx.eventBus().request( 'time', timeZone ){
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
    vertx.eventBus().request( 'time', 'INVALID!' ){
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
    4.times{ futs << vertx.eventBus().request( 'time', 'GMT' ) }
    6.times{ futs << vertx.eventBus().request( 'time', 'INVALID' ) }
    
    then: 'wait for completion and check that stats numbers grew'
    Status result
    Future.join futs onComplete{ result = ss.collectStats() }
    
    await{
      result.data.map.ok == initial.data.map.ok + 4
      result.data.map.err == initial.data.map.err + 6
    }
  }
  
}
