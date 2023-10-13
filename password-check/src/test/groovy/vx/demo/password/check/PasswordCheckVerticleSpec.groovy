package vx.demo.password.check

import groovy.yaml.YamlSlurper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import vx.demo.domain2.User
import vx.demo.gorm.Bootstrap

@Unroll
class PasswordCheckVerticleSpec extends Specification {

  @Shared PasswordCheckVerticle pcv = new PasswordCheckVerticle()
  
  def setupSpec() {
    Bootstrap.init( [:] )
  }
  
  def 'error in [#password] for #userCount users should be -#error-'() {
    setup:
    User.metaClass.static.count = {-> userCount }
    
    expect:
    pcv.getError( password ) == error
    
    where:
    password  | userCount |     error
    null      |   0       | 'password.blank'
    ''        |   0       | 'password.blank'
    '12'      |   0       | 'password.too.short'
    '123'     |   0       | 'password.wrong.content'
    '123'     |   10      | 'password.too.short'
    '1234'    |   10      | 'password.wrong.content'
    '1234'    |   100     | 'password.too.short'
    '12345'   |   100     | 'password.wrong.content'
    '123a'    |   0       | 'password.wrong.content'
    '123aA'   |   0       | null
  }

}
