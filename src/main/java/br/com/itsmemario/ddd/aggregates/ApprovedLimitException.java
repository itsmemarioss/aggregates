package br.com.itsmemario.ddd.aggregates;

public class ApprovedLimitException extends Exception {
  public ApprovedLimitException(String message) {
    super(message);
  }

  public ApprovedLimitException(String message, Throwable cause) {
    super(message, cause);
  }
}
