import React, { useState } from "react"
import axios from 'axios'
import cogoToast from "cogo-toast"
import Login from "./Login"
import Register from "./Register"

export default props => {

  const [ mode, setMode ] = useState( 'login' )

  const onAuthSuccess = resp => {
    const { user, authorization } = resp?.data ?? {}
    localStorage.setItem( 'user', JSON.stringify( user ) )
    localStorage.setItem( 'authorization', authorization )
    axios.defaults.headers.common.authorization = authorization
    props.history.replace( '/' )
  }

  const onAuthFail = ({ data }) => cogoToast.error( <span>Login Failed - <strong>{data.message}</strong></span> )

  return <div className="uk-flex uk-flex-center uk-margin-xlarge-top">
    <div className="uk-width-1-3">
      <div>
        <button type="submit" className={`uk-button ${'login' === mode && 'uk-button-primary'}`} onClick={_ => setMode( 'login' )}>Login</button>
        <button type="submit" className={`uk-button ${'register' === mode && 'uk-button-primary'}`} onClick={_ => setMode( 'register' )}>Sign Up</button>
      </div>
      <div className="uk-margin-medium-top">
        {'login' === mode && <Login onAuthSuccess={onAuthSuccess} onAuthFail={onAuthFail}/>}
        {'register' === mode && <Register history={props.history} location={props.location} onAuthSuccess={onAuthSuccess}/>}
      </div>
    </div>
  </div>
}