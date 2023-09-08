package vx.demo

import groovy.transform.TypeChecked
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import vx.demo.web.HealthCheckOnly
import vx.demo.web.WebVerticle

@TypeChecked
@HealthCheckOnly( 8093 )
class WeatherVerticle extends WebVerticle {

  final int HEALTH = 8093

  final String APPID = '4667a270d3e65836b5b91ee70b628c9a'

  WebClient http

  @Override
  void start() throws Exception {
    super.start()

    http = WebClient.create vertx
    
    vertx.eventBus().consumer( 'weather' ){ Message<JsonObject> msg ->
      log.info "<< ${msg.body()}"
      http.getAbs( 'https://api.openweathermap.org/data/2.5/weather' )
        .addQueryParam( 'appid', APPID )
        .addQueryParam( 'q', msg.body().getString( 'city' ) )
        .send{
          if( it.succeeded() )
            msg.reply it.result().bodyAsJsonObject()
          else
            msg.fail 400, it.cause().message
        }
    }
  }
}
