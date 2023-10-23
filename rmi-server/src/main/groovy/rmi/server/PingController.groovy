package rmi.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import groovy.transform.CompileStatic

@RestController
@CompileStatic
class PingController {

  @GetMapping( '/ping' )
  String ping() {
    'pong'
  }
}
