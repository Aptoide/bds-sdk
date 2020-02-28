package com.appcoins.sdk.billing.payasguest;

import android.os.AsyncTask;
import com.appcoins.sdk.billing.listeners.GetTransactionListener;
import com.appcoins.sdk.billing.listeners.LoadPaymentInfoListener;
import com.appcoins.sdk.billing.listeners.MakePaymentListener;
import com.appcoins.sdk.billing.models.AdyenPaymentParams;
import com.appcoins.sdk.billing.models.TransactionInformation;
import com.appcoins.sdk.billing.models.TransactionWallets;
import com.appcoins.sdk.billing.service.adyen.AdyenRepository;
import org.json.JSONObject;

public class AdyenPaymentInteract {

  private AdyenRepository adyenRepository;
  private AddressService addressService;

  public AdyenPaymentInteract(AdyenRepository adyenRepository, AddressService addressService) {
    this.adyenRepository = adyenRepository;
    this.addressService = addressService;
  }

  public void loadPaymentInfo(AdyenRepository.Methods method, String fiatPrice, String fiatCurrency,
      String walletAddress, LoadPaymentInfoListener loadPaymentInfoListener) {
    adyenRepository.loadPaymentInfo(method.getTransactionType(), fiatPrice, fiatCurrency,
        walletAddress, loadPaymentInfoListener);
  }

  void makePayment(final String paymentMethod, final boolean shouldStoreCard,
      final String returnUrl, final String fiatPrice, final String currency,
      final String orderReference, final String paymentType, final String origin,
      final String packageName, final String metadata, final String sku, final String callBackUrl,
      final String transactionType, final String userWalletAddress,
      final MakePaymentListener makePaymentListener) {

    AddressRetrievedListener addressRetrievedListener = new AddressRetrievedListener() {
      @Override public void onAddressRetrieved(String oemAddress, String storeAddress,
          String developerAddress) {
        adyenRepository.makePayment(
            new AdyenPaymentParams(paymentMethod, shouldStoreCard, returnUrl),
            new TransactionInformation(fiatPrice, currency, orderReference, paymentType, origin,
                packageName, metadata, sku, callBackUrl, transactionType),
            new TransactionWallets(userWalletAddress, developerAddress, oemAddress, storeAddress,
                userWalletAddress), makePaymentListener);
      }
    };
    AddressAsyncTask addressAsyncTask =
        new AddressAsyncTask(addressService, addressRetrievedListener, packageName);
    addressAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  void submitRedirect(String uid, String walletAddress, JSONObject details, String paymentData,
      MakePaymentListener makePaymentListener) {
    adyenRepository.submitRedirect(uid, walletAddress, details, paymentData, makePaymentListener);
  }

  void getTransaction(String uid, String walletAddress, String signature,
      GetTransactionListener getTransactionListener) {
    adyenRepository.getTransaction(uid, walletAddress, signature, getTransactionListener);
  }

  public interface AddressListener {

    void onResponse(AddressModel addressModel);
  }
}
