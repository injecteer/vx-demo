import { useContext, useEffect, useState } from 'react'
import { EventBusContext } from './EventBusProvider'
import { Link } from "react-router-dom"
import { MdNotificationsNone, MdOutlineNotificationsOff } from 'react-icons/md'

export default _ => {
  const { status, indicator } = useContext( EventBusContext )

  const [ className, setClassName ] = useState( '' )
  
  useEffect( _ => {
    console.info( 'StatusInd useEffect', indicator )
    if( indicator.newId ){
      setClassName( 'scaleUpDown' )
      setTimeout( _ => setClassName( '' ), 1100 )
    }
  }, [ indicator ] )

  return <div className={'pointer uk-margin-right ' + className}>
    <Link to="/logEvents" onClick={_ => setCount( 0 )}>
      {status ? <MdNotificationsNone size="2.4em" color="black"/> : <MdOutlineNotificationsOff size="2.4em" color="gray"/>}
      {status && !!indicator.count && <span className="uk-badge" style={{ marginLeft:'-1.6em' }}>{20 > indicator.count ? indicator.count : '20+'}</span>}
    </Link>
  </div>
}