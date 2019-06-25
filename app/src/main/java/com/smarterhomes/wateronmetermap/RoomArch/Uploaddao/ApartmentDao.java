package com.smarterhomes.wateronmetermap.RoomArch.Uploaddao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.smarterhomes.wateronmetermap.RoomArch.model.ApartmentData;
import com.smarterhomes.wateronmetermap.RoomArch.model.MeterPointData;
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vikhyat on 13/6/19.
 */
@Dao
public interface ApartmentDao {
    @Query("SELECT * FROM ApartmentData")
    List<ApartmentData> getAll();

    @Query("DELETE FROM ApartmentData")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void SaveApartmentData(ApartmentData apartmentData);

    @Query("SELECT * FROM ApartmentData WHERE societyIdApp LIKE :socID")
    List<ApartmentData> getApartmentsOfSociety(Integer socID);

    @Query("SELECT COUNT(*) FROM ApartmentData")
    public int hasData();
}
