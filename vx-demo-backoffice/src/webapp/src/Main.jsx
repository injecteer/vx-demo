import React from "react"
import { Link } from "react-router-dom"
import { Switch, Route, Redirect, withRouter } from 'react-router'
import axios from 'axios'
import { MdCircle, MdMenu } from 'react-icons/md'
import EventBus from '@vertx/eventbus-bridge-client.js'
import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'

import { isAuthenticated, IsAuthenticated, IsGranted, hasPermission, getUser, getAuthorisation } from "./auth/Authorization"

import Auth from "./auth/Auth"
import Console from "./Console"
import List from "./common/List"
import { FancyDate } from "./common/Misc"
import UserEdit from "./UserEdit"
import { ForgotPassword } from "./auth/ForgotPassword"
import cogoToast from "cogo-toast"

class Main extends React.Component {
  
  eventBus = null

  state = { status:false }

  componentDidMount() {
    this.connectEventBus()
  }

  logout = _ => {
    localStorage.removeItem( 'authorization' )
    localStorage.removeItem( 'userAccount' )
    delete axios.defaults.headers.common.authorization
    this.props.history.replace( '/auth' )
  }

  onAuthSuccess = resp => {
    const { userAccount, authorization } = resp.data
    localStorage.setItem( 'userAccount', JSON.stringify( userAccount ) )
    localStorage.setItem( 'authorization', authorization )
    axios.defaults.headers.common.authorization = authorization
    this.props.history.replace( '/' )
  }
  
  connectEventBus = _ => {
    if( this.eventBus || !isAuthenticated() ) return
    
    this.setState( { status:null } )

    this.eventBus = new EventBus( axios.defaults.baseURL + '/eventbus', { vertxbus_ping_interval:30000 } )
    
    this.eventBus.onopen = _ => {
      this.setState( { status:true } )

      this.eventBus.registerHandler( 'logevent.changed', { authorization:getAuthorisation() }, ( error, msg ) => {
        const { type, id } = msg.body
        cogoToast.info( <>New Id <b>{id}</b></> )
        this.setState( { clazz:'scaleUpDown', [type]:type + '.' + id } )
        setTimeout( _ => this.setState( { clazz:'' } ), 1100 )
      } )
    }
    
    this.eventBus.onerror = err => {
      console.info( 'onerror', err )
      cogoToast.error( err.message )
      this.setState( { status:false } )
      this.eventBus.close()
      this.eventBus = null
    }
    
    this.eventBus.onclose = _ => {
      cogoToast.info( 'Connection closed' )
      this.eventBus = null
      this.setState( { status:false } )
    }
  }

  LogEventsList = _ => <List key={this.state.LogEvent} noSearch object="LogEvent" readonly columns={[ 'id', 'what', 'success', e => [ 'created', <FancyDate time={e.dateCreated}/> ] ]}/>
  
  render() {
    const { status, clazz } = this.state
    
    const color = null === status ? 'gray' : ( status ? 'green' : 'red' )
    
    return <div>
      <div className="mainMenu uk-margin-right uk-margin-top">
        <IsAuthenticated>
          <MdCircle size="2em" color={color} onClick={this.connectEventBus} className={'pointer uk-margin-right ' + clazz}/>
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

            <PrivateRoute path="/users/:offset" role="superuser" exact component={UserList}/>
            <PrivateRoute path="/users" role="superuser" exact component={UserList}/>
            <PrivateRoute path="/user/edit/:id" role="superuser" exact component={UserEdit}/>

            <PrivateRoute path="/console" role="admin" exact component={Console} />

            <PrivateRoute path="/logEvents/:offset" role="kunde" exact component={this.LogEventsList} />
            <PrivateRoute path="/logEvents" role="kunde" exact component={this.LogEventsList} />

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
