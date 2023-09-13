package vx.demo.domain2

import org.grails.datastore.gorm.GormEntity

import grails.gorm.annotation.Entity
import groovy.transform.TupleConstructor
import vx.demo.ast.AutoGORMInitializer

@Entity
@TupleConstructor( excludes=[ 'id' ] )
class User implements GormEntity<User> {
  
  long id
  
  String name
  
  String email
  
  Date birthDate
  
  Address address
  
  Date dateCreated
  
  Date lastUpdated
  
  static hasOne = [ address:Address ]
  
  static constraints = {
    name blank:false, matches:/(\p{L}+\s?)+/
    birthDate validator:{ 
      Date now = new Date()
      now - 130 * 365 < it && it < now - 10 * 365
    }
    email unique:true
  }
}
