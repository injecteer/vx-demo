import React from "react"
import FormComponent, { Boolean, Text }  from "../common/FormComponent"

export default class LogEventEdit extends FormComponent {

  model = 'logEvent'

  mainName = 'what'

  savedData = _ => {
    const { what, success } = this.state.values
    return { what, success }
  }

  render() {
    const { what, success } = this.state.values ?? {}

    return super.render( <>
      <Text label="What" name="what" value={what} onChange={this.handleChange}/>
      <Boolean label="Success" name="success" defaultValue={success} onChange={this.handleChange}/>
    </> )
  }

}