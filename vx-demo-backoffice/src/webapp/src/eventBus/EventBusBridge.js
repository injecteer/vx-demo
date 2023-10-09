import EventBus from '@vertx/eventbus-bridge-client.js'
import { getAuthorization } from "../auth/Authorization"

export default class EventBusBridge {
  
  static eventBus = null

  static connected = false

  static connect( onConnect, handlers, onClose ) {
    this.close()

    this.eventBus = new EventBus( process.env.REACT_APP_SERVER + '/eventbus', { vertxbus_reconnect_delay_min:30000, vertxbus_ping_interval:60000 } )
    
    this.eventBus.enableReconnect( true )

    this.eventBus.onopen = _ => {
      onConnect()
      console.info( 'openned EB' )
      Object.entries( handlers ).forEach( ([ address, func ]) => this.eventBus?.registerHandler( address, { authorization:getAuthorization() }, func ) )
    }
    
    this.eventBus.onerror = err => console.info( 'onerror', err )
    
    this.eventBus.onclose = _ => {
      onClose()
      this.connected = false
    }

    return this.close
  }

  static isConnected = _ => this.connected

  static close() {
    this.connected = false
    this.eventBus?.close()
  }
}