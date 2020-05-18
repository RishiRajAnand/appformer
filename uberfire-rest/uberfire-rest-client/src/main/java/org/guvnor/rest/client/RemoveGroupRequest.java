package org.guvnor.rest.client;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class RemoveGroupRequest extends JobRequest {

    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
