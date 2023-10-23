package rmi.server;

public class AbrechnungException extends Exception {

  private static final long serialVersionUID = 3977859566442525488L;

  public AbrechnungException() {
    super();
  }

  public AbrechnungException(final String message) {
    super(message);
  }

  public AbrechnungException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public AbrechnungException(final Throwable cause) {
    super(cause);
  }
}