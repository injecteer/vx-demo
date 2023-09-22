package vx.demo.backoffice.controller

import org.springframework.context.MessageSource

import io.vertx.ext.web.Router
import vx.demo.authorization.Grant
import vx.demo.domain2.Address
import vx.demo.domain2.Permission
import vx.demo.domain2.User
import vx.demo.web.CRUDController

@Grant( [ Permission.kunde ] )
class UserController extends CRUDController<User> {
  
  UserController( Router router, MessageSource messageSource ){
    super(router, messageSource, User, Address )
  }
  
  @Override
  void bind( Object o, Map data, boolean bindIdOnly ) {
    super.bind( o, data, bindIdOnly )
    ((User)o).setPermissions data.permissions
  }
}
