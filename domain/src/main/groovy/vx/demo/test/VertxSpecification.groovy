package vx.demo.test

import java.util.concurrent.CountDownLatch

import org.apache.log4j.Logger

import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * For testing only!!
 */
//TODO: move it to a fixtures-project eventually
abstract class VertxSpecification extends Specification {

  @Shared Logger log
  
  @Shared Vertx vertx
  
  @Shared List<String> verticleIds
  
  PollingConditions conditions = new PollingConditions( timeout:5 )
  
  Map deploymentOptions() { [:] }
  
  def setupSpec() {
    log = Logger.getLogger getClass()
    vertx = Vertx.vertx()
    List<Future<String>> futs
    
    def deploys = getClass().getAnnotationsByType Deploy
    if( deploys )
      futs = deploys.collect{ vertx.deployVerticle it.value(), deploymentOptions() as DeploymentOptions }
    else
      futs = [ vertx.deployVerticle( getClass().name - 'Test' - 'Specification' - 'Spec', deploymentOptions() as DeploymentOptions ) ]
    
    CountDownLatch countDownLatch = new CountDownLatch( 1 ) 
    Future.all futs onComplete{
      if( it.succeeded() ){
        verticleIds = futs*.result()
        countDownLatch.countDown()
      }else
        throw it.cause()
    }
    countDownLatch.await()
    
    log.info "${futs.size()} deployed successfully"
  }

  void await( Closure c ) {
    c.delegate = this
    conditions.eventually c
  }
  
  def cleanupSpec() {
    Future.all verticleIds.collect( vertx.&undeploy ) onComplete{
      vertx.close()
      verticleIds = null 
      log.info 'context shutdown' 
    }
  }
  
}
