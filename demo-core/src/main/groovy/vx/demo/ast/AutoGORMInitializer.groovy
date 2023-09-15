package vx.demo.ast

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 *  The annotated class gets a <code>List<String> domainClasses</code> Field, containing names of all classes, annotated with @{@link grails.gorm.annotation.Entity}.
 *  <br>
 *  The class names can be used to create a DataSource.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( [ ElementType.TYPE ] )
@GroovyASTTransformationClass( classes = [ AutoGORMASTTransformation ] )
@interface AutoGORMInitializer {
}
