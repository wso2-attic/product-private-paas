package org.apache.stratos.manager.dto;

import java.util.List;


public class Persistence {
    private boolean isRequired;
    private List<Volume> volume;

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public List<Volume> getVolume() {
        return volume;
    }

    public void setVolume(List<Volume> volume) {
        this.volume = volume;
    }
}
