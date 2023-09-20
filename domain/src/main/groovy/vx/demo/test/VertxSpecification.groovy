package vx.demo.test

import org.apache.log4j.Logger

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * For testing only. 
 */
//TODO: move it to a fixtures-project eventually
abstract class VertxSpecification extends Specification {

  @Shared Logger log
  
  @Shared Vertx vertx
  
  @Shared String verticleId
  
  PollingConditions conditions = new PollingConditions( timeout:5 )
  
  Map deploymentOptions() { [:] }
  
  def setupSpec() {
    log = Logger.getLogger getClass()
    vertx = Vertx.vertx()
    String v = getClass().name - 'Test' - 'Specification' - 'Spec'
    log.info "deploying $v ..."
    vertx.deployVerticle( v, deploymentOptions() as DeploymentOptions ){ verticleId = it.result() }
    while( !verticleId ) Thread.sleep 500
    log.info "$v deployed successfully"
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
    log.info 'context shutdown' 
  }
  
}
