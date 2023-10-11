import { Component } from 'react'
import SockJS from 'sockjs-client'
import { MdOutlinePlayCircleFilled, MdOutlinePauseCircleFilled, MdMonitorHeart, MdCircle, MdSave, MdSettingsBackupRestore, MdOutlineClear } from 'react-icons/md'
import cogoToast from 'cogo-toast'

import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'
import './App.css'

export default class App extends Component {
  
  sockJS = null
  
  state = { list:[], options:{}, state:SockJS.CLOSED, settingsFor:null }

  timer = null

  componentDidMount() {
    this.connect()
  }

  connect = _ => {
    if( SockJS.OPEN === this.sockJS?.readyState ) return

    this.sockJS = new SockJS( process.env.REACT_APP_SERVER + '/ws' )
    this.sockJS.onopen = _ => {
      console.clear()
      this.setState( { state:this.sockJS.readyState } )
    }
    this.sockJS.onclose = _ => this.setState( { state:this.sockJS?.readyState ?? SockJS.CLOSED } )

    this.sockJS.onmessage = ({ data }) => {
      const json = JSON.parse( data )

      switch( json.type ) {
        case 'content':
          let { options } = this.state
          if( !Object.values( options ).length ) options = Object.keys( json.body ).reduce( ( res, curr ) => ({ ...res, [curr]:{ instances:1, worker:false } }), {} )
          this.setState( { list:json.body, options } )
          break
        case 'log':
          console.info( json.body )
          break
        case 'info':
          cogoToast.info( <><b>{json.verticle}</b><pre>{JSON.stringify( json.body, null, 2 )}</pre></>, { hideAfter:7 } )
          break
        case 'error':
          cogoToast.error( <pre><b>{json.verticle}</b><br/>{json.body}</pre>, { hideAfter:5 } )
          break

        default:
      } 
    }
  }

  componentWillUnmount() {
    this.sockJS?.close()
    this.sockJS = null
  }

  send = cmd => this.sockJS.send( JSON.stringify( cmd ) )

  start = verticle => e => {
    clearTimeout( this.timer )
    if( 1 === e.detail )
      this.timer = setTimeout( _ => {
        this.send( { command:'start', verticle, options:this.state.options[ verticle ] } )
        this.setState( { settingsFor:null } )
      }, 300 )
    else
      this.setState( { settingsFor:verticle } )
  }

  startAll = _ => this.send( { command:'start-all', verticles:this.state.options } )

  stop = verticle => _ => this.send( { command:'stop', verticle } )

  health = verticle => _ => this.send( { command:'health', verticle } )

  healthAll = _ => this.send( { command:'health-all' } )

  storePreset = _ => {
    const { options, list } = this.state
    const preset = Object.entries( list ).filter( ([ _, id ]) => !!id ).reduce( (res, [ name ]) => ({ ...res, [name]:options[ name ] }), {} )
    localStorage.setItem( 'preset', JSON.stringify( preset ) )
    cogoToast.info( 'Preset recorded' )
  }

  restorePreset = _ => {
    let options = localStorage.getItem( 'preset' )
    if( !options ){
      cogoToast.warn( 'No Preset recorded!' )
      return
    }
    options = JSON.parse( options )
    this.setState( { options, settingsFor:null }, _ => this.startAll() )
    cogoToast.info( 'Preset restored and started' )
  }

  handleChange = ({ currentTarget }) => {
    let { name, checked, value, type } = currentTarget
    const { options, settingsFor } = this.state
    const opt = options[ settingsFor ]

    switch( type ){
      case 'checkbox':
        opt[ name ] = checked
        break
        
      case 'number':
        opt[ name ] = parseInt( value )
        break
        
      default:
        opt[ name ] = value
    }
    this.setState( { options } )
  }
  
  render() {
    const { list, state, options, settingsFor } = this.state
    
    const opt = options[ settingsFor ]

    return <div className="uk-container uk-width-3-5 uk-margin-top" role="main" data-uk-height-viewport="offset-top: true; offset-bottom: 6">
      
      <h2><img src={process.env.PUBLIC_URL + '/favicon.png'} alt="logo" height="74" width="74" className="uk-margin-right"/>Mothership Dashboard <MdCircle className="pointer uk-margin-left" size=".6em" onClick={this.connect} color={SockJS.OPEN === state ? 'green' : 'red'} uk-tooltip={SockJS.OPEN === state ? 'Online' : 'Offline'}/></h2>

      <div className="uk-flex uk-flex-right uk-margin uk-width-5-6">
        <MdSave className="pointer uk-margin-left" onClick={this.storePreset} size="3em" uk-tooltip="Save run preset"/>
        <MdSettingsBackupRestore className="pointer uk-margin-left" onClick={this.restorePreset} size="3em" uk-tooltip="Restore & start preset"/>
        
        <hr className="uk-divider-vertical" style={{ height:'50px' }}/>
        
        <MdOutlinePlayCircleFilled className="pointer uk-margin-left" onClick={this.startAll} size="3em" color="green" uk-tooltip="Start All"/>      
        <MdMonitorHeart className="pointer uk-margin-left" onClick={this.healthAll} size="3em" uk-tooltip="Health Check"/>
      </div>

      <table className="uk-table">
        <thead>
          <tr>
            <th width="5%" style={{ textAlign:'right' }}>#</th>
            <th width="64%">Verticle</th>
            <th width="20%">actions</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {Object.entries( list ).map( ( [ v, stateDesc ], ix ) => {
            const [ state, desc ] = stateDesc

            return <tr key={ix}>
              <td align="right">{ix + 1}.</td>
              <td>
                <div uk-tooltip={state && `${options[ v ].worker ? 'worker, ' : ''} ${options[ v ].instances} inst`} style={state ? { color:'green', fontWeight:'bolder' } : {}}>{v}</div>
                <div style={{ fontSize:'small', color:'#aaa' }}>{desc}</div>
              </td>
              <td>
                {state ? 
                  <MdOutlinePauseCircleFilled className="pointer" onClick={this.stop( v )} size="2.6em" color="deepskyblue" uk-tooltip="Stop"/> :
                  <MdOutlinePlayCircleFilled className="pointer" onClick={this.start( v )} size="2.6em" color="green" uk-tooltip="Start"/>}

                <MdMonitorHeart className="pointer uk-margin-left" onClick={state ? this.health( v ) : _ => null} color={state ? '#555' : '#ddd'} size="2.6em" uk-tooltip="Health Check"/>
              </td>
              <td>{opt && v === settingsFor && <div style={{ position:'absolute' }}>
                
                <label htmlFor={'_' + hashCode( 'instances_' + v )} className="pointer">Instances</label>&nbsp;
                <input type="number" className="uk-range uk-form-width-xsmall" id={'_' + hashCode( 'instances_' + v )} name="instances" min="1" max="4" value={opt.instances} onChange={this.handleChange}/>
                &nbsp; 
                
                <label htmlFor={'_' + hashCode( 'worker_' + v )} className="pointer">Worker</label>&nbsp;
                <input type="checkbox" className="uk-checkbox" id={'_' + hashCode( 'worker_' + v )} name="worker" defaultChecked={opt.worker} onChange={this.handleChange}/>
                &nbsp;&nbsp;&nbsp;
                
                <button className="uk-button uk-button-primary uk-button-small" onClick={this.start( v )}>OK</button>
                <MdOutlineClear size="1.5em" className="pointer uk-margin-small-left" onClick={_ => this.setState( { settingsFor:null } )}/>
              </div>}</td>
            </tr> 
          } )}  
        </tbody>
      </table>

      <div className="footer">version: {window.projectVersion}</div>
    </div>
  }
}

const hashCode = s => {
  var hash = 0, i, chr
  if( s.length === 0 ) return hash
  for( i = 0; i < s.length; i++ ){
    chr = s.charCodeAt( i )
    hash = (( hash << 5 ) - hash) + chr
    hash |= 0 // Convert to 32bit integer
  }
  return hash
}