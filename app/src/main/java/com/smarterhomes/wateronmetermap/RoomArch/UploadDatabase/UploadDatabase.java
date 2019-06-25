package com.smarterhomes.wateronmetermap.RoomArch.UploadDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.smarterhomes.wateronmetermap.RoomArch.Uploaddao.UploadDao;
import com.smarterhomes.wateronmetermap.RoomArch.model.UploadData;

/**
 * Created by vikhyat on 10/6/19.
 */

@Database(entities = {UploadData.class}, version = 1)
public abstract class UploadDatabase extends RoomDatabase{
    public abstract UploadDao uploadDao();
}
