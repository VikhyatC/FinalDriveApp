package com.smarterhomes.wateronmetermap.RoomArch.Uploaddao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.smarterhomes.wateronmetermap.RoomArch.model.MeterPointData;
import com.smarterhomes.wateronmetermap.RoomArch.model.SocietyData;
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

import java.util.List;

/**
 * Created by vikhyat on 13/6/19.
 */
@Dao
public interface MeterPointDao {
    @Query("SELECT * FROM MeterPointData")
    List<MeterPointData> getAll();

    @Query("DELETE FROM MeterPointData")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void SaveMeterPointData(MeterPointData meterPointData);

    @Query("SELECT * FROM MeterPointData WHERE apartment_Id LIKE :aptId ORDER BY MeterPointData.meter_number")
    List<MeterPointData> geMetersInApt(Integer aptId);

    @Query("SELECT COUNT(*) FROM MeterPointData")
    public int hasData();
}
