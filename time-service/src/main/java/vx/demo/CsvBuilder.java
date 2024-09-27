package vx.demo;

import java.text.Format;
import java.util.function.Consumer;

public class CsvBuilder {

  final String lineBreak;
  
  final String separator;
  
  final String quote;
  
  private StringBuilder out = new StringBuilder();
  
  final RowBuilder rowBuilder = new RowBuilder();

  public CsvBuilder() {
    separator = ";";
    quote = "\"";
    lineBreak = "\n";
  }
  
  public CsvBuilder( String separator, String quote, String lineBreak ) {
    this.separator = separator;
    this.quote = quote;
    this.lineBreak = lineBreak;
  }

  public void row( Consumer<RowBuilder> block ) {
    block.accept( rowBuilder );
    if( out.isEmpty() ) out.append( rowBuilder.head );
    out.append( lineBreak ).append( rowBuilder.row );
    rowBuilder.clear();
  }

  public String build() {
    return out.toString();
  }
  
  class RowBuilder {
    
    StringBuilder head = new StringBuilder();
    StringBuilder row = new StringBuilder();
    
    public RowBuilder c( String name, Object val, Format... fmt ) {
      return column( name, val, fmt );
    }
    
    public RowBuilder column( String name, Object val, Format... fmt ) {
      if( out.isEmpty() ){
        if( !head.isEmpty() ) head.append( separator ); 
        head.append( quote ).append( name ).append( quote );
      }
      
      if( !row.isEmpty() ) row.append( separator );
      String txt = 1 == fmt.length && null != fmt[ 0 ] ? fmt[ 0 ].format( val ) : val.toString();
      row.append( quote ).append( txt ).append( quote );
      
      return this;
    }
    
    void clear() {
      head.setLength( 0 );
      row.setLength( 0 );
    }
  }
  
}
