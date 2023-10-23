package rmi.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

@SuppressWarnings("deprecation")
@SpringBootApplication
public class RmiServerApplication {

  @Bean( name = "/abrechnungMgr" )
  HttpInvokerServiceExporter abrechnungMgr() {
    HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
    exporter.setService(new AbrechnungMgr());
    exporter.setServiceInterface(AbrechnungMgrRemote.class);
    return exporter;
  }

  public static void main( String[] args ) {
    SpringApplication.run( RmiServerApplication.class, args );
  }
}
