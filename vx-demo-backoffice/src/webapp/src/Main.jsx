import React, { createRef, useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { Switch, Route, Redirect, withRouter } from 'react-router'
import axios from 'axios'
import { MdMenu, MdNotificationsNone, MdOutlineNotificationsOff } from 'react-icons/md'
import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'

import { isAuthenticated, IsAuthenticated, IsGranted, hasPermission, setUser, setAuthorization, clearAuth, getUser } from "./auth/Authorization"

import Auth from "./auth/Auth"
import Console from "./Console"
import List from "./common/List"
import { FancyDate } from "./common/Misc"
import UserEdit from "./UserEdit"
import { ForgotPassword } from "./auth/ForgotPassword"
import cogoToast from "cogo-toast"
import EventBusBridge from "./EventBusBridge"
import LogEventsList from "./LogEventsList"

class Main extends React.Component {
  
  logEventRef = createRef()

  state = { status:false, blink:null, reset:false }

  componentDidMount() {
    if( isAuthenticated() ) this.connectEventBusBridge()
    this.unlisten = this.props.history.listen( location => {
      if( '/logEvents' === location.pathname ) this.setState( { reset:true } )
    } )
  }

  componentWillUnmount() {
    EventBusBridge.close()
    this.unlisten()
  }

  logout = _ => {
    clearAuth()
    delete axios.defaults.headers.common.authorization
    this.props.history.replace( '/auth' )
    EventBusBridge.close()
  }

  onAuthSuccess = resp => {
    const { user, authorization } = resp.data
    setUser( user )
    setAuthorization( authorization )
    axios.defaults.headers.common.authorization = authorization
    this.props.history.replace( '/' )
    this.connectEventBusBridge()
  }
  
  connectEventBusBridge = _ => {
    if( EventBusBridge.isConnected() ) return

    EventBusBridge.connect( 
      _ => this.setState( { status:true } ), 
      {
        'weather.called':( error, msg ) => {
          const { id } = msg.body
          cogoToast.warn( <><b>weather.called</b> -&gt; New Id <b>{id}</b></> )
          this.setState( { blink:id, reset:false } )
        },
        ['user.' + getUser().id]:( error, msg ) => {
          const { id } = msg.body
          cogoToast.info( <><b>user.{getUser().id}</b> -&gt; New Id <b>{id}</b></> )
          if( '/logEvents' === this.props.location.pathname ) 
            this.logEventRef.current?.load( 0 )
          else
            this.setState( { blink:id, reset:false } )
        },
      },
      _ => this.setState( { status:false } )
    )
  }

  render() {
    return <div>
      <div className="mainMenu uk-margin-right uk-margin-top">
        <IsAuthenticated>
          <StatusIndicator {...this.state}/>
          <MdMenu size="2em"/>
          <div data-uk-dropdown="mode: hover">
            <ul className="uk-nav uk-dropdown-nav">
              <li><span className="pointer" onClick={this.logout}>Logout</span></li>
            </ul>
          </div>
        </IsAuthenticated>
      </div>

      <div className="uk-flex">
      
        <IsAuthenticated>
          <ul className="uk-nav uk-nav-default" style={{ minWidth:'200px' }}>
            <IsGranted all="admin">
              <MenuItem to="/console" exact label="Console"/>
            </IsGranted>
            <IsGranted all="superuser">
              <MenuItem to="/users" exact label="Users"/>
            </IsGranted>
            <IsGranted all="kunde">
              <MenuItem to="/logEvents" exact label="Log Events"/>
            </IsGranted>

          </ul>
        </IsAuthenticated>
        

        <div className="uk-container uk-width-expand uk-margin-top" role="main" data-uk-height-viewport="offset-top: true; offset-bottom: 6">
          <Switch>
            <Route path="/auth/forgotPassword" exact component={ForgotPassword} />
            <Route path="/auth" exact component={Auth} />

            <PrivateRoute path="/" exact render={_ => <h1>Main page</h1>} />

            <PrivateRoute path="/users" role="superuser" exact component={UserList}/>
            <PrivateRoute path="/user/edit/:id" role="superuser" exact component={UserEdit}/>

            <PrivateRoute path="/console" role="admin" exact component={Console} />

            <PrivateRoute path="/logEvents" role="kunde" exact render={_ => <LogEventsList ref={this.logEventRef} />} />

            <Route path="/403" render={_ => <h1>Not Authorized</h1>} />
            <Route render={_ => <h1>Not found</h1>} />
          </Switch>
        </div>
      </div>

      <div className="footer">version: {window.projectVersion}</div>
    </div>
  }
}

export default withRouter( Main )

const MenuItem = ( { label, to, clazz, regex, ...rest } ) => 
  <Route {...rest} path={to} children={({ match, location }) => {
    const single = to.substring( 0, to.length - 1 ) + '/'
    const selected = match || location.pathname.startsWith( to ) || location.pathname.startsWith( single ) || ( regex && location.pathname.match( regex ) )
    return <li className={selected ? 'uk-active' : ''}><Link to={to}>{label}</Link></li>
  }}/>

const PrivateRoute = ({ component:C, render, role, ...rest }) =>
  <Route {...rest} render={props => {
    if( isAuthenticated() ){
      if( !role || hasPermission( role ) )
        return C ? <C {...props}/> : render( props )
      else
        return props.history.replace( '/403' )
    }
    else
      return <Redirect to={{ pathname:'/auth', state:{ from:props.location } }}/> 
  }}/>

const UserList = props => <List object="User" {...props} readonly noSearch columns={[ 
  'email', 'name',
  u => [ 'permissions', u.permissions && u.permissions.join( ', ' ) ],
  u => [ 'Birth date', <FancyDate time={u.birthDate}/> ],
  u => [ 'created', <FancyDate time={u.dateCreated}/> ], 
  u => [ 'updated', <FancyDate time={u.lastUpdated}/> ], 
]}/> 

const StatusIndicator = ({ status, blink, reset }) => {
  const [ className, setClassName ] = useState( '' )

  const [ count, setCount ] = useState( -1 )
  
  useEffect( _ => {
    if( reset ){
      setCount( 0 )
    }else{
      setCount( c => c + 1 )
      setClassName( 'scaleUpDown' )
      setTimeout( _ => setClassName( '' ), 1100 )
    }
  }, [ blink, reset ] )

  return <div className={'pointer uk-margin-right ' + className}>
    <Link to="/logEvents" onClick={_ => setCount( 0 )}>
      {status ? <MdNotificationsNone size="2.4em" color="black"/> : <MdOutlineNotificationsOff size="2.4em" color="gray"/>}
      {status && !!count && <span className="uk-badge" style={{ marginLeft:'-1.6em' }}>{100 > count ? count : '99+'}</span>}
    </Link>
  </div>
}
