package me.ddggdd135.slimeae.api.wrappers;

public class HikariObject {
    protected Object handle;

    public HikariObject(Object handle) {
        this.handle = handle;
    }

    public Object getHandle() {
        return handle;
    }
}
