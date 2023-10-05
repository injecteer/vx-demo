import React, { Component } from "react"
import axios from 'axios'
import { Link } from "react-router-dom"
import { Boolean, Text } from "../common/FormComponent"

export default class Login extends Component {
  
  state = { email:'', password:'', rememberMe:true }
  
  login = e => {
    e?.preventDefault()

    const { onSubmit, onAuthSuccess, onAuthFail } = this.props
    
    if( onSubmit ){
      onSubmit( this.state )
      return
    }
    let { email, password, rememberMe } = this.state
    if( !email.length || !password.length ) return
  
    axios.post( '/api/pub/login', { email, password, rememberMe } ).then( onAuthSuccess ).catch( onAuthFail )
  }
  
  setValue = key => ({ currentTarget }) => this.setState( { [key]:currentTarget.value } )

  render() {
    const { email, password, rememberMe } = this.state

    return <div className="uk-container">
      <form onSubmit={this.login} className="uk-form-horizontal">
        <fieldset className="uk-fieldset">

          <Text label="E-mail" name="email" value={email} onChange={this.setValue( 'email' )}/>

          <Text label="Enter the PA55WORD" name="password" inputType="password" value={password} onChange={this.setValue( 'password' )}/>

          <Boolean label="Remember Me" name="rememberMe" defaultValue={rememberMe} onChange={e => this.setState( { rememberMe:e.currentTarget.checked } )}/>
        </fieldset>

        <div>
          <Link to={{ pathname:'/auth/forgotPassword', state:{ email } }} className="uk-margin-right">Forgot password?</Link>
          <button type="submit" className="uk-button uk-button-primary uk-align-right">Login</button>
        </div>
      </form>
    </div>
  }
}