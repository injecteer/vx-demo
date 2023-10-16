package vx.demo.domain2

import org.grails.datastore.gorm.GormEntity
import org.mindrot.jbcrypt.BCrypt
import vx.demo.web.EventBusValidator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import grails.gorm.annotation.Entity
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@Entity
@TupleConstructor( excludes=[ 'id' ] )
@EqualsAndHashCode( includes=[ 'email', 'name' ] )
@JsonIgnoreProperties( [ 'password', 'addressId', 'hibernateLazyInitializer', 'permissionMask', 'forcePasswordReset' ] )
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
  
  transient boolean forcePasswordReset = false
  
  static EventBusValidator passwordValidator
  
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
  
  /**
   * Lifecycle method to encrypt the password.
   */
  def beforeInsert() {
    encodePassword()
  }
  
  /**
   * Lifecycle method to update the encrypted password in case of it has been changed.
   */
  def beforeUpdate() {
    if( isDirty( 'password' ) ) 
      forcePasswordReset ? encodePassword() : setPassword( getPersistentValue( 'password' ) )
  }
     
  /**
   * Method supposed to encrypt the password. Automatically called on lifecycle events.
   * Implementations of this method should encrypt the password.
   *
   * @return a String containing the encrypted password
   */
  String encodePassword() {
    setPassword BCrypt.hashpw( password, BCrypt.gensalt() )
  }
  
  static hasOne = [ address:Address ]
  
  static transients = [ 'permissions' ]
  
  static constraints = {
    name blank:false, matches:/(\p{L}+\s?)+/
    password validator:{ String pw, User u -> !u.id || u.isDirty( 'password' ) && u.forcePasswordReset ? passwordValidator.validate( pw ) : true }
    email unique:true
    permissionMask min:1
    birthDate validator:{
      Date now = new Date()
      now - 130 * 365 < it && it < now - 10 * 365
    }
    address nullable:true
  }
}
