package org.guvnor.rest.client;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class UpdateGroupPermissionJobRequest extends JobRequest{

    private UpdatePermissionsRequest permissionsRequest;
    private String groupName;

    public UpdatePermissionsRequest getPermissionsRequest() {
        return permissionsRequest;
    }

    public void setPermissionsRequest(UpdatePermissionsRequest permissionsRequest) {
        this.permissionsRequest = permissionsRequest;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
