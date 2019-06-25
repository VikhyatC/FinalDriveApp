package com.smarterhomes.wateronmetermap.RoomArch.Uploaddao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

import java.util.List;

/**
 * Created by vikhyat on 10/6/19.
 */

@Dao
public interface UploadDao {
    @Query("SELECT * FROM UploadData")
    List<UploadData> getAll();

    @Query("DELETE FROM UploadData")
    void deleteAll();

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    @Insert
    public void SaveUploadedData(UploadData uploadData);

}
