package com.asf.appcoins.sdk.ads.poa.campaign;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class CampaignRepository {
  private final Api api;

  public enum CampaignType{
    BDS
  }

  public CampaignRepository(Api api) {
    this.api = api;
  }

  public Single<CampaignResponse> getCampaign(String packageName, int versionCode,
      String countryCode, CampaignType type) {
    return api.getCampaign(packageName, versionCode, countryCode, "desc", "price", true, type.name());
  }

  public interface Api {
    @GET("campaign/listall") Single<CampaignResponse> getCampaign(
        @Query("packageName") String packageName, @Query("vercode") int versionCode,
        @Query("countryCode") String countryCode, @Query("sort") String sort,
        @Query("by") String by, @Query("valid") boolean valid, @Query("type") String type);
  }
}