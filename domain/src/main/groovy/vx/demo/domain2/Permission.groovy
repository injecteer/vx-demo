package vx.demo.domain2

enum Permission {
  kunde, bearbeiter, superuser, admin
  
  int asMask() { 1 << ordinal() }
  
  static int all() { ( 1 << values().size() ) - 1 }
  
}
