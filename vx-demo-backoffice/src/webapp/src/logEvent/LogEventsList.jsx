import React, { useContext, useEffect, useRef } from "react"
import { Boolean } from "../common/FormComponent"
import List from "../common/List"
import { FancyDate } from "../common/Misc"
import { EventBusContext } from "../eventBus/EventBusProvider"

export default _ => {
  const ref = useRef()
  const { indicator, setIndicator } = useContext( EventBusContext )
  useEffect( () => {
    if( indicator.newId ) ref.current.load() 
    if( indicator.count ) setIndicator( { count:null } )
  }, [ indicator ] )
  return <LogEventsList ref={ref}/>
}

class LogEventsList extends List {
  
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
