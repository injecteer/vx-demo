import React from "react"
import EventBus from '@vertx/eventbus-bridge-client.js'
import cogoToast from "cogo-toast"
import { getAuthorisation } from "./auth/Authorization"
import axios from "axios"

export default class EventBusBridge {
  
  static eventBus = null

  static connect( onConnect, handlers, onClose ) {
    if( this.eventBus ) return
    
    this.eventBus = new EventBus( axios.defaults.baseURL + '/eventbus', { vertxbus_ping_interval:30000 } )
    
    this.eventBus.onopen = _ => {
      onConnect()
      Object.entries( handlers ).forEach( ([ address, func ]) => this.eventBus.registerHandler( address, { authorization:getAuthorisation() }, func ) )
    }
    
    this.eventBus.onerror = err => {
      console.info( 'onerror', err )
      cogoToast.error( err.message )
      this.eventBus?.close()
      this.eventBus = null
    }
    
    this.eventBus.onclose = _ => {
      onClose()
      this.eventBus = null
    }
  }

  static isConnected = _ => !!this.eventBus

  static close() {
    this.eventBus?.close()
    this.eventBus = null
  }
}