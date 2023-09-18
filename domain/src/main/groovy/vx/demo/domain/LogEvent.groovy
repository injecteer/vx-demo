package vx.demo.domain

import org.grails.datastore.gorm.GormEntity

import com.fasterxml.jackson.annotation.JsonFormat

import grails.gorm.annotation.Entity
import groovy.transform.TupleConstructor

@Entity
@TupleConstructor( excludes=[ 'id' ] )
class LogEvent implements GormEntity<LogEvent> {
  
  long id
  
  String what
  
  boolean success
  
  @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = 'yyyy-MM-dd HH:mm', timezone = 'Europe/Berlin' )
  Date dateCreated
  
  static constraints = {
    what inList:[ 'time', 'weather' ]
  }
}
