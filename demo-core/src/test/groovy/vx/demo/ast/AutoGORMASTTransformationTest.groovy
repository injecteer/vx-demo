package vx.demo.ast

import org.junit.jupiter.api.Test

import groovy.test.GroovyTestCase

class AutoGORMASTTransformationTest extends GroovyTestCase {

  @Test
  void '@AutoGORMInitializer should collect @Entity classes'() {
    assertScript """
    import grails.gorm.annotation.Entity
    import vx.demo.ast.AutoGORMInitializer

    @Entity class A {}

    @Entity class B {}

    class Ignored {}

    @AutoGORMInitializer class _Initializer {}

    def initializer = new _Initializer()
    
    assert initializer.domainClasses.size() == 2
    assert initializer.domainClasses == [ 'A', 'B' ]
    """
  }
}
