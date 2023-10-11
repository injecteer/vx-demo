import React from 'react'
import { createRoot } from 'react-dom/client'
import Modal from 'react-modal'
import { BrowserRouter } from "react-router-dom"
import Main from './Main'
import axios from 'axios'
import './index.css'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider, clearAuth, getAuthorization, storeAuthorization } from './auth/Authorization'
import { EventBusProvider } from './eventBus/EventBusProvider'
import ListParamProvider from './common/ListParamProvider'

Modal.setAppElement( '#root' )

const queryClient = new QueryClient()

axios.defaults.baseURL = process.env.REACT_APP_SERVER
axios.defaults.headers.common[ 'Content-Type' ] = 'application/json; charset=UTF-8'

axios.defaults.headers.common.authorization = getAuthorization()

axios.interceptors.response.use( resp => {
  const { authorization } = resp.headers
  
  if( authorization ){
    // console.info( 'new authorization', authorization.length )
    axios.defaults.headers.common.authorization = authorization
    storeAuthorization( authorization )
  }
  return resp
}, err => {
  try{
    if( !err.request ) return Promise.reject( { data:'Network error' } )

    switch( axios.defaults.headers.common.authorization ? err.request.status : null ){
      case 401:
        clearAuth()
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
    <AuthProvider>
      <EventBusProvider>
        <ListParamProvider>
          <Main/>
        </ListParamProvider>
      </EventBusProvider>
    </AuthProvider>
  </QueryClientProvider>
</BrowserRouter> )
