import React, { useContext } from "react"
import { Link, useHistory } from "react-router-dom"
import { Switch, Route, Redirect, withRouter } from 'react-router'
import { MdMenu } from 'react-icons/md'
import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'

import { isAuthenticated, IsAuthenticated, IsGranted, hasPermission, AuthContext } from "./auth/Authorization"

import Auth from "./auth/Auth"
import Console from "./Console"
import List from "./common/List"
import { FancyDate } from "./common/Misc"
import UserEdit from "./UserEdit"
import { ForgotPassword } from "./auth/ForgotPassword"
import LogEventsList from "./logEvent/LogEventsList"
import LogEventEdit from "./logEvent/LogEventEdit"
import StatusIndicator from "./eventBus/StatusIndicator"
import { EventBusContext } from "./eventBus/EventBusProvider"

const Main = () => {
  
  const { user, clearAuth } = useContext( AuthContext )
  
  const { status, indicator } = useContext( EventBusContext )

  const history = useHistory()

  const logout = _ => {
    clearAuth()
    history.replace( '/auth' )
  }

  return <div>
    <div className="mainMenu uk-margin-right uk-margin-top">
      <IsAuthenticated>
        <StatusIndicator/>
        <MdMenu size="2em"/>
        <div data-uk-dropdown="mode: hover">
          <ul className="uk-nav uk-dropdown-nav">
            <li>{user?.id}: {user?.email}</li>
            <li>{user?.name}</li>
            <li className="uk-nav-divider"></li>
            <li><span className="pointer" onClick={logout}>Logout</span></li>
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
            <MenuItem to="/logEvents" exact label={<>Log Events {status && !!indicator.count && <span className="uk-badge">{20 > indicator.count ? indicator.count : '20+'}</span>}</>}/>
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

          <PrivateRoute path="/logEvents" role="kunde" exact component={LogEventsList} />
          <PrivateRoute path="/logEvent/edit/:id" role="kunde" exact component={LogEventEdit}/>

          <Route path="/403" render={_ => <h1>Not Authorized</h1>} />
          <Route render={_ => <h1>Not found</h1>} />
        </Switch>
      </div>
    </div>

    <div className="footer">version: {window.projectVersion}</div>
  </div>
}

export default withRouter( Main )

const MenuItem = ({ label, to, clazz, regex, ...rest }) => 
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