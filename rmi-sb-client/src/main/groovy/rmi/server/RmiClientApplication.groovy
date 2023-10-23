package rmi.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean

import groovy.transform.CompileStatic

@SuppressWarnings("deprecation")
@SpringBootApplication
@CompileStatic
class RmiClientApplication {

  @Bean
  HttpInvokerProxyFactoryBean abrechnungMgr() {
    new HttpInvokerProxyFactoryBean( serviceUrl:'http://localhost:8070/abrechnungMgr', serviceInterface:AbrechnungMgrRemote )
  }

  static void main( String[] args ) {
    SpringApplication.run RmiClientApplication, args
  }
}
