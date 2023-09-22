import React from "react"

export const setUser = user => localStorage.setItem( 'user', JSON.stringify( user ) )

export const getUser = () => { 
  const u = localStorage.getItem( 'user' )
  try{
    return JSON.parse( u )
  }catch(e){
    return null
  }
}

export const setAuthorization = authorization => localStorage.setItem( 'authorization', authorization )

export const isAuthenticated = () => localStorage.getItem( 'authorization' ) && getUser()

export const IsAuthenticated = ({ children }) => <>{isAuthenticated() && children}</>

export const hasPermission = perm => {
  const u = getUser()
  return perm && u && u.permissions.includes( perm )
}

export const IsGranted = ({ all, any, children }) => {
  let granted = false
  
  if( all?.length )
    granted |= 'string' == typeof all ? hasPermission( all ) : all.every( hasPermission )
  else if( any?.length )
    granted |= 'string' == typeof any ? hasPermission( any ) : any.some( hasPermission )

  return granted ? children : null
}
