package vx.demo;

import static io.vertx.core.json.JsonObject.mapFrom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import vx.demo.web.HealthCheckOnly;
import vx.demo.web.WebVerticle;

@HealthCheckOnly( 8092 )
public class TimeVerticle extends WebVerticle implements Handler<Message<String>> {

  private final String FORMAT = "dd.MM.yyy EEE HH:mm";
  
  private final Stats stats = new Stats();
  
  private final List<String> IDS = List.of( TimeZone.getAvailableIDs() );

  @Override
  public void start( Promise<Void> startPromise ) throws Exception {
    start();
    
    vertx.eventBus().consumer( "time", this );
    
    startPromise.complete();
  }
  
  @Override
  public void healthChecks() {
    HealthCheckHandler hc = HealthCheckHandler.create( vertx ).register( "health", p -> p.complete( Status.OK( mapFrom( stats ) ) ) );
    router.get( "/health" ).handler( hc );
  }
  
  @Override
  public void handle( Message<String> msg ) {
    String timezone = msg.body().trim();
    log.info( "<< [" + timezone + "]" );
    
    if( IDS.contains( timezone ) ){
      stats.ok();
      var sdf = new SimpleDateFormat( FORMAT );
      sdf.setTimeZone( TimeZone.getTimeZone( timezone ) );
      msg.reply( sdf.format( new Date() ) );
    }else{
      stats.err();
      msg.fail( 404, "invalid time zone!" );
    }
  }
  
  static class Stats {
    long oks = 0l;
    long errs = 0l;
    
    void ok() { oks++; }
    void err() { errs++; }
  }
}
