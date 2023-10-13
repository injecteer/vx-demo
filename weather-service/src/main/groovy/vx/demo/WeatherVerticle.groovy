package vx.demo

import static io.vertx.core.json.JsonObject.mapFrom

import grails.gorm.transactions.Transactional
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.healthchecks.Status
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import vx.demo.domain.LogEvent
import vx.demo.gorm.StatisticsService
import vx.demo.web.HealthCheckOnly
import vx.demo.web.WebVerticle

@TypeChecked
@HealthCheckOnly( 8093 )
class WeatherVerticle extends WebVerticle {

  final String APPID = '4667a270d3e65836b5b91ee70b628c9a'

  private WebClient http
  
  private StatisticsService statisticsService
  
  @Override
  void start() throws Exception {
    super.start()
    
    statisticsService = new StatisticsService( 'weather' )
    
    http = WebClient.create vertx, [ connectTimeout:3000 ] as WebClientOptions
    
    vertx.eventBus().consumer( 'weather' ){ Message<JsonObject> msg ->
      log.info "<< ${msg.body()}"
      http.getAbs( 'https://api.openweathermap.org/data/2.5/weather' )
        .addQueryParam( 'appid', APPID )
        .addQueryParam( 'q', msg.body().getString( 'city' ) )
        .send this.&onSend.curry( msg )
    }
  }
  
  @Transactional
  void onSend( Message<JsonObject> msg, AsyncResult<HttpResponse<Buffer>> ar ) {
    if( ar.succeeded() )
      msg.reply ar.result().bodyAsJsonObject()
    else
      msg.fail 400, ar.cause().message
      
    var le = new LogEvent( 'weather', ar.succeeded() ).save()
    
    vertx.eventBus().publish 'weather.called', mapFrom( type:'LogEvent', id:le.id )
  }
  
  @Override
  Map<String, Handler<Promise<Status>>> healthChecks() {
    [ (getClass().simpleName):{ it.complete statisticsService.collectStats() } as Handler<Promise<Status>> ]
  }
}
