package org.guvnor.rest.client;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Permission {

    private Boolean read;
    private Boolean create;
    private Boolean update;
    private Boolean delete;
    private Boolean build;

    private List<PermissionException> exceptions;

    public Permission() {
    }

    public Permission(@MapsTo("read") Boolean read, @MapsTo("create") Boolean create,
                      @MapsTo("update") Boolean update, @MapsTo("delete") Boolean delete,
                      @MapsTo("build") Boolean build, @MapsTo("exceptions") List<PermissionException> exceptions) {
        this.read = read;
        this.create = create;
        this.update = update;
        this.delete = delete;
        this.build = build;
        this.exceptions = exceptions;
    }

    public Boolean isRead() {
        return read;
    }

    public Boolean isCreate() {
        return create;
    }

    public Boolean isUpdate() {
        return update;
    }

    public Boolean isDelete() {
        return delete;
    }

    public Boolean isBuild() {
        return build;
    }

    public List<PermissionException> getExceptions() {
        return exceptions;
    }
}
