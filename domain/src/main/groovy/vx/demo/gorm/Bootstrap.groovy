package vx.demo.gorm

import static java.lang.reflect.Modifier.*

import java.util.concurrent.atomic.AtomicBoolean

import org.grails.orm.hibernate.HibernateDatastore

import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import vx.demo.ast.AutoGORMInitializer

@Log4j
@TypeChecked
@AutoGORMInitializer
class Bootstrap {

  private AtomicBoolean initialized = new AtomicBoolean( false )
  
  private static Bootstrap me
  
  private Bootstrap() {}
  
  static Bootstrap getInstance() {
    if( !me ) me = new Bootstrap()
    me
  }
  
  static synchronized void init( Map config ) {
    getInstance().initialize config
  }
  
  private void initialize( Map config ) {
    if( initialized.get() ) return
    
    List<Class> classes = domainClasses.collect{ Class.forName( (String)it ) }
    
    new HibernateDatastore( (Map)config.gorm, classes as Class[] )
    log.info "initialized ${classes.size()} domain classes"
    
    initialized.set true
  }
  
  List<String> getDomainPackages() {
    domainClasses.groupBy{ it[ 0..<it.lastIndexOf( '.' ) ] }.keySet().sort()
  }
}
