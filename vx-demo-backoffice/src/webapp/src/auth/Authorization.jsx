import axios from "axios"
import React, { createContext, useContext, useState } from "react"

export const storeUser = user => localStorage.setItem( 'user', JSON.stringify( user ) )

export const getUser = () => { 
  const u = localStorage.getItem( 'user' )
  try{
    return JSON.parse( u )
  }catch(e){
    return null
  }
}

export const storeAuthorization = authorization => localStorage.setItem( 'authorization', authorization )

export const getAuthorization = _ => localStorage.getItem( 'authorization' )

export const isAuthenticated = _ => !!axios.defaults.headers.common.authorization

export const IsAuthenticated = ({ children }) => isAuthenticated() && children

export const hasPermission = perm => {
  const user = getUser()
  return perm && user?.permissions?.includes( perm )
}

export const IsGranted = ({ all, any, children }) => {
  let granted = false
  
  if( all?.length )
    granted |= 'string' == typeof all ? hasPermission( all ) : all.every( hasPermission )
  else if( any?.length )
    granted |= 'string' == typeof any ? hasPermission( any ) : any.some( hasPermission )

  return granted ? children : null
}

export const clearAuth = _ => {
  localStorage.removeItem( 'authorization' )
  localStorage.removeItem( 'user' )
  axios.defaults.headers.common.authorization = null
}

export const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
  const [ user, setUser ] = useState( getUser() )
  const saveUser = user => {
    setUser( user )
    storeUser( user )
  }
  return <AuthContext.Provider value={ { user, setUser:saveUser, clearAuth }}>
    {children}
  </AuthContext.Provider>
}