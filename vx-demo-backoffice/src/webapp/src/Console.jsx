import axios from "axios"
import React, { Component } from "react"
import { BackArrow } from './common/Misc'
import CodeMirrorEditor from "./common/CodeMirrorEditor"
import { PulseLoader } from "react-spinners"

export default class Console extends Component {
  
  state = { script:'', result:null, error:null, loading:false }

  setScript = script => this.setState( { script } )

  execute = _ => {
    this.setState( { loading:true } )
    axios.put( '/api/console/execute', { script:this.state.script } )
      .then( resp => this.setState( { result:resp.data, error:null, loading:false } ) )
      .catch( err => this.setState( { error:{ message:err?.data?.error, stack:err?.data?.stackTrace }, result:null, loading:false } ) )
  }

  render(){
    const { script, result, error, loading } = this.state || {}

    return <div>
      <h3>
        <BackArrow history={this.props.history}/> Console
      </h3>

      <div className="uk-margin-top">
        <CodeMirrorEditor value={script ?? ''} onChange={this.setScript} options={{ mode:'groovy', extraKeys:{ 'Ctrl-Space':'autocomplete', 'Ctrl-/':'toggleComment' }}}/>
      </div>

      <div className="uk-margin-top uk-flex uk-flex-middle">
        <button type="button" className="uk-button uk-button-primary" onClick={this.execute}>Execute</button>
        <PulseLoader className="uk-margin-left" loading={loading} color="#bbb" size="1.1em"/>
      </div>

      <div className="uk-margin-top">
        <legend>Output</legend>
        {result && <div className="uk-alert-primary" data-uk-alert>{result.message}</div>}
        {error && <div className="uk-alert-warning" data-uk-alert>{error.message}</div>}
        {result && <pre>{JSON.stringify( result.result ) || '- no results -'}</pre>}
        {error && <pre>{error.stack}</pre>}
      </div>
    </div>
  }
}