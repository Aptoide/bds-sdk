package com.appcoins.sdk.billing.payasguest;

import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.service.RequestResponse;
import com.appcoins.sdk.billing.service.Service;
import com.appcoins.sdk.billing.service.ServiceResponseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.appcoins.sdk.billing.payasguest.AdyenPaymentInteract.AddressListener;

public class WalletAddressService {

  private final String defaultStoreAddress;
  private final String defaultOemAddress;
  private Service service;

  public WalletAddressService(Service service) {
    this.service = service;
    defaultStoreAddress = BuildConfig.DEFAULT_STORE_ADDRESS;
    defaultOemAddress = BuildConfig.DEFAULT_OEM_ADDRESS;
  }

  String getDefaultStoreAddress() {
    return defaultStoreAddress;
  }

  String getDefaultOemAddress() {
    return defaultOemAddress;
  }

  void getStoreAddressForPackage(String packageName, String manufacturer, String model,
      String storeId, final AddressListener addressListener) {
    Map<String, String> queries = new HashMap<>();
    queries.put("package.name", packageName);
    queries.put("device.manufacturer", manufacturer);
    queries.put("device.model", model);
    queries.put("oemid", storeId);
    ServiceResponseListener serviceResponseListener = new ServiceResponseListener() {
      @Override public void onResponseReceived(RequestResponse requestResponse) {
        AddressResponseMapper addressResponseMapper = new AddressResponseMapper();
        AddressModel addressModel = addressResponseMapper.map(requestResponse, defaultStoreAddress);
        addressListener.onResponse(addressModel);
      }
    };
    service.makeRequest("/roles/8.20180518/stores", "GET", new ArrayList<String>(), queries,
        new HashMap<String, String>(), new HashMap<String, Object>(), serviceResponseListener);
  }

  void getOemAddressForPackage(String packageName, String manufacturer, String model,
      String storeId, final AddressListener addressListener) {
    Map<String, String> queries = new HashMap<>();
    queries.put("package.name", packageName);
    queries.put("device.manufacturer", manufacturer);
    queries.put("device.model", model);
    queries.put("oemid", storeId);
    ServiceResponseListener serviceResponseListener = new ServiceResponseListener() {
      @Override public void onResponseReceived(RequestResponse requestResponse) {
        AddressResponseMapper addressResponseMapper = new AddressResponseMapper();
        AddressModel addressModel = addressResponseMapper.map(requestResponse, defaultOemAddress);
        addressListener.onResponse(addressModel);
      }
    };
    service.makeRequest("/roles/8.20180518/oems", "GET", new ArrayList<String>(), queries,
        new HashMap<String, String>(), new HashMap<String, Object>(), serviceResponseListener);
  }
}
