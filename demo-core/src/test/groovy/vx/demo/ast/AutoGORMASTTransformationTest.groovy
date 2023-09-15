package vx.demo.ast

import org.junit.jupiter.api.Test

import groovy.test.GroovyTestCase

class AutoGORMASTTransformationTest extends GroovyTestCase {

  @Test
  void '@AutoGORMInitializer should collect @Entity classes'() {
    assertScript """
    import grails.gorm.annotation.Entity
    import vx.demo.ast.AutoGORMInitializer

    @Entity class A {
      String a
    }

    @Entity class B {
      String b
    }

    class Ignored {}

    @AutoGORMInitializer
    class Initializer {
    }
    
    assert new Initializer().domainClasses.size() == 2
    """
  }
}
