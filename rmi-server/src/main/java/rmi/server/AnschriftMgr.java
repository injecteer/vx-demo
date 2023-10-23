package rmi.server;

import rmi.server.domain.Anschrift;

public class AnschriftMgr implements AnschriftMgrRemote {

  @Override
  public Anschrift getAnschrift(long id) throws AnschriftException {
    if (0 >= id)
      throw new AnschriftException( "Id can not be negative!" );
    return Dummyinator3000.<Anschrift>generate(Anschrift.class);
  }

  @Override
  public String upperCase(String str) throws AnschriftException {
    try {
      return str.toUpperCase();
    }catch( Exception e ) {
      throw new AnschriftException( e.getMessage() );
    }
  }

}
