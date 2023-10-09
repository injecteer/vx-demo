import { createContext, useContext, useEffect, useState } from 'react'
import { useHistory, useLocation } from "react-router-dom"
import { AuthContext } from '../auth/Authorization'
import EventBusBridge from './EventBusBridge'
import cogoToast from 'cogo-toast'

export const EventBusContext = createContext()

export const EventBusProvider = ({ children }) => {

  const { user } = useContext( AuthContext )

  const [ status, setStatus ] = useState( false )
  const [ indicator, setIndicator ] = useState( { newId:null, blink:null, reset:false } )
  
  let location = useLocation().pathname
  const unlisten = useHistory().listen( loc => {
    location = loc.pathname
    if( '/logEvents' === location ) setIndicator( { reset:true } )
  } )

  useEffect( _ => {
    if( user )
      EventBusBridge.connect(
        _ => setStatus( true ), 
        {
          'weather.called':( error, msg ) => {
            const { id } = msg.body
            cogoToast.warn( <><b>weather.called</b> -&gt; New Id <b>{id}</b></> )
            
          },
          ['user.' + user.id]:( error, msg ) => {
            const { id } = msg.body
            cogoToast.info( <><b>user.{user.id}</b> -&gt; New Id <b>{id}</b></> )
            setIndicator( { newId:id, blink:'/logEvents' === location ? null : id } )
          }
        }, 
        _ => setStatus( false )
      )
    else
      EventBusBridge.close()

    return _ => {
      EventBusBridge.close()
      unlisten()
    }
  }, [ user ] )

  return <EventBusContext.Provider value={ { status, setStatus, indicator, setIndicator }}>
    {children}
  </EventBusContext.Provider>
}