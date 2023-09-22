package vx.demo.authorization

import org.apache.log4j.Logger

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import io.vertx.ext.web.RoutingContext
import vx.demo.domain2.Permission
import vx.demo.domain2.User

@CompileStatic
class GrantUtil {
  
  private static final Logger log = Logger.getLogger GrantUtil

  //for tests only!
  @PackageScope static Closure onOk, onFail
    
  static boolean checkAuthorization( RoutingContext rc, List<Permission> grants ) {
    Map principal = rc.user()?.principal()?.map
    
    String id = principal?.id
    if( !id ) return false
    
    //TODO: decrypt id?
    
    User ua = new User( permissions:principal.permissions as List<String> )
    
    if( ua.isOf( grants as Permission[] ) ){
      log.info "${rc.normalizedPath() ?: ''} ${ua.permissions.join( ', ' )} == ${grants.join( ', ' )}"
      onOk?.call()
      true
    }else{
      log.info "${rc.normalizedPath() ?: ''} ${ua.permissions.join( ', ' )} !! ${grants.join( ', ' )}"
      onFail?.call()
      rc.fail 403
      false
    }
  }
}
