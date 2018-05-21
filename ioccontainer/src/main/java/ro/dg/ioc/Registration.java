package ro.dg.ioc;

import java.util.List;

public class Registration {

    private String type;
    private String mapTo;
    private boolean singleton;

    private List<Constructor> constructorParams;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMapTo(String mapTo) {
        this.mapTo = mapTo;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public String getMapTo() {
        return mapTo;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public List<Constructor> getConstructorParams() {
        return constructorParams;
    }

    public void setConstructorParams(List<Constructor> constructorParams) {
        this.constructorParams = constructorParams;
    }
}
