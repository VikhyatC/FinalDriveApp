package com.smarterhomes.wateronmetermap.RoomArch.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by vikhyat on 10/6/19.
 */

@Entity
public class SocietyData{
    @PrimaryKey
    @Nonnull
    private int societyId;


    @ColumnInfo(name="societyNameApp")
    private String SocietyName;

    @Nonnull
    public int getSocietyId() {
        return societyId;
    }

    public void setSocietyId(@Nonnull int societyId) {
        this.societyId = societyId;
    }

    public String getSocietyName() {
        return SocietyName;
    }

    public void setSocietyName(String societyName) {
        SocietyName = societyName;
    }


}
