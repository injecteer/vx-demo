package vx.demo.domain

import org.grails.datastore.gorm.GormEntity

import grails.gorm.annotation.Entity
import groovy.transform.TupleConstructor

@Entity
@TupleConstructor( excludes=[ 'id' ] )
class LogEvent implements GormEntity<LogEvent> {
  
  long id
  
  String what
  
  boolean success
  
  Date dateCreated
  
  static constraints = {
    what inList:[ 'time', 'weather' ]
  }
}
