package org.guvnor.rest.client;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ResourcePermissionException {

    private String name;
    private Permission permissions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public void setPermissions(Permission permissions) {
        this.permissions = permissions;
    }
}
