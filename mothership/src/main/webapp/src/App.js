import { Component } from 'react'
import SockJS from 'sockjs-client'
import { MdOutlineDashboardCustomize, MdOutlinePlayCircleFilled, MdStopCircle, MdMonitorHeart } from 'react-icons/md'
import cogoToast from 'cogo-toast'

import 'uikit/dist/css/uikit.min.css'
import 'uikit/dist/js/uikit.min.js'
import './App.css'

export default class App extends Component {

  sockJS = null
  
  state = { list:[] }

  componentDidMount() {
    this.sockJS = new SockJS( window.hostUrl + '/ws' )
    this.sockJS.onmessage = ({ data }) => {
      const json = JSON.parse( data )
      switch( json.type ) {
        case 'content':
          this.setState( { list:json.body } )
          break
        case 'info':
          cogoToast.info( <><b>{json.verticle}</b><pre>{JSON.stringify( json.body, null, 2 )}</pre></>, { hideAfter:7 } )
          break
        case 'error':
          cogoToast.error( <><b>{json.verticle}</b>: {json.body}</>, { hideAfter:7 } )
          break
      } 
    }
  }

  componentWillUnmount() {
    this.sockJS?.close()
  }

  send = cmd => this.sockJS.send( JSON.stringify( cmd ) )

  start = verticle => _ => this.send( { command:'start', verticle } )

  stop = verticle => _ => this.send( { command:'stop', verticle } )

  health = verticle => _ => this.send( { command:'health', verticle } )

  healthAll = _ => this.send( { command:'health-all' } )

  render() {
    const { list } = this.state

    return <div className="uk-container uk-width-1-2 uk-margin-top" role="main" data-uk-height-viewport="offset-top: true; offset-bottom: 6">
      
      <h1><MdOutlineDashboardCustomize/> Verticles Dashboard</h1>
      <br/>
      
      <MdMonitorHeart className="pointer uk-margin-left" onClick={this.healthAll} size="3em" uk-tooltip="Health Check"/>

      <table className="uk-table">
        <thead>
          <tr>
            <th width="3%" style={{ textAlign:'right' }}>#</th>
            <th>Verticle</th>
            <th>actions</th>
          </tr>
        </thead>
        <tbody>
          {Object.entries( list ).map( ( [ v, state ], ix ) => <tr key={ix}>
            <td width="3%" align="right">{ix + 1}.</td>
            <td>{v}</td>
            <td>
              {state ? 
                <MdStopCircle className="pointer" onClick={this.stop( v )} size="2.6em" color="red" uk-tooltip="Stop"/> :
                <MdOutlinePlayCircleFilled className="pointer" onClick={this.start( v )} size="2.6em" color="green" uk-tooltip="Start"/>}

              <MdMonitorHeart className="pointer uk-margin-left" onClick={state ? this.health( v ) : _ => null} color={state ? '#555' : '#ddd'} size="2.6em" uk-tooltip="Health Check"/>
            </td>
          </tr> )}  
        </tbody>
      </table>

      <div className="footer">version: {window.projectVersion}</div>
    </div>
  }
}