package at.aau.nes.mjoelnir.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by a.monacchi on 21.04.2016.
 */
public class Device implements Parcelable{
    public String id;
    public String name;
    public String credit;
    public boolean connectionStatus;
    public boolean controlStatus;

    public Device(String id, String name, String credit, boolean connectionStatus, boolean controlStatus){
        this.id=id;
        this.name=name;
        this.credit=credit;
        this.connectionStatus = connectionStatus;
        this.controlStatus = controlStatus;
    }

    protected Device(Parcel in) {
        id = in.readString();
        name = in.readString();
        credit = in.readString();
        connectionStatus = in.readByte() != 0;
        controlStatus = in.readByte() != 0;
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(credit);
        dest.writeByte((byte) (connectionStatus ? 1 : 0));
        dest.writeByte((byte) (controlStatus ? 1 : 0));
    }
}
