package com.appcoins.sdk.billing.service.adyen;

import com.appcoins.sdk.billing.listeners.NoInfoResponseListener;
import com.appcoins.sdk.billing.listeners.billing.GetTransactionListener;
import com.appcoins.sdk.billing.listeners.billing.LoadPaymentInfoListener;
import com.appcoins.sdk.billing.listeners.billing.MakePaymentListener;
import com.appcoins.sdk.billing.models.billing.AdyenPaymentParams;
import com.appcoins.sdk.billing.models.billing.TransactionInformation;
import com.appcoins.sdk.billing.models.billing.TransactionWallets;
import com.appcoins.sdk.billing.service.Service;
import com.appcoins.sdk.billing.service.ServiceResponseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdyenRepository {

  private Service bdsService;
  private AdyenListenerProvider adyenListenerProvider;

  public AdyenRepository(Service bdsService, AdyenListenerProvider adyenListenerProvider) {

    this.bdsService = bdsService;
    this.adyenListenerProvider = adyenListenerProvider;
  }

  public void loadPaymentInfo(String method, String value, String currency, String walletAddress,
      final LoadPaymentInfoListener listener) {
    Map<String, String> queries = new LinkedHashMap<>();
    queries.put("wallet.address", walletAddress);
    queries.put("price.value", value);
    queries.put("price.currency", currency);
    queries.put("method", method);
    ServiceResponseListener serviceResponseListener =
        adyenListenerProvider.createLoadPaymentInfoListener(listener);

    bdsService.makeRequest("8.20191202/gateways/adyen_v2/payment-methods", "GET",
        new ArrayList<String>(), queries, null, null, serviceResponseListener);
  }

  public void makePayment(AdyenPaymentParams adyenPaymentParams,
      TransactionInformation transactionInformation, TransactionWallets transactionWallets,
      final MakePaymentListener makePaymentListener) {
    Map<String, String> queries = new HashMap<>();
    queries.put("wallet.address", transactionWallets.getMainWalletAddress());

    Map<String, Object> body =
        buildMakePaymentBody(adyenPaymentParams, transactionInformation, transactionWallets);
    ServiceResponseListener serviceResponseListener =
        adyenListenerProvider.createMakePaymentListener(makePaymentListener);

    bdsService.makeRequest("8.20191202/gateways/adyen_v2/transactions", "POST",
        new ArrayList<String>(), queries, null, body, serviceResponseListener);
  }

  public void getTransaction(String uid, String walletAddress, String signature,
      GetTransactionListener getTransactionListener) {

    ServiceResponseListener serviceResponseListener =
        adyenListenerProvider.createGetTransactionListener(getTransactionListener);

    List<String> path = new ArrayList<>();
    path.add(uid);

    Map<String, String> queries = new HashMap<>();
    queries.put("wallet.address", walletAddress);
    queries.put("wallet.signature", signature);

    bdsService.makeRequest("8.20191202/gateways/adyen_v2/transactions", "GET", path, queries, null,
        null, serviceResponseListener);
  }

  public void submitRedirect(String uid, String walletAddress, Object details, String data,
      final MakePaymentListener makePaymentListener) {
    ServiceResponseListener serviceResponseListener =
        adyenListenerProvider.createSubmitRedirectListener(makePaymentListener);

    List<String> path = new ArrayList<>();
    path.add(uid);

    Map<String, String> queries = new LinkedHashMap<>();
    queries.put("wallet.address", walletAddress);

    Map<String, Object> body = new LinkedHashMap<>();
    putIfNotNull(body, "payment.details", details.toString());
    putIfNotNull(body, "payment.data", data);

    bdsService.makeRequest("8.20191202/gateways/adyen_v2/transactions", "PATCH", path, queries,
        null, body, serviceResponseListener);
  }

  public void disablePayments(String walletAddress,
      final NoInfoResponseListener noInfoResponseListener) {
    ServiceResponseListener serviceResponseListener =
        adyenListenerProvider.createDisablePaymentsListener(noInfoResponseListener);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("wallet.address", walletAddress);

    bdsService.makeRequest("8.20191202/gateways/adyen_v2/disable-recurring", "POST", null, null,
        null, body, serviceResponseListener);
  }

  public void cancelRequests() {
    bdsService.cancelRequests();
  }

  private Map<String, Object> buildMakePaymentBody(AdyenPaymentParams adyenPaymentParams,
      TransactionInformation transactionInformation, TransactionWallets transactionWallets) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("payment.method", adyenPaymentParams.getCardPaymentMethod());
    body.put("price.currency", transactionInformation.getCurrency());
    putIfNotNull(body, "domain", transactionInformation.getPackageName());
    String metadata = parseMetaData(transactionInformation.getMetadata());
    putIfNotNull(body, "metadata", metadata);
    body.put("method", transactionInformation.getPaymentType());
    putIfNotNull(body, "origin", transactionInformation.getOrigin());
    putIfNotNull(body, "reference", transactionInformation.getReference());
    putIfNotNull(body, "payment.return_url", adyenPaymentParams.getReturnUrl());
    body.put("payment.store_method", adyenPaymentParams.shouldStorePaymentMethod());
    putIfNotNull(body, "product", transactionInformation.getSku());
    body.put("type", transactionInformation.getTransactionType()
        .toUpperCase());
    body.put("price.value", transactionInformation.getValue());
    putIfNotNull(body, "wallets.oem", transactionWallets.getOemWalletAddress());
    putIfNotNull(body, "wallets.store", transactionWallets.getStoreWalletAddress());
    putIfNotNull(body, "wallets.user", transactionWallets.getUserWalletAddress());
    putIfNotNull(body, "wallets.developer", transactionWallets.getDeveloperWalletAddress());
    putIfNotNull(body, "callback_url", transactionInformation.getCallbackUrl());
    return body;
  }

  private String parseMetaData(String metadata) {
    String parsedMetadata = metadata;
    if (metadata != null && (metadata.contains("{") || metadata.contains("["))) {
      parsedMetadata = metadata.replace("\"", "\\" + "\"");
      parsedMetadata = "\"" + parsedMetadata + "\"";
      parsedMetadata = parsedMetadata.replace("\n", "")
          .replace("\t", "");
    }
    return parsedMetadata;
  }

  private void putIfNotNull(Map<String, Object> map, String key, String value) {
    if (value != null) {
      map.put(key, value);
    }
  }
}
