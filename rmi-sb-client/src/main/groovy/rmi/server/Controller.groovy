package rmi.server

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

import rmi.server.domain.ObjektSO

@RestController
class Controller {

  @Autowired
  AbrechnungMgrRemote abrechnungMgrRemote 
  
  @GetMapping( path = '/abrechnungMgr/{id}', produces = MediaType.APPLICATION_JSON_VALUE )
  ResponseEntity<ObjektSO> abrechnungMgr( @PathVariable Long id ) {
    new ResponseEntity<ObjektSO>( abrechnungMgrRemote.getObjektSO( id ), HttpStatus.OK ) 
  }
  
  @GetMapping( '/ping' )
  String ping() {
    'pong'
  }
  
}
