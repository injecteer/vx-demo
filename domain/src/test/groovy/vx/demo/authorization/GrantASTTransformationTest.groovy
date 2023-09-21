package vx.demo.authorization

import org.junit.jupiter.api.Test

import groovy.test.GroovyTestCase

class GrantASTTransformationTest extends GroovyTestCase {

  final String HEAD = '''
    import io.vertx.core.*
    import io.vertx.core.json.JsonObject
    import io.vertx.ext.web.RoutingContext
    import io.vertx.ext.auth.User
    import io.vertx.core.http.HttpServerResponse
    import groovy.transform.*
    import vx.demo.authorization.*
    import static vx.demo.domain2.Permission.*

    @AutoImplement
    class U implements User {
      JsonObject principal(){ [ id:'abv123', permissions:[ 'kunde' ] ] as JsonObject }
    }

    @AutoImplement
    class RC implements RoutingContext {
      Integer accepted = 0, rejected = 0

      HttpServerResponse response() { null }
      
      User user() { new U() }
    }
  
    RoutingContext rc = new RC()
    GrantUtil.onOk = {-> rc.accepted++ }
    GrantUtil.onFail = {-> rc.rejected++ }
  '''
  
  @Test
  void '@Grant on a method should work'() {
    assertScript """
    $HEAD

    class A {
      @Grant( [ kunde ] )
      void kunde( RoutingContext rc ) {
      }

      @Grant( [ admin ] )
      void admin( RoutingContext rc ) {
      }
    }

    A a = new A()
    a.kunde rc
    a.admin rc
    
    assert 1 == rc.accepted
    assert 1 == rc.rejected
    """
  }
  
  @Test
  void '@Grant on a class should work'() {
    assertScript """
    $HEAD

    @Grant( [ kunde ] )
    class Usr {
      void kunde( RoutingContext rc ) {
      }
    }

    @Grant( [ admin ] )
    class Adm {
      void admin( RoutingContext rc ) {
      }
    }
    
    new Usr().kunde rc
    new Adm().admin rc
    
    assert 1 == rc.accepted
    assert 1 == rc.rejected
    """
  }
  
  @Test
  void '@Grant on super methods should work'() {
    assertScript """
    $HEAD

    class Supr {
      String out
      
      void kunde( RoutingContext rc ) {
        out = 'I am SUPER!'
      }
    }
    
    @Grant( [ kunde ] )
    class Chld extends Supr {
      void kunde1( RoutingContext rc ) {
        out = 'I am CHILD!'
      }
    }
    
    def chld = new Chld()
    assert !chld.out

    chld.kunde rc
    
    assert 'I am SUPER!' == chld.out
    assert 1 == rc.accepted

    chld.kunde1 rc

    assert 'I am CHILD!' == chld.out
    assert 2 == rc.accepted
    """
  }
  
  @Test
  void '@Grant on a class and method exclusively should work'() {
    assertScript """
    $HEAD
    
    @Grant( [ kunde ] )
    class Usr {
      void kunde( RoutingContext rc ) {
      }

      void kunde2( RoutingContext rc ) {
      }

      @Grant( [ admin ] )
      void admin( RoutingContext rc ) {
      }
    }
    
    Usr u = new Usr()
    u.kunde rc
    u.kunde2 rc
    assert 2 == rc.accepted
    assert 0 == rc.rejected

    u.admin rc
    assert 1 == rc.rejected
    """
  }
  
  @Test
  void 'repeating augmentation shall not happen'() {
    assertScript """
    $HEAD
    
    @Grant( [ kunde ] )
    class Usr {
      void kunde( RoutingContext rc ) {
      }
      
      void kunde2( RoutingContext rc ) {
      }
      
      @Grant( [ kunde ] )
      void kunde3( RoutingContext rc ) {
      }
    }
    
    Usr u = new Usr()
    u.kunde rc
    u.kunde2 rc
    u.kunde3 rc
    assert 3 == rc.accepted
    assert 0 == rc.rejected
    """
  }
  
  @Test
  void '@Grant( skip = true ) should ignore method'() {
    assertScript """
    $HEAD
    
    @Grant( [ kunde ] )
    class Usr {
      void kunde( RoutingContext rc ) {
      }
      
      @Grant( skip = true )
      void publ( RoutingContext rc ) {
      }
    }
    
    Usr u = new Usr()
    u.kunde rc
    u.publ rc
    assert 1 == rc.accepted
    assert 0 == rc.rejected
    """
  }
}
