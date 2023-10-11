import React, { useState } from "react"
import axios from 'axios'
import { Link } from "react-router-dom"
import { Boolean, Text } from "../common/FormComponent"

export default ({ onAuthSuccess, onAuthFail }) => {
  
  const [ state, setState ] = useState( { email:'', password:'', rememberMe:true } )
  
  const login = e => {
    e?.preventDefault()

    const { email, password, rememberMe } = state

    if( email.length && password.length ) 
      axios.post( '/api/pub/login', { email, password, rememberMe } ).then( onAuthSuccess ).catch( onAuthFail )
  }
  
  const setValue = key => ({ currentTarget }) => setState( s => ({ ...s, [key]:currentTarget.value }) )

  const { email, password, rememberMe } = state

  return <div className="uk-container">
    <form onSubmit={login} className="uk-form-horizontal">
      <fieldset className="uk-fieldset">

        <Text label="E-mail" name="email" value={email} onChange={setValue( 'email' )}/>

        <Text label="Enter the PA55WORD" name="password" inputType="password" value={password} onChange={setValue( 'password' )}/>

        <Boolean label="Remember Me" name="rememberMe" defaultValue={rememberMe} onChange={({ currentTarget }) => setState( s => ({ ...s, rememberMe:currentTarget.checked }) )}/>
      </fieldset>

      <div>
        <Link to={{ pathname:'/auth/forgotPassword', state:{ email } }} className="uk-margin-right">Forgot password?</Link>
        <button type="submit" className="uk-button uk-button-primary uk-align-right">Login</button>
      </div>
    </form>
  </div>
}