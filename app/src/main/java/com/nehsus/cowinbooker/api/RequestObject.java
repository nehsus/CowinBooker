package com.nehsus.cowinbooker.api;

/**
 * RequestObjects is the enum that is used to differentiate between API methods.
 *
 * @author Sushen Kumar
 */
public enum RequestObject {
    AUTH,
    METADATA,
    APPOINTMENT;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    RequestObject() {
    }
}
