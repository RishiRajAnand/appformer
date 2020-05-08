/*
 *
 *   Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.guvnor.rest.backend;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Repository;
import org.kie.soup.commons.util.Maps;
import org.uberfire.annotations.Customizable;
import org.uberfire.workbench.model.AppFormerActivities;
import org.uberfire.security.ResourceAction;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;

import static org.guvnor.structure.security.RepositoryAction.BUILD;
import static org.guvnor.structure.security.RepositoryAction.CREATE;
import static org.guvnor.structure.security.RepositoryAction.DELETE;
import static org.guvnor.structure.security.RepositoryAction.UPDATE;
import static org.uberfire.security.ResourceAction.READ;

/**
 * Utility class containing functions to help resolving permissions on specific resources
 */

@ApplicationScoped
public class ResourceTypePermissionValidator {

    private OrganizationalUnitService organizationalUnitService;

    private WorkspaceProjectService projectService;

    private AppFormerActivities appFormerActivities;

    private Map<ResourceType, List<ResourceAction>> permissionMap = new Maps.Builder<ResourceType, List<ResourceAction>>()
            .put(OrganizationalUnit.RESOURCE_TYPE, Arrays.asList(READ, UPDATE, DELETE, CREATE))
            .put(Repository.RESOURCE_TYPE, Arrays.asList(READ, CREATE, UPDATE, DELETE, BUILD))
            .put(ActivityResourceType.PERSPECTIVE, Arrays.asList(READ, UPDATE, DELETE, CREATE))
            .put(ActivityResourceType.EDITOR, Arrays.asList(READ))
            .build();

    public ResourceTypePermissionValidator() {
    }

    @Inject
    public ResourceTypePermissionValidator(@Customizable final AppFormerActivities appFormerActivities, final OrganizationalUnitService organizationalUnitService, final WorkspaceProjectService projectService) {
        this.appFormerActivities = appFormerActivities;
        this.organizationalUnitService = organizationalUnitService;
        this.projectService = projectService;
    }

    public boolean isPermissionAllowed(ResourceType resourceType, ResourceAction permissionType) {
        return permissionMap.get(resourceType).contains(permissionType);
    }

    public boolean isValidResourceType(ResourceType resourceType, String resourceId) {
        if (resourceType.equals(ActivityResourceType.PERSPECTIVE) &&
                appFormerActivities.getAllPerpectivesIds().contains(resourceId)) {
            return true;
        } else if (resourceType.equals(ActivityResourceType.EDITOR) &&
                appFormerActivities.getAllEditorIds().contains(resourceId)) {
            return true;
        } else if (resourceType.equals(OrganizationalUnit.RESOURCE_TYPE)) {
            return organizationalUnitService.getOrganizationalUnits().stream()
                    .anyMatch(orgUnit -> resourceId.equals(orgUnit.getName()));
        } else if (resourceType.equals(Repository.RESOURCE_TYPE)) {
            return projectService.getAllWorkspaceProjects().stream()
                    .anyMatch(project -> resourceId.equals(project.getName()));
        }
        return false;
    }
}
