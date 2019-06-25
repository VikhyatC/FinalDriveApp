package com.smarterhomes.wateronmetermap.RoomArch.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

/**
 * Created by vikhyat on 13/6/19.
 */

@Entity
public class MeterPointData {
    @PrimaryKey
    @Nonnull
    private int mptId;

    @ColumnInfo(name ="meter_number")
    private String mptName;

    @ColumnInfo(name="apartment_Id")
    private int aptId;

    public int getAptId() {
        return aptId;
    }

    public void setAptId(int aptId) {
        this.aptId = aptId;
    }

    @Nonnull
    public int getMptId() {
        return mptId;
    }

    public void setMptId(@Nonnull int mptId) {
        this.mptId = mptId;
    }

    public String getMptName() {
        return mptName;
    }

    public void setMptName(String mptName) {
        this.mptName = mptName;
    }

}
