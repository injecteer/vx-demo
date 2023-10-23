package rmi.server;

import rmi.server.domain.ObjektSO;

public class AbrechnungMgr implements AbrechnungMgrRemote {

  @Override
  public ObjektSO getObjektSO(long id) throws AbrechnungException {
    if (0 >= id)
      throw new AbrechnungException();

    return RemoteHelper.<ObjektSO>generate(ObjektSO.class).setId(id);
  }

}
