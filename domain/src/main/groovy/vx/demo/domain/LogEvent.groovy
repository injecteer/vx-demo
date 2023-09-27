package vx.demo.domain

import org.grails.datastore.gorm.GormEntity

import com.fasterxml.jackson.annotation.JsonFormat

import grails.gorm.annotation.Entity
import groovy.transform.TupleConstructor
import vx.demo.domain2.User

@Entity
@TupleConstructor( includes=[ 'what', 'success', 'user' ] )
class LogEvent implements GormEntity<LogEvent> {
  
  long id
  
  String what
  
  User user
  
  boolean success
  
  @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = 'yyyy-MM-dd HH:mm', timezone = 'Europe/Berlin' )
  Date dateCreated
  
  static constraints = {
    what inList:[ 'time', 'weather' ]
    user nullable:true
  }
}
