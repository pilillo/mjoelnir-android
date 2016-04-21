package at.aau.nes.mjoelnir.model;

/**
 * Created by a.monacchi on 21.04.2016.
 */
public class Device{
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
}
