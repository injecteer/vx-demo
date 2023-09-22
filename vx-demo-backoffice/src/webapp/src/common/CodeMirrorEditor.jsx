import React, { useEffect, useState } from "react"
import { Controlled as CodeMirror } from 'react-codemirror2'
import 'codemirror/lib/codemirror.css'
import 'codemirror/addon/edit/matchbrackets'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/search/searchcursor'
import 'codemirror/mode/groovy/groovy'
import 'codemirror/mode/javascript/javascript'
import 'codemirror/addon/comment/comment'
import 'codemirror/addon/hint/show-hint'
import 'codemirror/addon/hint/anyword-hint'

const OPTS = { autoCloseTags:true, lineNumbers:true, tabSize:2, lineWrapping:true, matchBrackets:true }
               
const toggleComment = editor => _ => {
  const start = editor.getCursor( true ).line
  const stop = editor.getCursor( false ).line
  let anyComments = false
  let allComments = true
  for( var ix = start; ix <= stop; ix++ ){
    let isComment = editor.getLine( ix ).startsWith( '//' )
    anyComments |= isComment
    allComments &= isComment
  }
  // if allComments, un-comment all
  // if anyComments, comment non-commented
  // otherwise comment all
  for( var ix = start; ix <= stop; ix++ ){
    const line = editor.getLine( ix )
    const lineStart = { line:ix, ch:0 }
    const lineEnd = { line:ix, ch:line.length }
    if( allComments )
      editor.replaceRange( line.substring( 2 ), lineStart, lineEnd )
    else if( anyComments ){
      if( !line.startsWith( '//' ) ) editor.replaceRange( '//' + line, lineStart, lineEnd )
    }else
      editor.replaceRange( '//' + line, lineStart, lineEnd )
  }
}

export default ({ value, onChange, options, onEditorMount }) => {
  if( null === value || ( 'undefined' === typeof value ) ) return null
  const opts = { ...OPTS, ...options }

  const editorDidMount = editor => {
    if( 'groovy' === opts.mode ) editor.toggleComment = toggleComment( editor )
    onEditorMount && onEditorMount( editor )
  }

  const [ val, setVal ] = useState( value ?? '' )

  useEffect( _ => setVal( value ), [ value ] )

  useEffect( _ => onChange( val ), [ val ] )

  const setValue = ( _, __, v ) => setVal( v )

  return <CodeMirror value={val} options={opts} className="CodeMirror-autoheight" onChange={setValue} onBeforeChange={setValue} editorDidMount={editorDidMount}/>
}