package vx.demo.password.check

import static io.vertx.core.json.JsonObject.mapFrom

import grails.gorm.transactions.Transactional
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.ext.healthchecks.Status
import vx.demo.domain2.User
import vx.demo.web.HealthCheckOnly
import vx.demo.web.WebVerticle

/**
 * Provides EB consumer <code>password-check</code>, which validates a password
 */
@HealthCheckOnly( 8094 )
class PasswordCheckVerticle extends WebVerticle {

  Map<String,Integer> stats = [ ok:0, ko:0 ]
  
  @Override
  void start() throws Exception {
    super.start()
    
    vertx.eventBus().consumer( 'password-check' ){ Message<String> msg ->
      String err = getError msg.body()
      if( err ){
        msg.fail 400, err
        stats.ko++
      }else{
        msg.reply 'ok'
        stats.ok++
      }
    }
  }
  
  @Transactional
  String getError( String pw ) {
    if( !pw ) 
      'password.blank'
    else{
      int minSize = 3 + Math.log10( User.count() ?: 1 )
      if( minSize > pw.size() ) 
        'password.too.short'
      else 
        pw =~ /\d+/ && pw =~ /[a-z]+/ && pw =~ /[A-Z]+/ ? null : 'password.wrong.content'
    }
  }

  @Override
  Map<String, Handler<Promise<Status>>> healthChecks() {
    [ (getClass().simpleName):{ it.complete Status.OK( mapFrom( stats ) ) } as Handler<Promise<Status>> ]
  }
}
