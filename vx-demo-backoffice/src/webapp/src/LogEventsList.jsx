import React from "react"
import List from "./common/List"
import { FancyDate } from "./common/Misc"
import { Boolean } from "./common/FormComponent"

export default class LogEventsList extends List {
  
  object = 'LogEvent'
  
  columns = [ 'id', e => [ 'user', e.user?.name ], 'what', 'success', e => [ 'created', <FancyDate time={e.dateCreated}/> ] ]
  
  readonly = true
  
  handleQueryChange = ({ currentTarget }) => {
    let { name, checked, value } = currentTarget
    const { query } = this.state
    
    if( name.endsWith( '*' ) ){
      name = name.substring( 0, name.length - 1 )
      query[ name ] = checked ? [ value ] : null
    }else
      query[ name ] = checked

    this.setState( { query }, this.search )
  }

  searchBox() {
    const { query } = this.state
    return <div className="uk-form-horizontal">
      <Boolean label="Show only my events" name="my" onChange={this.handleQueryChange} value="my" defaultValue={!!query.my}/>
      <Boolean label="Show only successful" name="successful" onChange={this.handleQueryChange} value="successful" defaultValue={!!query.successful}/>

      {[ 'time', 'weather' ].map( w => 
        <Boolean key={w} label={<>Show only <b>{w}</b> events</>} name="whats*" value={w} defaultValue={query.whats?.includes( w )} onChange={this.handleQueryChange}/>
      )}
    </div>
  }
} 
