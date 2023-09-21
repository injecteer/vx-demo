package vx.demo.domain2

import org.grails.datastore.gorm.GormEntity

import grails.gorm.annotation.Entity
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@Entity
@TupleConstructor( excludes=[ 'id' ] )
@EqualsAndHashCode( includes=[ 'email', 'name' ] )
class User implements GormEntity<User> {
  
  long id
  
  String name
  
  String email
  
  String password
  
  Integer permissionMask = Permission.kunde.asMask()
  
  Date birthDate
  
  Address address
  
  Date dateCreated
  
  Date lastUpdated
  
  List<String> getPermissions() {
    permissions()*.name()
  }
  
  void setPermissions( List<String> list ) {
    setPermissionMask list.inject( 0 ){ int res, String p -> res | Permission.valueOf( p ).asMask() }
  }
  
  List<Permission> permissions() {
    Permission.values().findAll{ permissionMask & it.asMask() }
  }
  
  boolean isOf( Permission... permisions ) {
    permisions.any{ permissionMask & it.asMask() }
  }
  
  boolean notOf( Permission... permisions ) {
    permisions.every{ !( permissionMask & it.asMask() ) }
  }
  
  static hasOne = [ address:Address ]
  
  static constraints = {
    name blank:false, matches:/(\p{L}+\s?)+/
    email unique:true
    permissionMask min:1
    birthDate validator:{
      Date now = new Date()
      now - 130 * 365 < it && it < now - 10 * 365
    }
  }
}
