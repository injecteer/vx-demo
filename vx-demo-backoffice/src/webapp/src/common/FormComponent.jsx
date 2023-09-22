import React, { Component } from "react"
import { BackArrow, FormButtons, hashCode } from "./Misc"
import { PulseLoader } from "react-spinners"
import cogoToast from "cogo-toast"
import axios from "axios"

export default class FormComponent extends Component {
  
  model = null
  
  mainName = 'name'
  
  state = { values:{}, loading:false }
  
  componentDidMount() {
    this.load()
  }

  getUrl() {
    const { id } = this.props.match.params
    return id ? `/api/${this.getModel()}/${id}` : null
  }

  load() {
    const url = this.getUrl()
    if( !url ) return
    
    this.setState( { loading:true } )
    axios.get( url ).then( resp => {
      if( !resp.data ) return
      const { object } = resp.data
      if( object ){
        delete resp.data.object
        this.setState( { values:object, ...resp.data }, this.onLoad )
      }else
        this.setState( { values:resp.data }, this.onLoad )
    } ).finally( this.setState( { loading:false } ) )
  }
  
  onLoad() {}
  
  kill = _ => {
    const { id } = this.props.match.params
    axios.delete( `/api/${this.getModel()}/${id}` ).then( () => {
      this.props.history.goBack()
      cogoToast.info( this.state.values[ this.mainName ] + ' deleted' )
    } ).catch( resp => cogoToast.warn( resp ) )
  }
  
  savedData = _ => this.state.values
  
  save = e => {
    e?.preventDefault()

    let data
    try{
      data = this.savedData()
    }catch( msg ){
      cogoToast.error( msg )
      return
    }
    
    const { id } = this.props.match.params

    axios.post( `/api/${this.getModel()}${id ? '/' + id : ''}`, data ).then( resp => {
      cogoToast.info( this.state.values[ this.mainName ] + ' saved' )
      if( !id ) this.props.history.replace( `/${this.getModel()}/edit/${resp.data.id}` )
    } ).catch( err => cogoToast.warn( 'string' == typeof err ? err: JSON.stringify( err.errors ) ) )
  }
  
  getModel = _ => this.model[ 0 ].toLowerCase() + this.model.substring( 1 )
  
  render( children, preview ) {
    const { id } = this.props.match.params
    const { loading } = this.state

    return <div>
      <h3><BackArrow history={this.props.history}/> {id ? 'Edit' : 'Create'} {this.model}</h3>
      
      <PulseLoader loading={loading} color="#bbb" size="1.6em"/>
      
      {!loading && <div className="uk-flex">
        <form onSubmit={this.save} className="uk-form-horizontal uk-width-1-2">
          <fieldset className="uk-fieldset">
            {children}
          </fieldset>
          
          <FormButtons kill={id && this.kill} history={this.props.history}/>
        </form>

        <div className="uk-width-1-2 uk-margin-large-left">
          {preview}
        </div>
      </div>}
    </div>
  }

  handleChange = ({ currentTarget }) => {
    let { name, checked, value, type } = currentTarget
    const values = this.state.values ?? {}

    switch( type ){
      case 'checkbox':
        if( name.endsWith( '*' ) ){
          name = name.substring( 0, name.length - 1 )
          if( values[ name ] ){
            const ix = values[ name ].indexOf( value )
            checked ? values[ name ].push( value ) : values[ name ].splice( ix, 1 )
          }else if( checked )
            values[ name ] = [ value ]
        }else
          values[ name ] = checked
        break
        
      case 'number':
      case 'range':
        const int = parseInt( value )
        const flt = parseFloat( value )
        values[ name ] = int === flt ? int : flt
        break
        
      case 'select':
        values[ name ] = value || null
        break

      default:
        values[ name ] = value
    }
    
    this.setState( { values } )
  }
}

export const Field = ({ label, name, children }) => <div className="uk-margin">
  <label className="uk-form-label pointer" htmlFor={name ? '_' + hashCode( name ) : ''}>{label}</label>
  <div className="uk-form-controls">
    {children}
  </div>
</div>

export const Text = ({ name, label, value, onChange, className, inputType }) => <Field label={label} name={name}>
  <input className={className ?? 'uk-input'} type={inputType ?? 'text'} name={name} id={'_' + hashCode( name )} value={value ?? ''} onChange={onChange}/>
</Field>

export const LongText = ({ name, label, value, onChange, className, rows }) => <Field label={label} name={name}>
  <textarea className={className ?? 'uk-textarea'} rows={rows ?? 5} name={name} id={'_' + hashCode( name )} value={value ?? ''} onChange={onChange}/>
</Field>

export const Number = ({ name, label, min, max, step, value, onChange, className, inputType }) => <Field label={label} name={name}>
  <input className={className ?? 'uk-input'} type={inputType ?? 'number'} min={min ?? 1} max={max ?? 100} step={step ?? 1} name={name} id={'_' + hashCode( name )} value={value ?? 1} onChange={onChange}/>
</Field>

export const Boolean = ({ name, label, value, defaultValue, onChange, className }) => <Field label={label} name={name}>
  <input className={className ?? 'uk-checkbox'} type="checkbox" name={name} id={'_' + hashCode( name )} checked={value ?? defaultValue ?? true} onChange={onChange}/>
</Field>