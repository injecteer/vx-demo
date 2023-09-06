package vx.demo.web

import org.apache.log4j.Logger

import groovy.transform.TypeChecked
import io.vertx.core.AbstractVerticle

@TypeChecked
class WebVerticle extends AbstractVerticle {

  boolean isStandalone = false

  protected Logger log
  
  WebVerticle() {
    log = Logger.getLogger getClass()
  }
  
  @Override
  void start() throws Exception {
    isStandalone = !config().map.nonStandalone
    if( isStandalone ){
      log.info getClass().getResource( '/banner.txt' ).text
    }
    log.info 'starting ...'
  }
}
