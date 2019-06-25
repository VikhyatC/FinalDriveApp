package com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.ApartmentDao;
import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.MeterPointDao;
import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.SocietyDao;
import com.smarterhomes.wateronmetermap.RoomArch.model.ApartmentData;
import com.smarterhomes.wateronmetermap.RoomArch.model.MeterPointData;
import com.smarterhomes.wateronmetermap.RoomArch.model.SocietyData;

/**
 * Created by vikhyat on 10/6/19.
 */

@Database(entities = {SocietyData.class, MeterPointData.class, ApartmentData.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {
    public abstract SocietyDao societyDao();
    public abstract MeterPointDao meterPointDao();
    public abstract ApartmentDao apartmentDao();
}
