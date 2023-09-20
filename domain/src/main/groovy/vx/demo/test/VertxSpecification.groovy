package vx.demo.test

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * For testing only. 
 */
abstract class VertxSpecification extends Specification {

  @Shared Vertx vertx
  
  @Shared String verticleId
  
  PollingConditions conditions = new PollingConditions( timeout:5 )
  
  Map deploymentOptions() { [:] }
  
  def setupSpec() {
    vertx = Vertx.vertx()
    String v = getClass().name - 'Test' - 'Specification' - 'Spec'
    vertx.deployVerticle( v, deploymentOptions() as DeploymentOptions ){ verticleId = it.result() }
    while( !verticleId ) Thread.sleep 500
  }

  void await( Closure c ) {
    c.delegate = this
    conditions.eventually c
  }
  
  def cleanupSpec() {
    vertx.undeploy( verticleId ){ 
      vertx.close()
      verticleId = null 
    }
    while( verticleId ) Thread.sleep 500
  }
  
}
