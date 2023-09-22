import React from "react"
import { Link } from "react-router-dom"
import { Switch, Route, Redirect, withRouter } from 'react-router'
import axios from 'axios'
import { MdMenu } from 'react-icons/md'
import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'

import { isAuthenticated, IsAuthenticated, IsGranted, hasPermission } from "./auth/Authorization"

import Auth from "./auth/Auth"
import Console from "./Console"
import List from "./common/List"
import { FancyDate } from "./common/Misc"
import UserEdit from "./UserEdit"
import { ForgotPassword } from "./auth/ForgotPassword"

class Main extends React.Component {
  
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

  render() {
    return <div>
      <div className="mainMenu">
        <IsAuthenticated>
          <MdMenu/>
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

            <PrivateRoute path="/logEvents/:offset" role="kunde" exact component={LogEventList} />
            <PrivateRoute path="/logEvents" role="kunde" exact component={LogEventList} />

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

const UserList = props => <List object="User" {...props} readonly columns={[ 
  'email', 'name',
  u => [ 'permissions', u.permissions && u.permissions.join( ', ' ) ],
  u => [ 'Birth date', <FancyDate time={u.birthDate}/> ],
  u => [ 'created', <FancyDate time={u.dateCreated}/> ], 
  u => [ 'updated', <FancyDate time={u.lastUpdated}/> ], 
]}/>

const LogEventList = props => <List noSearch object="LogEvent" readonly {...props} columns={[ 'what', 'success', e => [ 'created', <FancyDate time={e.dateCreated}/> ] ]}/>
