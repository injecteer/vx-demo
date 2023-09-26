import React from 'react'
import { createRoot } from 'react-dom/client'
import Modal from 'react-modal'
import { BrowserRouter } from "react-router-dom"
import Main from './Main'
import axios from 'axios'
import './index.css'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

Modal.setAppElement( '#root' )

const queryClient = new QueryClient()

axios.defaults.baseURL = window.hostUrl
axios.defaults.headers.common[ 'Content-Type' ] = 'application/json; charset=UTF-8'

axios.defaults.headers.common.authorization = localStorage.getItem( 'authorization' )

axios.interceptors.response.use( resp => {
  const { authorization } = resp.headers
  
  if( authorization ){
    // console.info( 'new authorization', authorization.length )
    axios.defaults.headers.common.authorization = authorization
    localStorage.setItem( 'authorization', authorization )
  }
  return resp
}, err => {
  try{
    if( !err.request ) return Promise.reject( { data:'Network error' } )

    switch( axios.defaults.headers.common.authorization ? err.request.status : null ){
      case 401:
        delete axios.defaults.headers.common.authorization
        localStorage.removeItem( 'authorization' )
        localStorage.removeItem( 'userAccount' )
        window.location.reload()
        return Promise.reject( err.response )
        
      case 403:
        return Promise.resolve( err.response )
        
      default:
        return Promise.reject( err.response )
      }
  }catch(e){
    return Promise.reject( { data:'Network error' } )
  }
} )

createRoot( document.getElementById( 'root' ) ).render( <BrowserRouter>
  <QueryClientProvider client={queryClient}>
    <Main/>
  </QueryClientProvider>
</BrowserRouter> )
