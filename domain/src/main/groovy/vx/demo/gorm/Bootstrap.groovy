package vx.demo.gorm

import java.util.concurrent.atomic.AtomicBoolean

import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

import grails.gorm.annotation.Entity
import groovy.transform.TypeChecked
import groovy.util.logging.Log4j

@Log4j
@TypeChecked
class Bootstrap {

  static AtomicBoolean initialized = new AtomicBoolean( false )
  
  static synchronized void init( Map config ) {
    if( initialized.get() ) return
    
    ClassPathScanningCandidateComponentProvider compProvider = new ClassPathScanningCandidateComponentProvider( false )
    compProvider.addIncludeFilter new AnnotationTypeFilter( Entity )
    
    List<Class<?>> domainClasses = config.domainPackages.inject( [] ){ List<Class<?>> res, String pckg ->
      compProvider.findCandidateComponents( pckg ).each{ res << Class.forName( it.beanClassName ) }
      res
    }
    
    log.info "initialized ${domainClasses.size()} domain classes"
    new HibernateDatastore( (Map)config.gorm, domainClasses as Class<?>[] )
    
    initialized.set true
  }
}
