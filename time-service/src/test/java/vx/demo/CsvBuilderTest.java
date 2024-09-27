package vx.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.Test;

class CsvBuilderTest {

  @Test
  void test() {
    var b = new CsvBuilder();
    
    b.row( r -> r
        .c( "aa", 22 ) 
        .c( "bb", "bbb" )
        .c( "number", 99, new DecimalFormat( "#####" ) )
        .c( "date", new Date( 672744428639l ), new SimpleDateFormat( "yyyy.MM.dd HH:mm" ) ) );
    
    b.row( r -> r
        .c( "aa", 33 ) 
        .c( "bb", "ccc" )
        .c( "number", 5, new DecimalFormat( "#####" ) )
        .c( "date", new Date( 672744428639l ), new SimpleDateFormat( "yyyy.MM.dd HH:mm" ) ) );
    
    System.out.println( b.build() );
  }

}
