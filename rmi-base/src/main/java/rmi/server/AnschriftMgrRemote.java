package rmi.server;

import rmi.server.domain.Anschrift;

public interface AnschriftMgrRemote {

  Anschrift getAnschrift( long id ) throws AnschriftException;
  
  String upperCase( String str ) throws AnschriftException;

}