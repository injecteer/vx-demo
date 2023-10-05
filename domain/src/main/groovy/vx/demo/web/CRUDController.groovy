package vx.demo.web

import org.apache.log4j.Logger
import org.grails.datastore.gorm.GormEntity
import org.springframework.context.MessageSource

import grails.gorm.transactions.Transactional
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

abstract class CRUDController<T extends GormEntity> implements Controller {
  
  final Class<T> clazz
  
  final Binder binder
  
  protected Logger log
  
  @SafeVarargs
  CRUDController( Router router, MessageSource messageSource, Class<?>... classes ) {
    log = Logger.getLogger getClass()
    
    this.messageSource = messageSource
    
    clazz = classes[ 0 ]
    
    binder = new Binder( classes )
    
    String name = clazz.simpleName.uncapitalize()
    
    registerMappings router, name, pluralize( name )
  }
  
  void registerMappings( Router router, String name, String namePlural ) {
    if( !router ) return
    router.post "/$namePlural" produces JSON handler this::list
    router.get "/$name/:id/:props" produces JSON handler this.&details
    router.get "/$name/:id/" produces JSON handler this.&details
    router.get "/$name/:id" produces JSON handler this.&details
    router.post "/$name/:id" consumes JSON produces JSON handler this.&save
    router.post "/$name" consumes JSON produces JSON handler this.&save
    router.delete "/$name/:id" handler this.&delete
  }
  
  @Transactional
  void list( RoutingContext rc ) {
    Map params = params rc
    
    def q = query rc, params
    
    if( null == q ){
      err rc, 400
      return
    }
    
    int offset = params.offset?.toInteger() ?: 0
    int max = params.max?.toInteger() ?: 20
    def list = clazz.list( offset:offset, max:max )
    ok rc, [ list:list, count:clazz.count() ]
  }

  protected Map query( RoutingContext rc, Map params ) {
    Collections.emptyMap()
  }
  
  @Transactional
  void details( RoutingContext rc ) {
    T o = clazz.read rc.pathParam( 'id' )
    if( o ){
      String props = rc.pathParam( 'props' )?.trim()
      ok rc, props ? props.split( ',' ).collectEntries{ it && o.hasProperty( it ) ? [ it, o[ it ] ] : Collections.emptyMap() } : o
    }else
      notFound rc
  }
  
  @Transactional
  void delete( RoutingContext rc ) {
    String id = rc.pathParam 'id'
    T o = id ? clazz.get( id ) : null
    if( o?.id ){
      log.info "deleting $o"
      o.delete flush:true
      ok rc
    }else
      notFound rc
  }
  
  @Transactional
  void save( RoutingContext rc ) {
    try{
      Map params = params rc
      T o = params.id ? clazz.get( params.id ) : clazz.newInstance()
      bind o, params
      if( save( o, rc ) ) ok rc, o
    }catch( e ){
      log.error 'save', e
      err rc, 400
    }
  }
  
  void bind( o, Map data, boolean bindIdOnly = false ) {
    binder.bind o, data, bindIdOnly
  }
  
  /**
   * Tries to save an instance of <code>T</code>. In case of success returns <code>true</code>, or sends a code 400 back otherwise.
   * 
   * @param o instance of <code>T</code> to save
   * @param rc RoutingContext
   * @param params save parameters
   * @return
   */
  @Transactional
  boolean save( T o, RoutingContext rc, Map params = [ deepValidate:false, flush:true ] ) {
    T saved = o.save params
    if( saved ){
      log.info "saved $saved"
      true
    }else{
      List errors = errors2messages o
      log.warn "save failed for $o: $errors"
      err rc, [ errors:errors ], 400
      false
    }
  }
  
  private String pluralize( String single ) {
    "$single${single =~ /^.+(sh|ch|x|s|z|zh)$/ ? 'e' : ''}s"
  }
  
}
