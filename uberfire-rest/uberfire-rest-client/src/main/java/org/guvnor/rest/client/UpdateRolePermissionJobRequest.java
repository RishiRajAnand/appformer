package org.guvnor.rest.client;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class UpdateRolePermissionJobRequest extends JobRequest{

    private UpdateSettingRequest permissionsRequest;
    private String roleName;

    public UpdateSettingRequest getPermissionsRequest() {
        return permissionsRequest;
    }

    public void setPermissionsRequest(UpdateSettingRequest permissionsRequest) {
        this.permissionsRequest = permissionsRequest;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
