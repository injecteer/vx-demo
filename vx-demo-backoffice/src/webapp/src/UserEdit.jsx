import React from "react"
import FormComponent, { Field, Text }  from "./common/FormComponent"
import { hashCode }  from "./common/Misc"
import moment from "moment"

export default class UserEdit extends FormComponent {

  model = 'user'

  mainName = 'email'

  onLoad() {
    const { values } = this.state
    values.birthDate = moment( values.birthDate ).format( 'YYYY-MM-DD' )
    this.setState( { values } )
  }

  savedData = _ => {
    const { name, permissions, birthDate } = this.state.values
    console.info( permissions )
    return { name, permissions, birthDate }
  }

  render() {
    const { name, birthDate, permissions } = this.state.values ?? {}

    return super.render( <>
      <Text label="Name" name="name" value={name} onChange={this.handleChange}/>

      <Text label="Birth Date" inputType="date" name="birthDate" value={birthDate} onChange={this.handleChange}/>
      
      <Field label="Permissions">
      {[ 'kunde', 'bearbeiter', 'superuser', 'admin' ].map( p => 
          <label className="uk-margin-right pointer" key={p} htmlFor={'_' + hashCode( p )}>
            <input className="uk-checkbox" type="checkbox" name="permissions*" value={p} id={'_' + hashCode( p )} defaultChecked={permissions && permissions.includes( p )} onChange={this.handleChange}/> {p}
          </label>
        )}
      </Field>

    </> )
  }

}