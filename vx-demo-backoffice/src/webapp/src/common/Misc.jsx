import React, { useEffect, useRef, useState } from "react"
import { MdArrowBack, MdCheckBox, MdClose } from "react-icons/md"
import { GrClose } from "react-icons/gr"
import moment from 'moment'
import { confirmAlert } from 'react-confirm-alert'
import 'react-confirm-alert/src/react-confirm-alert.css'

export const BackArrow = ({ history }) => <MdArrowBack className="pointer" onClick={() => history.goBack()}/> 

export const BackClose = ({ history }) => <GrClose className="pointer uk-margin-right" size=".8em" onClick={_ => history.goBack()}/> 

export const FancyDate = ({ time, threshold }) => {
  time = moment( time )
  return time.isBefore( moment().subtract( threshold || 36, 'hours' ) ) ? time.format( 'DD.MM.yyyy' ) : time.fromNow()
}

export const hashCode = s => {
  var hash = 0, i, chr
  if( s.length === 0 ) return hash
  for( i = 0; i < s.length; i++ ){
    chr = s.charCodeAt( i )
    hash = (( hash << 5 ) - hash) + chr
    hash |= 0 // Convert to 32bit integer
  }
  return hash
}

export const onDelete = kill => _ => {
  confirmAlert( { 
    title:'Confirm deletion',
    message:'Are you sure you want to delete this?',
    buttons:[ { label:'Delete', onClick:kill ?? ( _ => null )}, { label:'Cancel' } ],
    closeOnEscape:true,
    closeOnClickOutside:true,
  } )
}

export const FormButtons = ({ history, kill }) => <div className="uk-margin-top">
  <button type="button" className="uk-button uk-button-default" onClick={() => history && history.goBack()}>Cancel</button> 
  <button type="submit" className="uk-button uk-button-primary uk-align-right">{kill ? 'Update' : 'Save'}</button> 
  {kill && <button type="button" className="uk-button uk-button-danger uk-align-right" onClick={onDelete( kill )}>Delete</button>}
</div>

export const InlineEdit = ({ text, onDone }) => {

  const [ value, setValue ] = useState( text )

  const [ editing, setEditing ] = useState( false )
  
  const cancel = _ => {
    setValue( text )
    setEditing( false )
  }

  const finish = _ => {
    onDone( value )
    setEditing( false )
  }

  const onKeyDown = ({ key }) => {
    switch( key ){
      case 'Escape':
        cancel()
        break
      case 'Enter':
        finish()
        break
    }
  }
  
  const inputRef = useRef()

  useEffect( _ => inputRef?.current?.focus(), [ editing ] )

  return editing ? 
    <>
      <div className="inline-edit-overlay" onClick={cancel} onFocus={cancel}/>
      <div className="inline-edit">
        <input type="text" className="uk-input uk-form-width-medium uk-form-small" ref={inputRef} value={value} onChange={({ currentTarget }) => setValue( currentTarget.value )} onKeyDown={onKeyDown} />
        <MdCheckBox color="green" size="2em" className="pointer" onClick={finish}/>
        <MdClose size="2em" className="pointer" onClick={cancel}/>
      </div> 
    </>
    : 
    <span className="pointer" onClick={_ => setEditing( true )}>{value}</span>
}