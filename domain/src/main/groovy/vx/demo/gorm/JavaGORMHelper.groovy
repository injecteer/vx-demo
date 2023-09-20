package vx.demo.gorm

import java.util.function.Consumer

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Session
import org.springframework.transaction.TransactionStatus

import groovy.util.logging.Log4j

/**
 * Simple delegation to GORM API static methods
 */
@Log4j
class JavaGORMHelper {
  
  static <T extends GormEntity> void withTransaction( Class<T> clazz, Consumer<TransactionStatus> action ) {
    clazz.withTransaction{ action it }
  }
  
  static <T extends GormEntity> void withNewSession( Class<T> clazz, Consumer<Session> action ) {
    clazz.withNewSession{ action it }
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
  
  static <T extends GormEntity> List<T> findAllBy( Class<T> clazz, String what, Map<String,Object> params = [:], Object... args ) {
    clazz."findAllBy$what"( *args, params )
  }
  
  static <T extends GormEntity> T findBy( Class<T> clazz, String what, Object... args ) {
    clazz."findBy$what"( *args )
  }
  
  static <T extends GormEntity> T countBy( Class<T> clazz, String what, Object... args ) {
    clazz."countBy$what"( *args )
  }
  
}
