package rmi.server;

import rmi.server.domain.ObjektSO;

public class AbrechnungMgr implements AbrechnungMgrRemote {

  @Override
  public ObjektSO getObjektSO(long id) throws AbrechnungException {
    if (0 >= id)
      throw new AbrechnungException();

    try {
      Thread.sleep(2200);
    } catch (InterruptedException e) {
    }
    
    return Dummyinator3000.<ObjektSO>generate(ObjektSO.class).setId(id);
  }

}
