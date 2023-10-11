import React, { useState } from "react"
import axios from 'axios'
import cogoToast from "cogo-toast"
import { Text } from "../common/FormComponent"

export default ({ onAuthSuccess }) => {
  
  const [ state, setState ] = useState( { email:'', name:'', password:'', password1:'', birthDate:'' } )
  
  const checkPasswords = _ => {
    const { password, password1 } = state
    if( password !== password1 ){
      cogoToast.warn( 'Passwords do not match!' )
      return false
    }
    return true
  }

  const register = e => {
    e?.preventDefault()

    const { email, name, password, birthDate } = state
    const emptyFields = Object.entries( { email, name, password, birthDate } ).filter( ([ k, v ]) => !v.length ).map( ([ k ]) => k )

    if( emptyFields.length ){
      cogoToast.warn( <span>You have to fill in the fields <ul>{emptyFields.map( (f, ix) => <li key={ix}><b>{f}</b></li> )}</ul></span> )
      return
    } 
    if( !checkPasswords() ) return
    
    axios.post( '/api/pub/register', { email, name, birthDate, password } ).then( onAuthSuccess ).catch( e => {
      console.warn( 'register failed', e )
      cogoToast.error( <span>Registration failed with following error(s) <ul>{e?.data?.errors?.map( (f, ix) => <li key={ix}><b>{f}</b></li> )}</ul></span> )
    } )
  }

  const setValue = key => ({ currentTarget }) => setState( s => ({ ...s, [key]:currentTarget.value }) )

  const { email, name, birthDate, password, password1 } = state

  return <div className="uk-container">
    <form onSubmit={register} className="uk-form-horizontal">
      
      <fieldset className="uk-fieldset">
        <Text label="E-mail" name="email" value={email} onChange={setValue( 'email' )}/>

        <Text label="Name" name="name" value={name} onChange={setValue( 'name' )}/>

        <Text label="Birth Date" inputType="date" name="birthDate" value={birthDate} onChange={setValue( 'birthDate' )}/>

        <Text label="Enter the PA55WORD" name="password" inputType="password" value={password} onChange={setValue( 'password' )}/>

        <Text label="Repeat the PA55WORD" name="password1" inputType="password" value={password1} onChange={setValue( 'password1' )}/>
      </fieldset>

      <div>
        <button type="submit" className="uk-button uk-button-primary uk-align-right">Register</button>
      </div>
    </form>
  </div>
}