package vx.demo.web

import static groovy.transform.TypeCheckingMode.SKIP

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

import org.apache.log4j.Logger
import org.mindrot.jbcrypt.BCrypt

import grails.gorm.transactions.Transactional
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.http.Cookie
import io.vertx.core.http.CookieSameSite
import io.vertx.ext.auth.User as VUser
import io.vertx.ext.web.RoutingContext
import vx.demo.domain2.Permission
import vx.demo.domain2.User

@Transactional
@TypeChecked
abstract class SecurityControllerBase implements Controller {
  
  protected final Logger log
  
  static class Config {
    
    String skip

    int sessionDuration
    
    int refreshTreshold = 300
    
    Map pubSecKeys
    
    String cookieName = 'rememberme'
    
    long cookieExpires = 60 * 24 * 7 * 2
  }
  
  final Config config
  
  SecurityControllerBase( Map config ) {
    log = Logger.getLogger getClass()
    this.config = config as Config
  }
  
  @TypeChecked( SKIP )
  void checkAuth( RoutingContext rc ) {
    if( config.skip && rc.normalizedPath().startsWith( config.skip ) ){
      rc.next()
      return
    }

    String authorization = rc.request().getHeader( 'authorization' )?.trim()
    
    if( !authorization?.startsWith( 'Bearer ' ) ){
      err rc, 401
      return
    }
    
    checkJwt( authorization ){ AsyncResult<VUser> ar ->
      if( ar.failed() ){
        log.warn "checkAuth ${rc.request().method()} : ${rc.normalizedPath()} FAIL -> ${ar.cause()}"
        checkCookie rc
        return
      }
      log.info "checkAuth ${rc.request().method()} : ${rc.normalizedPath()} -> OK"
      
      rc.user = ar.result()
      Map<String,Object> principal = rc.user().principal().map
      
      // refresh still valid token
      if( config.refreshTreshold > principal.validTil - System.currentTimeSeconds() )
        addAuthHeader rc, principal.id, principal.permissions
        
      rc.next()
    }
  }

  abstract String createJwt( Map payload, Map options )
    
  abstract void checkJwt( String authorization, Handler<AsyncResult<VUser>> handler )

  boolean checkpw( String plaintext, String hashed ) {
    BCrypt.checkpw plaintext, hashed
  }
  
  Cookie remembermeCookie( User u ) {
    String cookieToken = createJwt( [ signature:tokenSignature( u.email, u.password ), email:u.email ], [ expiresInMinutes:config.cookieExpires, permissions:u.permissions ] )
    Cookie cookie = Cookie.cookie config.cookieName, cookieToken
    cookie.path = '/'
    cookie.maxAge = config.cookieExpires * 60
    cookie.sameSite = CookieSameSite.LAX
    cookie
  }
  
  @TypeChecked( SKIP )
  void checkCookie( RoutingContext rc ) {
    Cookie c = rc.request().getCookie config.cookieName
    if( !c?.value ){
      err rc, 401
      return
    }
    
    checkJwt( c.value ){ AsyncResult<VUser> res ->
      log.info "$c.value -> $res"
      if( res.failed() ){
        err rc, 401
        return
      }
      
      Map data = res.result().principal().map
      log.info "cookie = $data"
        
      byte[] actual = data.signature.decodeBase64()
      
      User.withTransaction{
        User u = User.findByEmail data.email
        byte[] expected
        if( u ) expected = tokenSignature( u.email, u.password ).decodeBase64()
        if( equal( actual, expected ) ){
          addAuthHeader rc, u.id, u.permissions()
          rc.user = res.result()
          rc.next()
        }else
          err rc, 401
      }
    }
  }
      
  User extractUser( String authorization ) {
    CompletableFuture<User> fut = new CompletableFuture<>()
    checkJwt( authorization ){ AsyncResult<VUser> ar ->
      if( ar.succeeded() )
        fut.complete new User( id:ar.result().principal().getLong( 'id' ), permissions:(List<String>)ar.result().principal().map.permissions )
      else
        fut.completeExceptionally ar.cause()
    }
    try{
      fut.get 2, TimeUnit.SECONDS
    }catch( Exception e ){
      log.warn "decodeJwt failed: $e"
      null
    }
  }
    
  String addAuthHeader( RoutingContext rc, long id, List<Permission> permissions ) {
    String token = 'Bearer ' + generateJWT( id, permissions )
    rc.response().headers().add 'authorization', token
    token
  }
  
  String generateJWT( long id, List<Permission> permissions, int duration = 0 ) {
    int d = duration ?: config.sessionDuration
    createJwt( [ id:id, validTil:System.currentTimeSeconds() + d ], [ expires:d, permissions:permissions*.toString() ] )
  }
  
  boolean equal( byte[] actual, byte[] expected ) {
    expected && actual && MessageDigest.isEqual( actual, expected )
  }
  
  String tokenSignature( String... parts ) {
    try{
      MessageDigest digest = MessageDigest.getInstance 'MD5'
      digest.digest( parts.join( '.' ).bytes ).encodeHex().toString().bytes.encodeBase64().toString()
    }catch( NoSuchAlgorithmException e ){
      throw new IllegalStateException( 'No MD5 algorithm available!' )
    }
  }
  
  private static final String CHARS = [ 'a'..'z', 'A'..'Z', 0..9 ].flatten().join ''
  
  private static final String SPECIALS = '!?%#&@-=_./'
  
  private String generatePassword() {
    Random rand = new Random( System.currentTimeMillis() )
    List pw = (0..8).collect{ CHARS[ rand.nextInt( CHARS.size() ) ] }
    pw += (0..1).collect{ SPECIALS[ rand.nextInt( SPECIALS.size() ) ] }
    pw.shuffled().join ''
  }
}
