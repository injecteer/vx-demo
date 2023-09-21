package vx.demo.domain2

enum Permission {
  kunde, bearbeiter, superuser, admin
  
  int asMask() { 1 << ordinal() }
}
