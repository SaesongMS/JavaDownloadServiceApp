package com.example.lab3;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class StatusInfo implements Parcelable {
    private int downloadedBytes;
    private int fileSize;
    private String status;
    private int progress;

    public StatusInfo(int downloadedBytes, int fileSize, String status, int progress){
        this.downloadedBytes = downloadedBytes;
        this.fileSize = fileSize;
        this.status = status;
        this.progress = progress;
    }

    public StatusInfo(Parcel parcel){
        this.downloadedBytes = parcel.readInt();
        this.fileSize = parcel.readInt();
        this.status = parcel.readString();
        this.progress = parcel.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(downloadedBytes);
        parcel.writeInt(fileSize);
        parcel.writeString(status);
        parcel.writeInt(progress);
    }

    public static final Parcelable.Creator<StatusInfo> CREATOR = new Parcelable.Creator<StatusInfo>(){
        @Override
        public StatusInfo createFromParcel(Parcel parcel){
            return new StatusInfo(parcel);
        }

        @Override
        public StatusInfo[] newArray(int size){
            return new StatusInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }
}
