import React, { Component } from "react"
import axios from 'axios'
import cogoToast from "cogo-toast"
import { Text } from "../common/FormComponent"
import { Navigate } from "react-router-dom"

export default class Register extends Component {
  
  state = { redirectToReferrer:false, email:'', name:'', password:'', password1:'', birthDate:'' }

  register = e => {
    e?.preventDefault()

    const { email, name, password, birthDate } = this.state
    const emptyFields = Object.entries( { email, name, password, birthDate } ).filter( ([ k, v ]) => !v.length ).map( ([ k ]) => k )

    if( emptyFields.length ){
      cogoToast.warn( <span>You have to fill in the fields <ul>{emptyFields.map( (f, ix) => <li key={ix}><b>{f}</b></li> )}</ul></span> )
      return
    } 
    if( !this.checkPasswords() ) return
    
    if( this.props.onSubmit ){
      this.props.onSubmit( { email, name, password } )
      return
    }
    
    axios.post( '/api/pub/register', { email, name, birthDate, password } ).then( resp => {
      this.setState( { redirectToReferrer:true } )
      this.props.onAuthSuccess()
    } ).catch( e => {
      console.warn( 'register failed', e )
      this.setState( { redirectToReferrer:false } )
      cogoToast.error( <span>Registration failed with following error(s) <ul>{e.data?.errors?.map( (f, ix) => <li key={ix}><b>{f}</b></li> )}</ul></span> )
    } )
  }

  checkPasswords = _ => {
    const { password, password1 } = this.state
    if( password !== password1 ){
      cogoToast.warn( 'Passwords do not match!' )
      return false
    }
    return true
  }

  setValue = key => ({ currentTarget }) => this.setState( { [key]:currentTarget.value } )

  render() {
    let { from } = this.props.location.state || { from:{ pathname:'/' } }
    if( this.state.redirectToReferrer ) return <Navigate to={from} />

    const { email, name, birthDate, password, password1 } = this.state

    return <div className="uk-container">
      <form onSubmit={this.register} className="uk-form-horizontal">
        
        <fieldset className="uk-fieldset">
          <Text label="E-mail" name="email" value={email} onChange={this.setValue( 'email' )}/>

          <Text label="Name" name="name" value={name} onChange={this.setValue( 'name' )}/>

          <Text label="Birth Date" inputType="date" name="birthDate" value={birthDate} onChange={this.setValue( 'birthDate' )}/>

          <Text label="Enter the PA55WORD" name="password" inputType="password" value={password} onChange={this.setValue( 'password' )}/>

          <Text label="Repeat the PA55WORD" name="password1" inputType="password" value={password1} onChange={this.setValue( 'password1' )}/>
        </fieldset>

        <div>
          <button type="submit" className="uk-button uk-button-primary uk-align-right">Register</button>
        </div>
      </form>
    </div>
  }
}