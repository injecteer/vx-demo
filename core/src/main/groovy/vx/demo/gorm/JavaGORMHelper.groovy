package vx.demo.gorm

import java.util.function.Consumer

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Session
import org.springframework.transaction.TransactionStatus

import groovy.util.logging.Log4j

@Log4j
class JavaGORMHelper {
  
  static <T extends GormEntity> void withTransaction( Class<T> clazz, Consumer<TransactionStatus> action ) {
    log.info "start TX $clazz.simpleName $action"
    clazz.withTransaction{ action it }
    log.info 'end TX'
  }
  
  static <T extends GormEntity> void withNewSession( Class<T> clazz, Consumer<Session> action ) {
    log.info "start Session $clazz.simpleName $action"
    clazz.withNewSession{ action it }
    log.info 'end Session'
  }
  
  static <T extends GormEntity> List<T> findAll( Class<T> clazz, CharSequence query ) {
    clazz.findAll query
  }
  
  static <T extends GormEntity> T find( Class<T> clazz, CharSequence query ) {
    clazz.find query
  }
  
  static <T extends GormEntity> T get( Class<T> clazz, id ) {
    clazz.get id
  }
  
  static <T extends GormEntity> T read( Class<T> clazz, id ) {
    clazz.read id
  }
  
  static <T extends GormEntity> List<Object[]> executeQuery( Class<T> clazz, CharSequence query ) {
    clazz.executeQuery( query ) as List
  }
  
}
