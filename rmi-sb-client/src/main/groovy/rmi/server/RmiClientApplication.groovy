package rmi.server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter

import groovy.transform.CompileStatic

@SuppressWarnings("deprecation")
@SpringBootApplication
@CompileStatic
class RmiClientApplication {

  @Bean
  HttpInvokerProxyFactoryBean abrechnungMgr() {
    HttpInvokerProxyFactoryBean invoker = new HttpInvokerProxyFactoryBean()
    invoker.serviceUrl = 'http://localhost:8070/abrechnungMgr'
    invoker.serviceInterface = AbrechnungMgrRemote
    invoker
  }

  static void main( String[] args ) {
    SpringApplication.run RmiClientApplication, args
  }
}
