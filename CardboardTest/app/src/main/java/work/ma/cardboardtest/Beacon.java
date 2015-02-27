package work.ma.cardboardtest;

/**
 * Created by sam on 20/02/15.
 */
public class Beacon {
    public int id;
    public String uuid;
    public int major;
    public int minor;
    public int proximity;

    public Beacon(int id, String uuid, int major, int minor,int proximity ){
        this.id = id;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.proximity = proximity;
    }

    public Beacon(String uuid, int major, int minor ){
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public Beacon(String uuid, int major, int minor,int proximity ){
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.proximity = proximity;
    }

    public Beacon(){

    }
}
