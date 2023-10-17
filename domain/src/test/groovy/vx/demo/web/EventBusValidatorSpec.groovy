package vx.demo.web

import io.vertx.core.AbstractVerticle
import spock.lang.Unroll
import vx.demo.test.Deploy
import vx.demo.test.VertxSpecification

@Deploy( PingVerticle )
@Unroll
class EventBusValidatorSpec extends VertxSpecification {
  
  def 'test correct address: [#input] should bring /#res/'() {
    given:
    EventBusValidator ebv = new EventBusValidator( vertx, 'ping' )
    
    expect:
    ebv.validate( input ) == res
    
    where:
    input     | res
    null      | 'ko'
    'ing'     | 'ko'
    'ping'    | true
  }
  
  def 'test invalid address'() {
    when:
    EventBusValidator ebv = new EventBusValidator( vertx, 'INVALID' )
    
    then:
    ebv.validate( 'ping' ) == 'service.failure'
  }
  
  static class PingVerticle extends AbstractVerticle {
    void start() {
      vertx.eventBus().consumer( 'ping' ){ 'ping' == it.body() ? it.reply( 'pong' ) : it.fail( 400, 'ko' ) }
    }
  }  
}
