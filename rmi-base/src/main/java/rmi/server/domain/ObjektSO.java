package rmi.server.domain;

import java.io.Serializable;
import java.util.Date;

public class ObjektSO implements Comparable<ObjektSO>, Serializable {

  private static final long serialVersionUID = 2853647410376104279L;

  private long id;

  private long fkHeizkostenLiegenschaft;

  private Date letzteshzgDatum;
  private Date letztesnbkDatum;
  private Date ersteSollstellung;
  private Date letzteSollstellung;
  private Date letzteGebuehrenaenderung;

  private String bezeichnung;

  private int nummer;

  private int endeHeizungsjahr;
  private int endeNebenkostenjahr;

  private int reorgArt;

  private boolean isMieterInWeg;

  private Anschrift anschrift;

  public long getId() {
    return id;
  }

  public ObjektSO setId(long id) {
    this.id = id;
    return this;
  }

  public String getBezeichnung() {
    return bezeichnung;
  }

  public void setBezeichnung(String bezeichnung) {
    this.bezeichnung = bezeichnung;
  }

  public int getEndeHeizungsjahr() {
    return endeHeizungsjahr;
  }

  public void setEndeHeizungsjahr(int endeHeizungsjahr) {
    this.endeHeizungsjahr = endeHeizungsjahr;
  }

  public int getEndeNebenkostenjahr() {
    return endeNebenkostenjahr;
  }

  public void setEndeNebenkostenjahr(int endeNebenkostenjahr) {
    this.endeNebenkostenjahr = endeNebenkostenjahr;
  }

  public Date getErsteSollstellung() {
    return ersteSollstellung;
  }

  public void setErsteSollstellung(Date ersteSollstellung) {
    this.ersteSollstellung = ersteSollstellung;
  }

  public long getFkHeizkostenLiegenschaft() {
    return fkHeizkostenLiegenschaft;
  }

  public void setFkHeizkostenLiegenschaft(long fkHeizkostenLiegenschaft) {
    this.fkHeizkostenLiegenschaft = fkHeizkostenLiegenschaft;
  }

  public Date getLetzteGebuehrenaenderung() {
    return letzteGebuehrenaenderung;
  }

  public void setLetzteGebuehrenaenderung(Date letzteGebuehrenaenderung) {
    this.letzteGebuehrenaenderung = letzteGebuehrenaenderung;
  }

  public Date getLetzteshzgDatum() {
    return letzteshzgDatum;
  }

  public void setLetzteshzgDatum(Date letzteshzgDatum) {
    this.letzteshzgDatum = letzteshzgDatum;
  }

  public Date getLetztesnbkDatum() {
    return letztesnbkDatum;
  }

  public void setLetztesnbkDatum(Date letztesnbkDatum) {
    this.letztesnbkDatum = letztesnbkDatum;
  }

  public Date getLetzteSollstellung() {
    return letzteSollstellung;
  }

  public void setLetzteSollstellung(Date letzteSollstellung) {
    this.letzteSollstellung = letzteSollstellung;
  }

  public int getNummer() {
    return nummer;
  }

  public void setNummer(int nummer) {
    this.nummer = nummer;
  }

  public int getReorgArt() {
    return reorgArt;
  }

  public void setReorgArt(int reorgArt) {
    this.reorgArt = reorgArt;
  }

  public boolean isMieterInWeg() {
    return isMieterInWeg;
  }

  public void setMieterInWeg(boolean isMieterInWeg) {
    this.isMieterInWeg = isMieterInWeg;
  }

  public Anschrift getAnschrift() {
    return anschrift;
  }

  public void setAnschrift(Anschrift anschrift) {
    this.anschrift = anschrift;
  }

  @Override
  public int compareTo(ObjektSO obj) {
    return Integer.compare(nummer, obj.getNummer());
  }
}