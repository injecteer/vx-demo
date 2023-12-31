import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { AuthContext } from '../auth/Authorization'
import EventBusBridge from './EventBusBridge'
import cogoToast from 'cogo-toast'

export const EventBusContext = createContext()

export const EventBusProvider = ({ children }) => {

  const { user } = useContext( AuthContext )

  const [ status, setStatus ] = useState( false )
  const [ newIds, setNewIds ] = useState( [] )
  
  useEffect( _ => {
    if( user )
      EventBusBridge.connect(
        _ => setStatus( true ), 
        {
          'weather.called':( error, msg ) => {
            const { id } = msg.body
            cogoToast.warn( <><b>weather.called</b> ➔ New Id <b>{id}</b></> )
          },
          ['user.' + user.id]:( error, msg ) => {
            const { id } = msg.body
            cogoToast.info( <><b>user.{user.id}</b> ➔ New Id <b>{id}</b></> )
            setNewIds( old => [ ...old, id ] )
          }
        }, 
        _ => setStatus( false )
      )
    else
      EventBusBridge.close()

    return EventBusBridge.close
  }, [ user ] )

  const setNewIdsCB = useCallback( setNewIds, [] )

  const val = useMemo( _ => ({ status, newIds, setNewIds:setNewIdsCB }), [ status, newIds, setNewIdsCB ] )

  return <EventBusContext.Provider value={val}>{children}</EventBusContext.Provider>
}