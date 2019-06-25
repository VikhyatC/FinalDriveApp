package com.smarterhomes.wateronmetermap.RoomArch.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

/**
 * Created by vikhyat on 13/6/19.
 */

@Entity
public class ApartmentData {
    @PrimaryKey
    @Nonnull
    private int aptId;

    @ColumnInfo(name="societyIdApp")
    private int socId;

    @ColumnInfo(name ="apartmentNameApp")
    private String aptName;

    public int getSocId() {
        return socId;
    }

    public void setSocId(int socId) {
        this.socId = socId;
    }

    @Nonnull
    public int getAptId() {
        return aptId;
    }

    public void setAptId(@Nonnull int aptId) {
        this.aptId = aptId;
    }

    public String getAptName() {
        return aptName;
    }

    public void setAptName(String aptName) {
        this.aptName = aptName;
    }
}
