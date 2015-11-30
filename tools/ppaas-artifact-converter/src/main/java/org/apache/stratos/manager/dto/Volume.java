package org.apache.stratos.manager.dto;

/**
 * Created by nishadi on 11/24/15.
 */
public class Volume {
    private String device;
    private String mappingPath;
    private int size;
    private boolean removeOnTermination;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getMappingPath() {
        return mappingPath;
    }

    public void setMappingPath(String mappingPath) {
        this.mappingPath = mappingPath;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isRemoveOnTermination() {
        return removeOnTermination;
    }

    public void setRemoveOnTermination(boolean removeOnTermination) {
        this.removeOnTermination = removeOnTermination;
    }
}
