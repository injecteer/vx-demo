package vx.demo.backoffice.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import grails.gorm.transactions.Transactional
import groovy.util.logging.Log4j
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import vx.demo.authorization.Grant
import vx.demo.domain2.Permission
import vx.demo.gorm.Bootstrap
import vx.demo.web.Controller

@Log4j
@Component
class ConsoleController implements Controller {
  
  private final String domainPackageImports
  
  private final GroovyShell groovyShell
  
  @Autowired
  ConsoleController( Vertx vertx, Router router ) {
    domainPackageImports = Bootstrap.instance.domainPackages.collect{ "import ${it}.*" }.join '\n'
    groovyShell = new GroovyShell( getClass().classLoader, [ vertx:vertx ] as Binding )
    router.put '/console/execute' consumes JSON produces JSON blockingHandler this.&execute
  }
  
  @Transactional
  @Grant([ Permission.admin ])
  void execute( RoutingContext rc ) {
    Map params = params rc
    
    try{
      long start = System.currentTimeMillis()
      Script script = groovyShell.parse( domainPackageImports + '\n' + params.script )
      Map res = [ result:script.run() ]
      res.message = "Script ran successfully in ${System.currentTimeMillis() - start} ms".toString()
      ok rc, res
    }catch( Throwable e ){
      err rc, e, 418
    }
  }
  
}
