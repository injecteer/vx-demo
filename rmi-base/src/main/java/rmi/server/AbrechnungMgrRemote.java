package rmi.server;

import rmi.server.domain.ObjektSO;

public interface AbrechnungMgrRemote {

  ObjektSO getObjektSO( long id ) throws AbrechnungException;

}