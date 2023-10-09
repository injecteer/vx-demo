import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { AuthContext } from '../auth/Authorization'
import EventBusBridge from './EventBusBridge'
import cogoToast from 'cogo-toast'

export const EventBusContext = createContext()

export const EventBusProvider = ({ children }) => {

  const { user } = useContext( AuthContext )

  const [ status, setStatus ] = useState( false )
  const [ indicator, setIndicator ] = useState( { newId:null, count:null } )
  
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
            setIndicator( old => ({ newId:id, count:( old.count ?? 0 ) + 1 }) )
          }
        }, 
        _ => setStatus( false )
      )
    else
      EventBusBridge.close()

    return EventBusBridge.close
  }, [ user ] )

  const setStatusCB = useCallback( setStatus, [] )
  const setIndicatorCB = useCallback( setIndicator, [] )

  const val = useMemo( _ => ({ status, setStatus:setStatusCB, indicator, setIndicator:setIndicatorCB }), 
                      [ status, setStatusCB, indicator, setIndicatorCB ] )

  return <EventBusContext.Provider value={val}>
    {children}
  </EventBusContext.Provider>
}