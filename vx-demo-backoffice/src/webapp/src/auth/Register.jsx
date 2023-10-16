import React, { useEffect, useState } from "react"
import axios from 'axios'
import cogoToast from "cogo-toast"
import { FormContext, Text } from "../common/FormComponent"

const timeouts = {}

export default ({ onAuthSuccess }) => {

  const [ validations, setValidations ] = useState( { email:null, name:null, password:null, password1:null, birthDate:null } )

  const [ state, setState ] = useState( { email:'', name:'', password:'', password1:'', birthDate:'' } )
  
  const validate = ( field, value ) => {
    if( value.length ) axios.patch( `/api/pub/check/${field}`, { [field]:value } )
         .then( _ => setValidations( old => ({ ...old, [field]:true }) ) )
         .catch( ({ data }) => setValidations( old => ({ ...old, [field]:data.body }) ) )
  }

  const register = e => {
    e?.preventDefault()

    if( Object.values( validations ).some( v => !v || true !== v ) ) return

    const { email, name, password, birthDate } = state

    axios.post( '/api/pub/register', { email, name, birthDate, password } ).then( onAuthSuccess ).catch( e => {
      if( e?.data?.errors ) setValidations( Object.keys( e.data.errors ) )
      cogoToast.error( 'Registration failed' )
    } )
  }

  useEffect( _ => {
    if( state.password1.length ) setValidations( old => ({ ...old, password1:state.password !== state.password1 ? 'Passwords do not match!' : true }) )
  }, [ state ] )

  const setValue = e => {
    let { name, value } = e.currentTarget

    setState( s => ({ ...s, [name]:value }) )
    setValidations( e => ({ ...e, [name]:null }) )

    if( 'password1' === name || !value.length ) return
    
    if( timeouts[ name ] ) clearTimeout( timeouts[ name ] )
    timeouts[ name ] = setTimeout( _ => validate( name, value ), 1000 )
  }

  return <div className="uk-container">
    <form onSubmit={register} className="uk-form-horizontal">
      
      <FormContext.Provider value={{ ...validations }}>
        <fieldset className="uk-fieldset">
          <Text label="E-mail" name="email" value={state.email} onChange={setValue}/>

          <Text label="Name" name="name" value={state.name} onChange={setValue}/>

          <Text label="Birth Date" inputType="date" name="birthDate" value={state.birthDate} onChange={setValue}/>

          <Text label="Enter the PA55WORD" name="password" inputType="password" value={state.password} onChange={setValue}/>

          <Text label="Repeat the PA55WORD" name="password1" inputType="password" value={state.password1} onChange={setValue}/>
        </fieldset>
      </FormContext.Provider>

      <div>
        <button type="submit" className="uk-button uk-button-primary uk-align-right" disabled={Object.values( validations ).some( v => !v || true !== v )}>Register</button>
      </div>
    </form>
  </div>
}