import React from "react"
import FormComponent, { Boolean, Field, Text }  from "./common/FormComponent"
import moment from "moment"
import { withParams } from "./common/Misc"

class UserEdit extends FormComponent {

  model = 'user'

  mainName = 'email'

  onLoad() {
    const { values } = this.state
    values.birthDate = moment( values.birthDate ).format( 'YYYY-MM-DD' )
    this.setState( { values } )
  }

  savedData = _ => {
    const { name, permissions, birthDate } = this.state.values
    return { name, permissions, birthDate }
  }

  render() {
    const { name, birthDate, permissions } = this.state.values ?? {}

    return super.render( <>
      <Text label="Name" name="name" value={name} onChange={this.handleChange}/>

      <Text label="Birth Date" inputType="date" name="birthDate" value={birthDate} onChange={this.handleChange}/>
      
      <Field label="Permissions">
        {[ 'kunde', 'bearbeiter', 'superuser', 'admin' ].map( p => 
          <Boolean key={p} label={p} name="permissions*" value={p} defaultValue={permissions?.includes( p )} onChange={this.handleChange}/>
        )}
      </Field>

    </> )
  }

}

export default withParams( UserEdit )