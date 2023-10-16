package vx.demo.web

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import groovy.util.logging.Log4j
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.eventbus.ReplyFailure

@Log4j
class EventBusValidator {

  private static final DeliveryOptions DO = new DeliveryOptions( sendTimeout:1_500 )

  final Vertx vertx

  final String address

  EventBusValidator( Vertx vertx, String address ) {
    this.vertx = vertx
    this.address = address
  }

  /**
   *
   * @param o
   * @return validation error message (key) or true if valid
   */
  def validate( o ) {
    CountDownLatch countDownLatch = new CountDownLatch( 1 )
    
    Future<Message> fut = vertx.eventBus().request( address, o, DO ).onComplete{ countDownLatch.countDown() }
    
    boolean done = countDownLatch.await 4, TimeUnit.SECONDS
    
    if( !done )
      'service.failure'
    else if( fut.failed() ){
      ReplyException cause = (ReplyException)fut.cause()
      ReplyFailure.RECIPIENT_FAILURE == cause?.failureType ? cause.message : 'service.failure'
    }else
      true
  }
}
