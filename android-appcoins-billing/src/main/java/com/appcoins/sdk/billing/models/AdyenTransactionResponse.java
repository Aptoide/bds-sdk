package com.appcoins.sdk.billing.models;

public class AdyenTransactionResponse {

  private final String uid;
  private final String hash;
  private final String orderReference;
  private final String status;
  private final String pspReference;
  private final String resultCode;
  private final String url;
  private final String paymentData;
  private final String refusalReason;
  private final String refusalReasonCode;
  private final boolean error;

  public AdyenTransactionResponse(String uid, String hash, String orderReference, String status,
      String pspReference, String resultCode, String url, String paymentData, String refusalReason,
      String refusalReasonCode, boolean error) {

    this.uid = uid;
    this.hash = hash;
    this.orderReference = orderReference;
    this.status = status;
    this.pspReference = pspReference;
    this.resultCode = resultCode;
    this.url = url;
    this.paymentData = paymentData;
    this.refusalReason = refusalReason;
    this.refusalReasonCode = refusalReasonCode;
    this.error = error;
  }

  public String getUid() {
    return uid;
  }

  public String getHash() {
    return hash;
  }

  public String getOrderReference() {
    return orderReference;
  }

  public String getStatus() {
    return status;
  }

  public String getPspReference() {
    return pspReference;
  }

  public String getResultCode() {
    return resultCode;
  }

  public String getRefusalReason() {
    return refusalReason;
  }

  public String getRefusalReasonCode() {
    return refusalReasonCode;
  }

  public boolean isError() {
    return error;
  }

  public String getUrl() {
    return url;
  }

  public String getPaymentData() {
    return paymentData;
  }
}