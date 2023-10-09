import React, { useContext } from "react"
import { Route, Routes, Navigate, useNavigate, Outlet, useLocation } from 'react-router'
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
import { Link } from "react-router-dom"

export default () => {
  
  const { user, clearAuth } = useContext( AuthContext )
  
  const { newIds } = useContext( EventBusContext )

  const nav = useNavigate()

  const logout = _ => {
    clearAuth()
    nav.navigate( '/auth' )
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
            <MenuItem to="/console" label="Console"/>
          </IsGranted>
          <IsGranted all="superuser">
            <MenuItem to="/users" label="Users"/>
          </IsGranted>
          <IsGranted all="kunde">
            <MenuItem to="/logEvents" label={<>Log Events {!!newIds.length && <span className="uk-badge">{20 > newIds.length ? newIds.length : '20+'}</span>}</>}/>
          </IsGranted>
        </ul>
      </IsAuthenticated>

      <div className="uk-container uk-width-expand uk-margin-top" role="main" data-uk-height-viewport="offset-top: true; offset-bottom: 6">
        <Routes>
          <Route path="auth">
            <Route path="forgotPassword" element={<ForgotPassword/>} />
            <Route path="" element={<Auth/>} />
          </Route>

          <Route element={<For role="superuser"/>}>
            <Route path="users" element={<UserList/>}/>
            <Route path="user/edit/:id" element={<UserEdit navigate={nav}/>}/>
          </Route>

          <Route element={<For role="admin"/>} >
            <Route path="console" element={<Console/>} />
          </Route>

          <Route element={<For role="kunde"/>}>
            <Route path="logEvents" element={<LogEventsList/>} />
            <Route path="logEvent/edit/:id" element={<LogEventEdit navigate={nav}/>}/>
            <Route path="/" element={<h1>Main page</h1>} /> 
          </Route>
       
          <Route path="403" element={<h1>Not Authorized</h1>} />
          <Route path="*" element={<h1>Not found</h1>} />
        </Routes>
      </div>
    </div>

    <div className="footer">version: {window.projectVersion}</div>
  </div>
}

const MenuItem = ({ label, to }) => {
  const location = useLocation()
  const single = to.substring( 0, to.length - 1 ) + '/'
  const selected = location.pathname.startsWith( to ) || location.pathname.startsWith( single )
  return <li className={selected ? 'uk-active' : ''}><Link to={to}>{label}</Link></li>
}

const For = ({ role, ...rest }) => {
  if( !isAuthenticated() ) return <Navigate to="/auth"/> 
  return !role || hasPermission( role ) ? <Outlet/> : <Navigate  to="/403"/> 
}

const UserList = props => <List object="User" {...props} readonly noSearch columns={[ 
  'email', 'name',
  u => [ 'permissions', u.permissions && u.permissions.join( ', ' ) ],
  u => [ 'Birth date', <FancyDate time={u.birthDate}/> ],
  u => [ 'created', <FancyDate time={u.dateCreated}/> ], 
  u => [ 'updated', <FancyDate time={u.lastUpdated}/> ], 
]}/>