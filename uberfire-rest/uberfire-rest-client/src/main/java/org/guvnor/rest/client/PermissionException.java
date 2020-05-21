package org.guvnor.rest.client;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class PermissionException {

    private String resourceName;
    private Permission permissions;

    public PermissionException() {
    }

    public PermissionException(@MapsTo("resourceName") String resourceName, @MapsTo("permissions") Permission permissions) {
        this.resourceName = resourceName;
        this.permissions = permissions;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Permission getPermissions() {
        return permissions;
    }
}
