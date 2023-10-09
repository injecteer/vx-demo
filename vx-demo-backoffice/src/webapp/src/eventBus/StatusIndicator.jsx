import { useContext, useEffect, useState } from 'react'
import { EventBusContext } from './EventBusProvider'
import { Link } from "react-router-dom"
import { MdNotificationsNone, MdOutlineNotificationsOff } from 'react-icons/md'

export default _ => {
  const { status, indicator } = useContext( EventBusContext )

  const [ className, setClassName ] = useState( '' )
  const [ count, setCount ] = useState( null )
  
  useEffect( _ => {
    if( indicator.reset ){
      setCount( 0 )
    }else if( indicator.blink ){
      setCount( c => ( c ?? 0 ) + 1 )
      setClassName( 'scaleUpDown' )
      setTimeout( _ => setClassName( '' ), 1100 )
    }
  }, [ indicator ] )

  return <div className={'pointer uk-margin-right ' + className}>
    <Link to="/logEvents" onClick={_ => setCount( 0 )}>
      {status ? <MdNotificationsNone size="2.4em" color="black"/> : <MdOutlineNotificationsOff size="2.4em" color="gray"/>}
      {status && !!count && <span className="uk-badge" style={{ marginLeft:'-1.6em' }}>{100 > count ? count : '99+'}</span>}
    </Link>
  </div>
}