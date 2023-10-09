import { useContext, useEffect, useState } from 'react'
import { EventBusContext } from './EventBusProvider'
import { Link } from "react-router-dom"
import { MdNotificationsNone, MdOutlineNotificationsOff } from 'react-icons/md'

export default () => {
  const { status, newIds } = useContext( EventBusContext )

  const [ className, setClassName ] = useState( '' )
  
  useEffect( _ => {
    if( newIds.length ){
      setClassName( 'scaleUpDown' )
      setTimeout( () => setClassName( '' ), 1100 )
    }
  }, [ newIds ] )

  return <div className={'pointer uk-margin-right ' + className}>
    <Link to="/logEvents" onClick={_ => setCount( 0 )}>
      {status ? <MdNotificationsNone size="2.4em" color="black"/> : <MdOutlineNotificationsOff size="2.4em" color="gray"/>}
      {status && !!newIds.length && <span className="uk-badge" style={{ marginLeft:'-1.6em' }}>{20 > newIds.length ? newIds.length : '20+'}</span>}
    </Link>
  </div>
}