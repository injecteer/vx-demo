import React, { useState } from "react"
import axios from 'axios'
import cogoToast from "cogo-toast"
import { BackClose } from "../common/Misc"
import { PulseLoader } from "react-spinners"

export const ForgotPassword = ({ history, location, onSubmit }) => {
  
  const [ email, setEmail ] = useState( location && location.state && location.state.email || '' )
  
  const [ loading, setLoading ] = useState( false )
  
  const send = e => {
    if( e ) e.preventDefault()
    if( onSubmit ){
      onSubmit( email )
      return
    }
    if( 0 === email.length ) return
    setLoading( true )
    axios.post( '/api/pub/forgotPassword', { email } ).then( _ => {
      history.goBack()
      cogoToast.info( 'Check your Inbox for the Password Reset Email' )
    } ).catch( err => console.error( err ) ).finally( _ => setLoading( false ) )
  }

  return <div className="uk-flex uk-flex-center uk-margin-top">
    <div className="uk-width-1-2">
    
      <h2>
        <BackClose history={history}/>
        Forgot password?
      </h2>

      <form onSubmit={send}>
        <fieldset className="uk-fieldset">
          <div className="uk-margin">
            <input type="text" disabled={loading} className="uk-input" placeholder="E-mail" defaultValue={email} onChange={e => setEmail( e.currentTarget.value.trim() )}/>
          </div>
        </fieldset>
        <div className="uk-align-right">
          <PulseLoader loading={loading} color="#bbb" size=".6em"/>
          <button type="submit" disabled={loading} className="uk-button uk-button-primary uk-margin-left">Send</button>
        </div>
      </form>
    </div>
  </div>
}