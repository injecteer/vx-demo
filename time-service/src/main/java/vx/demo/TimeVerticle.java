package vx.demo;

import static io.vertx.core.json.JsonObject.mapFrom;
import static vx.demo.gorm.JavaGORMHelper.findAllBy;
import static vx.demo.gorm.JavaGORMHelper.withTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.healthchecks.Status;
import vx.demo.domain.LogEvent;
import vx.demo.gorm.StatisticsService;
import vx.demo.web.HealthCheckOnly;
import vx.demo.web.WebVerticle;

@HealthCheckOnly(8092)
public class TimeVerticle extends WebVerticle implements Handler<Message<String>> {

  private static final Map<String, Object> PARAMS = Map.of("sort", "id", "order", "desc", "max", 1);

  private final String FORMAT = "dd.MM.yyy EEE HH:mm";

  private final List<String> IDS = List.of(TimeZone.getAvailableIDs());

  private StatisticsService statisticsService;

  @Override
  public void start() throws Exception {
    super.start();

    statisticsService = new StatisticsService("time");

    vertx.eventBus().consumer("time", this);
  }

  @Override
  public void handle(Message<String> msg) {
    String timezone = msg.body().trim();
    log.info("<< [" + timezone + "] -> " + IDS.contains(timezone) + " >> " + msg.replyAddress());

    withTransaction(LogEvent.class, tx -> {
      LogEvent le = new LogEvent("time");

      if (IDS.contains(timezone)) {
        var sdf = new SimpleDateFormat(FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        le.setSuccess(true);
        msg.reply(sdf.format(new Date()));
      } else {
        le.setSuccess(false);
        msg.fail(404, "invalid time zone!");
      }

      le.save();
    });
  }

  @Override
  protected Map<String, Handler<Promise<Status>>> healthChecks() {
    return Map.of(getClass().getSimpleName(), p -> {
      withTransaction(LogEvent.class, tx -> {
        
        List<LogEvent> lastSuccesses = findAllBy(LogEvent.class, "SuccessAndWhat", PARAMS, true, "time");
        var lastSuccess = lastSuccesses.isEmpty() ? null : lastSuccesses.iterator().next();
        
        List<LogEvent> lastFailures = findAllBy(LogEvent.class, "SuccessAndWhatNotEqual", PARAMS, false, "weather");
        var lastFailure = lastFailures.isEmpty() ? null : lastFailures.iterator().next();
        
        Status status = statisticsService.collectStats();
        status.getData().put("lastSuccess", mapFrom(lastSuccess));
        status.getData().put("lastFailure", mapFrom(lastFailure));
        
        p.complete(status);
      });
    });
  }
}
