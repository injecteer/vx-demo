package vx.demo.domain2

import org.grails.datastore.gorm.GormEntity

import grails.gorm.annotation.Entity
import groovy.transform.TupleConstructor
import vx.demo.ast.AutoGORMInitializer

@Entity
@TupleConstructor( excludes=[ 'id' ] )
class Address implements GormEntity<Address> {
  
  long id
  
  String street
  
  String town
  
  String plz
  
  User user
  
  static constraints = {
    street blank:false
    plz matches:/\d{5}/
  }
}
