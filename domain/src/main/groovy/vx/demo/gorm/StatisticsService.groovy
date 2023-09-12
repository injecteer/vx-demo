package vx.demo.gorm

import static io.vertx.core.json.JsonObject.mapFrom

import grails.gorm.transactions.Transactional
import groovy.transform.TupleConstructor
import io.vertx.ext.healthchecks.Status
import vx.demo.domain.LogEvent

@TupleConstructor
@Transactional
class StatisticsService {

  final String what
  
  Status collectStats() {
    List stats = LogEvent.executeQuery '''select CASE success WHEN true THEN 'ok' else 'err' END, count( id ) 
                                          from LogEvent where what=?0 group by success''', what
    Map payload = stats.collectEntries{ it }
    String status = payload.ok > payload.err ? 'OK' : 'KO'
    Status."$status" mapFrom( payload )
  }
  
}
