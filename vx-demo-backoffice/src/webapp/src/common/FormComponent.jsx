import React, { Component, createContext, useContext } from "react"
import { BackArrow, FormButtons, hashCode } from "./Misc"
import { PulseLoader } from "react-spinners"
import cogoToast from "cogo-toast"
import axios from "axios"
import { MdOutlineCheckCircle, MdOutlineErrorOutline } from "react-icons/md"

export const FormContext = createContext( {} )

export default class FormComponent extends Component {
  
  model = null
  
  state = { values:{}, validations:{}, loading:false }
  
  timeouts = {}

  componentDidMount() {
    this.load()
  }

  getUrl() {
    const { id } = this.props.params
    return id ? `/api/${this.getModel()}/${id}` : null
  }

  load() {
    const url = this.getUrl()
    if( !url ) return
    
    this.setState( { loading:true } )
    axios.get( url ).then( resp => {
      if( !resp.data ) return
      const { object } = resp.data
      let state
      if( object ){
        delete resp.data.object
        state = { values:object, ...resp.data }
      }else
        state = { values:resp.data }
      
      state.validations = Object.keys( state.values ).reduce( (res, n) => ({ ...res, [n]:null }), {} )
      this.setState( state, this.onLoad )

    } ).finally( this.setState( { loading:false } ) )
  }
  
  onLoad() {}
  
  validate = field => {
    axios.patch( `/api/${this.getModel()}/${field}`, { [field]:this.state.values[ field ] } ).then( _ => {
      const { validations } = this.state
      validations[ field ] = true
      this.setState( { validations } )
    } ).catch( ({ data }) => {
      const { validations } = this.state
      validations[ field ] = data.body
      this.setState( { validations } )
    } )
  }
    
  kill = _ => {
    const { id } = this.props.params
    axios.delete( `/api/${this.getModel()}/${id}` ).then( () => {
      this.props.navigate( -1 )
      cogoToast.info( <>{this.getModel()} <b>{id}</b> deleted</> )
    } ).catch( resp => cogoToast.warn( resp ) )
  }

  savedData = _ => this.state.values
  
  save = e => {
    e?.preventDefault()

    const { validations } = this.state

    if( Object.values( validations ).some( v => null !== v && true !== v ) ) return

    let data
    try{
      data = this.savedData()
    }catch( msg ){
      cogoToast.error( msg )
      return
    }
    
    const { id } = this.props.params

    axios.post( `/api/${this.getModel()}${id ? '/' + id : ''}`, data ).then( resp => {
      cogoToast.info( <>{this.getModel()} <b>{id}</b> saved</> )
      if( !id ) this.props.navigate( `/${this.getModel()}/edit/${resp.data.id}`, { replace:true } )
    } ).catch( e => {
      cogoToast.error( 'Errors occured by saving', { hideAfter:7 } )
      if( !e?.data?.errors ) return
      Object.entries( e.data.errors ).forEach( ([ field, err ]) => validations[ field ] = err )
      this.setState( { validations } )
    } )
  }
  
  getModel = _ => this.model[ 0 ].toLowerCase() + this.model.substring( 1 )
  
  render( children ) {
    const { id } = this.props.params
    const { loading, validations } = this.state

    const saveDisabled = Object.values( validations ).some( v => null !== v && true !== v )

    return <div>
      <h3><BackArrow/> {id ? 'Edit' : 'Create'} {this.model}</h3>
      
      <PulseLoader loading={loading} color="#bbb" size="1.6em"/>
      
      {!loading && <div className="uk-flex">
        <form onSubmit={this.save} className="uk-form-horizontal uk-width-3-4">
          <fieldset className="uk-fieldset">
            <FormContext.Provider value={{ ...validations }}>
              {children}
            </FormContext.Provider>
          </fieldset>
          
          <FormButtons kill={id && this.kill} saveDisabled={saveDisabled}/>
        </form>

      </div>}
    </div>
  }

  handleChange = ({ currentTarget }) => {
    let { name, checked, value, type } = currentTarget
    const { values, validations } = this.state
    validations[ name ] = null

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
    
    this.setState( { values, validations } )

    if( 'checkbox' === type || !value.length ) return
    
    if( this.timeouts[ name ] ) clearTimeout( this.timeouts[ name ] )
    this.timeouts[ name ] = setTimeout( _ => this.validate( name ), 1000 )
  }
}

export const Field = ({ label, name, children }) => {
  const validation = useContext( FormContext )[ name ] ?? null
  
  const Icon = null !== validation ? ( true === validation ? MdOutlineCheckCircle : MdOutlineErrorOutline ) : null

  return <div className="uk-margin">
    <label className="uk-form-label pointer" htmlFor={name ? '_' + hashCode( name ) : ''}>{label}</label>
    <div className="uk-form-controls">
      {children}
      {Icon && <Icon color={true === validation ? 'green' : 'red'} size="1.6em" style={{ position:'absolute', marginLeft:'.7em', marginTop:'5px' }}/>}
      {null !== validation && true !== validation && <div className="uk-alert-danger" data-uk-alert>{validation}</div>}
    </div>
  </div>
}

export const Text = ({ name, label, value, onChange, className, inputType }) => <Field label={label} name={name}>
  <input className={className ?? 'uk-input'} type={inputType ?? 'text'} name={name} id={'_' + hashCode( name )} value={value ?? ''} onChange={onChange}/>
</Field>

export const LongText = ({ name, label, value, onChange, className, rows }) => <Field label={label} name={name}>
  <textarea className={className ?? 'uk-textarea'} rows={rows ?? 5} name={name} id={'_' + hashCode( name )} value={value ?? ''} onChange={onChange}/>
</Field>

export const Number = ({ name, label, min, max, step, value, onChange, className, inputType }) => <Field label={label} name={name}>
  <input className={className ?? 'uk-input'} type={inputType ?? 'number'} min={min ?? 1} max={max ?? 100} step={step ?? 1} name={name} id={'_' + hashCode( name )} value={value ?? 1} onChange={onChange}/>
</Field>

export const Boolean = ({ name, label, value, defaultValue, onChange, className }) => <Field label={label} name={name + '_' + value}>
  <input className={className ?? 'uk-checkbox'} type="checkbox" name={name} value={value} id={'_' + hashCode( name + '_' + value )} checked={!!defaultValue} onChange={onChange}/>
</Field>