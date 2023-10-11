import { createContext, useCallback, useMemo, useState } from 'react'

export const ListParamContext = createContext( {} )

/**
 * Saves search parameter for lists, to reproduce the same list results on re-rendering the page
 */
export default ({ children }) => {

  const [ params, setParams ] = useState( {} )
  
  const setParamsCB = useCallback( setParams, [] )

  return <ListParamContext.Provider value={useMemo( _ => ({ params, setParams:setParamsCB }), [ params, setParamsCB ] )}>
    {children}
  </ListParamContext.Provider>
}