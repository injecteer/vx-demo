package vx.demo;

import static io.vertx.core.json.JsonObject.mapFrom;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.healthchecks.Status;
import vx.demo.web.HealthCheckOnly;
import vx.demo.web.WebVerticle;

@HealthCheckOnly( 8092 )
public class TimeVerticle extends WebVerticle implements Handler<Message<String>> {

  private final String FORMAT = "dd.MM.yyy EEE HH:mm";
  
  private final Stats stats = new Stats();
  
  private final List<String> IDS = List.of( TimeZone.getAvailableIDs() );

  @Override
  public void start() throws Exception {
    super.start();
    vertx.eventBus().consumer( "time", this );
  }
  
  @Override
  public void handle( Message<String> msg ) {
    String timezone = msg.body().trim();
    log.info( "<< [" + timezone + "] -> " + IDS.contains( timezone ) + " >> " + msg.replyAddress() );
    
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
  
  @Override
  protected Map<String, Handler<Promise<Status>>> healthChecks() {
    return Map.of( getClass().getSimpleName(), p -> p.complete( Status.OK( mapFrom( stats ) ) ) );
  }
  
  static class Stats {
    public long oks = 0l;
    public long errs = 0l;
    
    void ok() { oks++; }
    void err() { errs++; }
  }
}
