package rmi.server.domain;

import java.io.Serializable;
import java.util.Date;

public class Anschrift implements Serializable {

  private static final long serialVersionUID = 4049077129201070130L;

  public String strasse;

  public boolean isZustellanschrift;

  public Date aenderungstermin;

  public String getStrasse() {
    return strasse;
  }

  public void setStrasse(String strasse) {
    this.strasse = strasse;
  }

  public boolean isZustellanschrift() {
    return isZustellanschrift;
  }

  public void setZustellanschrift(boolean isZustellanschrift) {
    this.isZustellanschrift = isZustellanschrift;
  }

  public Date getAenderungstermin() {
    return aenderungstermin;
  }

  public void setAenderungstermin(Date aenderungstermin) {
    this.aenderungstermin = aenderungstermin;
  }

}