import React, { PureComponent } from 'react'
import axios from 'axios'
import { Link } from "react-router-dom"
import { PulseLoader } from 'react-spinners'
import cogoToast from 'cogo-toast'
import { GrClose } from 'react-icons/gr'
import { IoReload } from 'react-icons/io5'
import { BackArrow } from './Misc'
import { MdKeyboardArrowLeft, MdKeyboardArrowRight } from 'react-icons/md'
import { useQuery } from '@tanstack/react-query'

export default class List extends PureComponent {
  
  columns = null

  object = null

  readonly = null

  linkPath = 'edit'

  title = null

  axiosInstance = null

  state = { list:[], count:0, offset:0, loading:true, query:{ query:'' } }
  
  componentDidMount() {
    const { list, match } = this.props
    
    if( list )
      this.setState( { list, count:list.length, loading:false } ) 
    else if( window.searchParams[ this.object ] )
      this.setState( { query:window.searchParams[ this.object ] }, _ => this.load() )
    else
      this.load( match?.params?.offset ?? 0 )
  }
  
  componentWillUnmount() {
    window.searchParams[ this.object ] = this.state.query
  }

  reload = _ => this.load( this.state.offset ?? 0 )

  load = offset => {
    if( this.props.list ) return

    this.setState( { loading:true } )
    const object = this.object ?? this.props.object
    const max = this.max ?? this.props.max ?? 20
    const obj = object[ 0 ].toLowerCase() + object.substring( 1 )
    const { query } = this.state
    const params = { offset, max, ...query }
    const axs = this.axiosInstance ?? this.props.axios ?? axios
    
    // const { isLoading, isError, error, data, isFetching, isPreviousData } = useQuery( {
    //   queryKey:[ obj + 's', offset ],
    //   queryFn:_ => axs.post( `/api/${obj}s`, params ),
    //   keepPreviousData:true
    // } )
    // this.setState( { ...data, offset, loading:isLoading } )
    axs.post( `/api/${obj}s`, params ).then( resp => this.setState( { ...resp.data, offset } ) ).finally( _ => this.setState( { loading:false } ) )
  }
  
  handleQueryChange = ({ currentTarget }) => {
    const { query } = this.state
    query.query = currentTarget.value
    this.setState( { query } )
  }

  searchBox() {
    if( this.props.noSearch ) return null

    const { query } = this.state.query
    
    return <div className="uk-margin">
      <form onSubmit={this.submitSearch}>
        <input type="text" name="query" className="uk-input uk-width-1-2" placeholder="Search" value={query.query} onChange={this.handleQueryChange}/>
        {query && <GrClose className="pointer uk-margin-left uk-margin-right" size=".8em" color="gray" onClick={this.resetSearch}/>}
        <input type="submit" className="uk-button uk-button-default" value="Search"/>
      </form>
    </div>
  }
  
  newButton() {
    if( this.readonly || this.props.readonly ) return null
    const object = this.object ?? this.props.object
    const obj = object[ 0 ].toLowerCase() + object.substring( 1 )
    return <Link to={`/${obj}/create`}><button type="button" className="uk-button uk-button-primary">+ {object}</button></Link>
  }
  
  resetSearch = e => {
    e?.preventDefault()
    const { query } = this.state
    query.query = ''
    this.setState( { query }, _ => this.search() )
  }

  search() {
    const { search } = this.props
    search && typeof search === 'function' ? search( this.state.query ) : this.load( 0 )
  }

  submitSearch = e => {
    e?.preventDefault()
    this.search()
  }

  render() {
    const { object, columns, linkPath, hili } = { ...this, ...this.props }
    const { loading, count, offset } = this.state
    const list = this.props.list ?? this.state.list

    const obj = object[ 0 ].toLowerCase() + object.substring( 1 )
    
    return <div>
      <h3 className="uk-flex">
        <BackArrow history={this.props.history}/> {this.title ?? object} ({loading ? '..' : count})
        <IoReload className="pointer uk-margin-large-left" size=".7em" onClick={this.reload} data-uk-tooltip="Reload"/>
      </h3>

      {this.searchBox()}

      <div className="uk-margin uk-flex">
        {this.newButton()}
        <PulseLoader loading={loading} className="uk-align-center" color="#bbb" size=".8em"/>
      </div>

      <table className="uk-table uk-table-divider uk-table-middle">
        <thead>
          <tr>
            <th width="3%" style={{ textAlign:'right' }}>#</th>
            {columns.map( ( c, ix ) => {
              const txt = 'function' === typeof c ? c({})[ 0 ] : c
              return <th key={ix}>{txt}</th> 
            })}
          </tr>
        </thead>
        <tbody>
          {list?.map( ( o, trIx ) => 
            <tr key={o.id} className={hili?.includes( o.id ) ? 'hili' : ''}>
              <td width="3%" align="right">{parseInt( offset ?? 0 ) + trIx + 1}.</td>
              {columns.map( ( n, tdIx ) => {
                let v = ''
                if( 'function' === typeof n ){
                  v = n( o )[ 1 ]
                  
                }else{
                  v = o[ n ]
                  switch( typeof v ){
                    case 'object': 
                      v = v ? v.join( ', ' ) : ''
                      break
                    case 'boolean':
                      v = <span style={{fontSize:'larger'}}>{v ? '☑' : '☐'}</span>
                      break
                    case 'string':
                      if( n.startsWith( 'icon' ) ) v = <img alt="icon" style={{ width:'2em', height:'2em' }} onError={window.addDefaultSrc} src={`data:image/svg+xml;utf8,${encodeURIComponent( v )}`}/>
                      break
                    default:
                  }
                }
                return <td key={tdIx}>
                  {0 === tdIx ? <Link to={`/${obj}/${linkPath}/${o.id}`}>{v}</Link> : v}
                </td> } )}
            </tr> ) }

          {!list?.length && <tr><td align="center" colSpan={columns.length + 1}><i>- no {obj}s found -</i></td></tr>}
          
        </tbody>
      </table>

      <hr/>
      
      <Pagination count={count} max={20} offset={offset} onClick={this.load}/>
    </div>
  }

}

export const Pagination = ( { count, max, offset, interval, onClick } ) => {
  const total = Math.ceil( count / max )
  if( 2 > total ) return null
  const current = Math.floor( offset / max )
  
  interval = interval ?? 3
  const lower = Math.max( current - interval + 1, 1 )
  const upper = Math.min( current + interval, total - 1 )

  const range = []
  for( let ix = lower; ix < upper; ix++ ) range.push( ix )

  return <ul className="uk-pagination uk-flex-center uk-margin">
    <li onClick={_ => 0 < current ? onClick( offset - max ) : null} className={0 < current ? 'pointer' : 'uk-disabled'}><span><MdKeyboardArrowLeft size="1.5em"/></span></li>
    <li onClick={_ => 0 !== current ? onClick( 0 ) : null} className={0 === current ? 'uk-active' : 'pointer'}><span>1</span></li>
    
    {1 < lower && <li className="uk-disabled"><span>...</span></li>}
    
    {range.map( ix => <li key={ix} onClick={_ => ix === current ? null : onClick( ix * max )} className={ix === current ? 'uk-active' : 'pointer'}><span>{ix + 1}</span></li> )}

    {total > upper + 1 && <li className="uk-disabled"><span>...</span></li>}

    <li onClick={_ => total !== current ? onClick( ( total - 1 ) * max ) : null} className={total - 1 === current ? 'uk-active' : 'pointer'}><span>{total}</span></li>
    <li onClick={_ => current < total - 1 ? onClick( offset + max ) : null} className={current < total - 1 ? 'pointer' : 'uk-disabled'}><span><MdKeyboardArrowRight size="1.5em"/></span></li>
  </ul>
}