package vx.demo.backoffice.controller

import static groovy.transform.TypeCheckingMode.SKIP

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Component

import grails.gorm.transactions.Transactional
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.authentication.TokenCredentials
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import vx.demo.domain2.Permission
import vx.demo.domain2.User
import vx.demo.web.Binder
import vx.demo.web.SecurityControllerBase

@Component
class SecurityController extends SecurityControllerBase {

  final Binder binder = new Binder( User )
  
  JWTAuth jwtAuth
  
  final boolean registerWithAdmin = System.getProperty 'registerWithAdmin', 'false' toBoolean()
  
  private final Binder binder = new Binder( User )
  
  @Autowired
  SecurityController( Vertx vertx, Router router, Map cfg, MessageSource messageSource ) {
    super( cfg.security, messageSource )

    config.skip = '/api/pub/'
    
    log.info "registerWithAdmin = $registerWithAdmin"
    
    JWTAuthOptions opts = new JWTAuthOptions( pubSecKeys:[ new PubSecKeyOptions( config.pubSecKeys ) ] )
    jwtAuth = JWTAuth.create vertx, opts

    router.route '/*' order -100 handler this.&checkAuth
    router.patch '/pub/check/:field' consumes JSON produces JSON blockingHandler this::validateField
    router.post '/pub/register' consumes JSON produces JSON blockingHandler this::register
    router.post '/pub/login' consumes JSON produces JSON handler this::login
    router.post '/pub/forgotPassword' consumes JSON produces JSON handler this::forgotPassword
  }
  
  @Transactional
  void register( RoutingContext rc ) {
    Map params = params rc
    User u = new User()
    if( registerWithAdmin ) params.permissionMask = Permission.all()
    binder.bind u, params
    
    if( u.save( flush:true ) ){
      String token = addAuthHeader rc, u.id, u.permissions()
      ok rc, [ user:u, authorization:token ]
    }else{
      Map errors = errors2messagesMap u
      err rc, [ errors:errors ], 400
    }
  }
  
  @TypeChecked( SKIP )
  @Transactional
  void login( RoutingContext rc ) {
    Map params = params rc
    User u = User.findByEmail params.email?.toLowerCase()
    boolean success = u && checkpw( params.password, u.password )
    log.info "login ${u?.email} / $success"
    
    if( success ){
      String authorization = addAuthHeader rc, u.id, u.permissions()
      if( params.rememberMe?.toBoolean() ) rc.response().addCookie remembermeCookie( u )
      ok rc, [ user:u, authorization:authorization ]
    }else
      noAuth rc, 'bad.credentials'
  }
  
  @TypeChecked( SKIP )
  @Transactional
  void forgotPassword( RoutingContext rc ) {
    String email = params rc email
    
    User u = User.findByEmail email?.toLowerCase()
    
    if( !u ){
      notFound rc
      return
    }
    
    String pw = generatePassword()
    log.info "reset pw for $email -> $pw"
    
    u.forcePasswordReset = true
    u.password = pw
    u.save deepValidate:false, flush:true
    
    ok rc
  }
  
  @Override
  String createJwt( Map payload, Map options ) {
    jwtAuth.generateToken payload as JsonObject, options as JWTOptions
  }
  
  @Override
  void checkJwt( String authorization, Handler<AsyncResult<User>> handler ) {
    jwtAuth.authenticate( new TokenCredentials( authorization?.substring( 7 ) ?: 'TOTALLY INVALID' ), handler )
  }
  
  @Transactional
  void validateField( RoutingContext rc ) {
    def params = params rc
    User u = new User()
    binder.bind u, params

    if( u.validate( [ params.field ] ) )
      ok rc
    else
      err rc, errors2messagesMap( u )[ params.field ], 400
  }
}
