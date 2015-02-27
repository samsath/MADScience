package work.ma.cardboardtest;

/**
 * Created by sam on 20/02/15.
 */
public class Models {
    private String name;
    private int id;
    private int x;
    private int y;
    private int z;
    private String object;
    private String message;
    private String File;


    public Models(String name, int id, int x, int y, int z, String object, String message, String file) {
        this.name = name;
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.object = object;
        this.message = message;
        File = file;
    }

    public Models(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFile() {
        return File;
    }

    public void setFile(String file) {
        File = file;
    }

    @Override
    public String toString() {
        return "Models{" +
                "name='" + name + '\'' +
                ", x=" + x +
                ", z=" + z +
                ", y=" + y +
                '}';
    }
}
