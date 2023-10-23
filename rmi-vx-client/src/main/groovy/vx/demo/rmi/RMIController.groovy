package vx.demo.rmi

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean

import groovy.util.logging.Log4j
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import rmi.server.AbrechnungMgrRemote
import rmi.server.AnschriftMgrRemote
import vx.demo.web.Controller

@Log4j
class RMIController implements Controller {

  private Map<String,?> services = [
    abrechnungMgr:new HttpInvokerProxyFactoryBean( serviceUrl:'http://localhost:8070/abrechnungMgr', serviceInterface:AbrechnungMgrRemote ),
    anschriftMgr:new HttpInvokerProxyFactoryBean( serviceUrl:'http://localhost:8070/anschriftMgr', serviceInterface:AnschriftMgrRemote ),
  ]
  
  RMIController( Router router ) {
    router.post '/api/:service/:method' consumes JSON produces JSON blockingHandler this.&run
    
    services = services.collectEntries{ k, v ->
      v.afterPropertiesSet()
      [ k, v.object ]
    } 
  }
  
  void run( RoutingContext rc ) {
    Map params = params rc
    var service = services[ params.service ]
    log.info "run << $params | $service"
    if( !service ){
      notFound rc
      return
    }
    try{
      def res = service."$params.method"( *params.arguments )
      ok rc, res
    }catch( Throwable t ) {
      log.error 'oops', t
      err rc, t.message
    }
  }
  
}
