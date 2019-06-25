package com.smarterhomes.wateronmetermap.Interfaces;

/**
 * Created by vikhyat on 30/4/19.
 */

public interface InstallerInterface {
    public void ShowSocieties(String response);
    public void ShowApartments(String response);
    public void ErrorfetchingData();
    public void fetchMeteringPoints(String response);

    void UrlPassedToserver(String strResult);
}
