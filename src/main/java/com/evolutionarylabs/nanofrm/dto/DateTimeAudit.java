package com.evolutionarylabs.nanofrm.dto;

import java.util.Date;

/**
 * Created by cleberzanella on 02/05/17.
 */
public class DateTimeAudit {

    private Date createdOn;
    private Date updatedOn;
    private Date removedOn;

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Date getRemovedOn() {
        return removedOn;
    }

    public void setRemovedOn(Date removedOn) {
        this.removedOn = removedOn;
    }

}
