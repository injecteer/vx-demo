package rmi.server

import static java.lang.reflect.Modifier.*

import org.apache.commons.lang3.RandomStringUtils

import groovy.transform.CompileStatic

@CompileStatic
class RemoteHelper {

  static final long YEAR_MS = 31_536_000_000 
  
  static <T extends Serializable> T generate( Class<T> clazz ) {
    clazz.metaClass.properties.inject( clazz.newInstance() ){ T o, MetaProperty p ->
      switch( p.type ){
        case { isStatic( p.modifiers ) || isFinal( p.modifiers ) }: return o
        case String: o[ p.name ] = RandomStringUtils.randomAlphabetic 10; break
        case int: o[ p.name ] = RandomStringUtils.randomNumeric( 5 ).toInteger(); break
        case long: o[ p.name ] = RandomStringUtils.randomNumeric( 10 ).toLong(); break
        case boolean: o[ p.name ] = Math.random() > .5; break
        case Date: o[ p.name ] = new Date( System.currentTimeMillis() - (long)(Math.random() * YEAR_MS) ); break
        case Serializable: o[ p.name ] = generate( p.type ); break
      }
      o
    }
  }

}