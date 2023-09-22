package vx.demo.web


import static java.lang.reflect.Modifier.*

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

import org.grails.datastore.gorm.GormEntity

import com.fasterxml.jackson.annotation.JsonFormat

import groovy.util.logging.Log4j
import io.vertx.ext.web.RoutingContext

@Log4j
class Binder {
  
  static final String DATE_FMT = 'yyyy-MM-dd'
  
  // property name -> ( property class, collection type )
  final Map<String,Tuple2<Class,Class>> classProps
  
  @SafeVarargs
  Binder( Class<?>... classes ) {
    classProps = classes.inject( [:] ){ Map<String,Tuple2<Class,Class>> res, Class<?> clazz ->
      res << clazz.declaredFields.collectEntries{ Field fld ->
        if( isFinal( fld.modifiers ) || isStatic( fld.modifiers ) || !( fld.name ==~ /^(?!.*(_gorm_|metaClass|lastUpdated|dateCreated|\$)).*$/ ) )
          return Collections.emptyMap()
        
        Class elemType
        if( fld.genericType instanceof ParameterizedType && 1 == fld.genericType.actualTypeArguments.size() )
          elemType = fld.genericType.actualTypeArguments.first()
        [ fld.name.replaceFirst( /.*__(\w+)/, '$1' ), new Tuple2( fld.type, elemType ) ]
      }
    }.asImmutable()
  }
  
  void bind( o, RoutingContext rc, boolean bindIdOnly = false ) {
    bind o, rc.body().asJsonObject().map, bindIdOnly
  }
    
  void bind( o, Map data, boolean bindIdOnly = false ) {
    log.info "$o << $data"
    data.each{ k, v ->
      if( 'id' == k ) return
      Tuple2<Class,Class> t = classProps[ k ]
      if( !t ) return
      
      if( t.v1.isCase( List ) ) o[ k ] = null
      o[ k ] = null == v ? v : bindSingle( t.v1, t.v2, v, bindIdOnly )
      log.info "$k : $v : ${t.v1.simpleName} -> ${o[ k ]}"
    }
  }

  protected bindSingle( Class<?> clazz, Class<?> listElemClass, value, boolean bindIdOnly ) {
    if( '' == value && String != clazz ) return null
    
    switch( clazz ){
      case GormEntity:
        switch( value ){
          case Map:
            def o = clazz.declaredConstructors.first().newInstance()
            bind o, value
            return o
          case String:
            return bindIdOnly ? getIdObject( clazz, value ) : clazz.get( value )
          default:
            return null
        }
      case Enum:
        return clazz.valueOf( value )
      case boolean:
      case Boolean:
      case Integer:
      case float:
      case Float:
      case long:
      case Long:
      case Double:
      case double:
        return value."to${clazz.simpleName.capitalize()}"()
      case Date:
        return value in Number ? new Date( value ) : Date.parse( DATE_FMT, value )
      case List:
        return value.collect{ bindSingle listElemClass, null, it, bindIdOnly }
      default:
        return value
    }
  }
  
  private getIdObject( Class<? extends GormEntity> clazz, String id ) {
    def idObj = clazz.declaredConstructors.first().newInstance()
    idObj.id = id
    idObj
  }
  
}
