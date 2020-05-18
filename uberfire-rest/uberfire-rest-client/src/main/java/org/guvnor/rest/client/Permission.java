package org.guvnor.rest.client;

import java.io.Serializable;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Permission implements Serializable {
    private Boolean read;
    private Boolean create;
    private Boolean update;
    private Boolean delete;

    List<ResourcePermissionException> exceptions;

    public Boolean isRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean isCreate() {
        return create;
    }

    public void setCreate(Boolean write) {
        this.create = write;
    }

    public Boolean isUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Boolean isDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public List<ResourcePermissionException> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<ResourcePermissionException> exceptions) {
        this.exceptions = exceptions;
    }
}
