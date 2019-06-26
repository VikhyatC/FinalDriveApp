package com.smarterhomes.wateronmetermap.RoomArch.Uploaddao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.smarterhomes.wateronmetermap.RoomArch.model.SocietyData;
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

import java.util.List;

/**
 * Created by vikhyat on 13/6/19.
 */

@Dao
public interface SocietyDao {
    @Query("SELECT * FROM SocietyData ORDER BY SocietyData.societyNameApp")
    List<SocietyData> getAll();

    @Query("DELETE FROM SocietyData")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void SaveSocietyData(SocietyData societyData);

    @Query("SELECT COUNT(*) FROM SocietyData")
    public int hasData();


}
