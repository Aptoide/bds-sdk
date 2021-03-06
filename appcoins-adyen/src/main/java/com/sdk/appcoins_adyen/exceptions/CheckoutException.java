package com.sdk.appcoins_adyen.exceptions;

/**
 * Exception generated by the Checkout components to indicate an error.
 * Usually related to an implementation error.
 */
public class CheckoutException extends RuntimeException {

  private static final long serialVersionUID = -2465223452079546925L;

  public CheckoutException(String errorMessage) {
    this(errorMessage, null);
  }

  public CheckoutException(String errorMessage, Throwable cause) {
    super(errorMessage, cause);
  }
}