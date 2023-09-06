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
import io.vertx.ext.web.Router;
import vx.demo.web.WebVerticle;

public class TimeVerticle extends WebVerticle implements Handler<Message<String>> {

  private final int HEALTH = 8092;

  private final String FORMAT = "dd.MM.yyy EEE HH:mm";
  
  private final Stats stats = new Stats();
  
  private final List<String> IDS = List.of( TimeZone.getAvailableIDs() );

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void start( Promise startPromise ) throws Exception {
    start();
    
    vertx.eventBus().consumer( "time", this );
    
    Router router = Router.router( vertx );
    HealthCheckHandler hc = HealthCheckHandler.create( vertx ).register( "health", p -> p.complete( Status.OK( mapFrom( stats ) ) ) );
    router.get( "/health" ).handler( hc );
    vertx.createHttpServer().requestHandler( router ).listen( HEALTH, startPromise );
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
