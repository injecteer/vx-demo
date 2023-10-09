import React, { useContext, useState } from "react"
import cogoToast from "cogo-toast"
import Login from "./Login"
import Register from "./Register"
import { AuthContext } from "./Authorization"
import { useLocation, useNavigate } from "react-router-dom"

export default _ => {

  const [ mode, setMode ] = useState( 'login' )

  const { setUser } = useContext( AuthContext )
  
  const nav = useNavigate()
  let location = useLocation()

  const onAuthSuccess = resp => {
    const { user } = resp.data
    setUser( user )
    nav.replace( '/' )
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
        {'register' === mode && <Register history={nav} location={location} onAuthSuccess={onAuthSuccess}/>}
      </div>
    </div>
  </div>
}