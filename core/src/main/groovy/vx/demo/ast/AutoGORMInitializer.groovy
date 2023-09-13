package vx.demo.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

@Retention( RetentionPolicy.RUNTIME )
@Target( [ ElementType.TYPE ] )
@GroovyASTTransformationClass( classes = [ AutoLoadedASTTransformation ] )
/**
 *  in start-up
 */
@interface AutoGORMInitializer {
}
