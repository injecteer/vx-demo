package vx.demo.authorization

import static java.lang.annotation.ElementType.METHOD
import static java.lang.annotation.ElementType.TYPE
import static java.lang.annotation.RetentionPolicy.RUNTIME

import java.lang.annotation.Retention
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import vx.demo.domain2.Permission

/**
 * Java - to make default {} possible
 * 
 * @author Konstantin Smirnov
 *
 */
@Retention( RUNTIME )
@Target( [ TYPE, METHOD ] )
@GroovyASTTransformationClass( classes = [ GrantASTTransformation ] )
@interface Grant {

  Permission[] value() default []
  
  boolean skip() default false
  
}
