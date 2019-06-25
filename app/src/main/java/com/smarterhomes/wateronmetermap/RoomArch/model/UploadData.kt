package com.smarterhomes.wateronmetermap.RoomArch.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.support.annotation.NonNull
import com.smarterhomes.wateronmetermap.RoomArch.Converters

/**
 * Created by vikhyat on 10/6/19.
 */
@Entity
class UploadData {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id:Long ?= null


    @ColumnInfo(name = "societyId")
    var societyId: String ?=""

    @ColumnInfo(name = "societyName")
    var society_name: String? = ""

    @ColumnInfo(name = "aptId")
    var aptId: String? = ""

    @ColumnInfo(name = "aptName")
    var apt_name: String? = ""

    @ColumnInfo(name = "meterName")
    var meter_name: String? = ""

    @ColumnInfo(name = "meterId")
    var meterId: String? = ""
    /*
    @ColumnInfo(name = "referenceUrl")
    private String locationRefUrl;*/

    /*@ColumnInfo(name = "imgBytes")
    @TypeConverters(Converters::class)
    var imgBytes: Array<Byte>? = null*/

    @ColumnInfo(name = "imgUrls")
    var imgUrls: String? = null

    @ColumnInfo(name = "state")
    var state: String? = ""

    /*    public String getLocationRefUrl() {
        return locationRefUrl;
    }

    public void setLocationRefUrl(String locationRefUrl) {
        this.locationRefUrl = locationRefUrl;
    }*/
}

