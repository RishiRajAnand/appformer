/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.rest.backend;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.guvnor.rest.client.NewUser;
import org.guvnor.rest.client.Permission;
import org.guvnor.rest.client.PermissionException;
import org.guvnor.rest.client.UpdateSettingRequest;
import org.guvnor.rest.client.WorkbenchPermission;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.annotations.Customizable;
import org.uberfire.backend.authz.AuthorizationService;
import org.uberfire.ext.security.management.api.RestWorkbenchEnties;
import org.uberfire.ext.security.management.api.exception.GroupNotFoundException;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;
import org.uberfire.ext.security.management.api.service.UserManagerService;
import org.uberfire.security.ResourceType;
import org.uberfire.security.authz.AuthorizationPolicy;
import org.uberfire.security.authz.PermissionCollection;
import org.uberfire.security.authz.PermissionManager;
import org.uberfire.workbench.model.ActivityResourceType;

import static org.guvnor.structure.security.RepositoryAction.BUILD;
import static org.guvnor.structure.security.RepositoryAction.CREATE;
import static org.guvnor.structure.security.RepositoryAction.DELETE;
import static org.guvnor.structure.security.RepositoryAction.UPDATE;
import static org.uberfire.security.ResourceAction.READ;

@ApplicationScoped
public class UserManagementResourceHelper implements JobRequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementJobRequestHelper.class);

    private static final String EDIT_GLOBAL_PREFERENCES = "globalpreferences.edit";
    private static final String GUIDED_DECISION_TABLE_EDIT_COLUMNS = "guideddecisiontable.edit.columns";
    private static final String EDIT_PROFILE_PREFERENCES = "profilepreferences.edit";
    private static final String ACCESS_DATA_TRANSFER = "datatransfer.access";
    private static final String EDIT_SOURCES = "dataobject.edit";
    private static final String JAR_DOWNLOAD = "jar.download";
    private static final String PLANNER_AVAILABLE = "planner.available";

    @Inject
    private GroupManagerService groupManagerService;

    @Inject
    private UserManagerService userManagerService;

    @Inject
    private RoleManagerService roleManagerService;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private PermissionManager permissionManager;

    @Inject
    @Customizable
    private RestWorkbenchEnties restWorkbenchEnties;

    public Response createGroup(final String groupName,
                                final List<String> users) {
        List<String> party = restWorkbenchEnties.getAllEditorId();

        if (groupName == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Group name cannot be empty")
                    .build();
        }
        Group group;
        try {
            group = groupManagerService.get(groupName);
            if (group != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Group with name " + groupName + " already exists")
                        .build();
            } else {
                group = groupManagerService.create(new GroupImpl(groupName));
                groupManagerService.assignUsers(groupName, users);
                if (group != null) {
                    return Response.status(Response.Status.OK)
                            .entity("Group " + group.getName() + " is created successfully.")
                            .build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .build();
                }
            }
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to create '" + groupName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response createUser(NewUser newUser) {
        if (newUser.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User name cannot be empty")
                    .build();
        }

        try {
            User user;
            user = userManagerService.get(newUser.getName());
            if (user != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("User with name " + newUser.getName() + " already exists")
                        .build();
            } else {
                user = createUserObject(newUser);
                User userCreated = userManagerService.create(user);
                if (userCreated != null) {
                    return Response.status(Response.Status.OK)
                            .entity("User " + userCreated.getIdentifier() + " is created successfully.")
                            .build();
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .build();
                }
            }
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to create '" + newUser.getName() + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response removeGroup(final String groupName) {
        if (groupName == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Group name must be provided")
                    .build();
        }
        try {
            groupManagerService.delete(groupName);
            return Response.status(Response.Status.OK)
                    .entity("Group " + groupName + " is deleted successfully.")
                    .build();
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to remove '" + groupName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response assignGroupsToUser(final String userName,
                                       final List<String> groups) {

        if (userName == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User name cannot be empty")
                    .build();
        }

        try {
            userManagerService.assignGroups(userName, groups);
            return Response.status(Response.Status.OK)
                    .entity("Groups" + groups + " are assigned successfully to user " + userName)
                    .build();
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to assign groups to user  '" + userName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response assignRolesToUser(final String userName,
                                      final List<String> roles) {

        if (userName == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("User name cannot be empty")
                    .build();
        }

        try {
            userManagerService.assignRoles(userName, roles);
            return Response.status(Response.Status.OK)
                    .entity("Roles" + roles + " are assigned successfully to user " + userName)
                    .build();
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to assign roles to user  '" + userName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response updateGroupPermissions(final String groupName,
                                           final UpdateSettingRequest permissionsRequest) {

        if (groupName == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Group name cannot be empty")
                    .build();
        }

        try {
            Group group = groupManagerService.get(groupName);
            AuthorizationPolicy authzPolicy = permissionManager.getAuthorizationPolicy();

            if (permissionsRequest.getHomePage() != null && isValidResourceType(ActivityResourceType.PERSPECTIVE, permissionsRequest.getHomePage())) {
                authzPolicy.setHomePerspective(group, permissionsRequest.getHomePage());
            }
            if (permissionsRequest.getPriority() != null) {
                authzPolicy.setPriority(group, permissionsRequest.getPriority());
            }

            PermissionCollection pc = authzPolicy.getPermissions(group);
            generatePermissionCollection(pc, permissionsRequest);
            authzPolicy.setPermissions(group, pc);

            authorizationService.savePolicy(authzPolicy);

            return Response.status(Response.Status.OK)
                    .entity("Group" + groupName + " permissions are updated successfully.")
                    .build();
        } catch (GroupNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Group with name " + groupName + "doesn't exists")
                    .build();
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to update permissions for  '" + groupName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    public Response updateRolePermissions(final String roleName,
                                          final UpdateSettingRequest permissionsRequest) {

        if (roleName == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Role name cannot be empty")
                    .build();
        }

        try {
            Role role = roleManagerService.get(roleName);
            if (role != null) {
                AuthorizationPolicy authzPolicy = permissionManager.getAuthorizationPolicy();
                if (permissionsRequest.getHomePage() != null && isValidResourceType(ActivityResourceType.PERSPECTIVE, permissionsRequest.getHomePage())) {
                    authzPolicy.setHomePerspective(role, permissionsRequest.getHomePage());
                }
                if (permissionsRequest.getPriority() != null) {
                    authzPolicy.setPriority(role, permissionsRequest.getPriority());
                }

                PermissionCollection pc = authzPolicy.getPermissions(role);
                generatePermissionCollection(pc, permissionsRequest);
                authzPolicy.setPermissions(role, pc);

                authorizationService.savePolicy(authzPolicy);

                return Response.status(Response.Status.OK)
                        .entity("Role" + roleName + " permissions are updated successfully.")
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Role with name " + roleName + "doesn't exists")
                        .build();
            }
        } catch (Exception e) {
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to update permissions for  '" + roleName + "': " + e.getMessage();
            logger.error(errMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errMsg)
                    .build();
        }
    }

    private PermissionCollection generatePermissionCollection(PermissionCollection pc, UpdateSettingRequest permissionRequest) {
        if (permissionRequest.getPages() != null) {
            addToCollection(pc, ActivityResourceType.PERSPECTIVE, permissionRequest.getPages());
        }
        if (permissionRequest.getSpaces() != null) {
            addToCollection(pc, OrganizationalUnit.RESOURCE_TYPE, permissionRequest.getSpaces());
        }
        if (permissionRequest.getProject() != null) {
            addToCollection(pc, Repository.RESOURCE_TYPE, permissionRequest.getProject());
        }
        if (permissionRequest.getEditor() != null) {
            addToCollection(pc, ActivityResourceType.EDITOR, permissionRequest.getEditor());
        }
        if (permissionRequest.getWorkbench() != null) {
            addWorkBenchPermissions(pc, permissionRequest.getWorkbench());
        }
        return pc;
    }

    private void addWorkBenchPermissions(PermissionCollection pc, WorkbenchPermission permission) {

        if (permission.getAccessDataTransfer() != null) {
            pc.add(permissionManager.createPermission(ACCESS_DATA_TRANSFER, permission.getAccessDataTransfer()));
        }
        if (permission.getEditDataObject() != null) {
            pc.add(permissionManager.createPermission(EDIT_SOURCES, permission.getEditDataObject()));
        }
        if (permission.getEditGlobalPreferences() != null) {
            pc.add(permissionManager.createPermission(EDIT_GLOBAL_PREFERENCES, permission.getEditGlobalPreferences()));
        }
        if (permission.getEditProfilePreferences() != null) {
            pc.add(permissionManager.createPermission(EDIT_PROFILE_PREFERENCES, permission.getEditProfilePreferences()));
        }
        if (permission.getJarDownload() != null) {
            pc.add(permissionManager.createPermission(JAR_DOWNLOAD, permission.getJarDownload()));
        }
        if (permission.getPlannerAvailable() != null) {
            pc.add(permissionManager.createPermission(PLANNER_AVAILABLE, permission.getPlannerAvailable()));
        }
        if (permission.getEditGuidedDecisionTableColumns() != null) {
            pc.add(permissionManager.createPermission(GUIDED_DECISION_TABLE_EDIT_COLUMNS, permission.getEditGuidedDecisionTableColumns()));
        }
    }

    private void addToCollection(PermissionCollection pc, ResourceType resourceType, Permission permission) {

        if (permission.isRead() != null) {
            pc.add(permissionManager.createPermission(resourceType, READ, permission.isRead()));
        }
        if (permission.isCreate() != null) {
            pc.add(permissionManager.createPermission(resourceType, CREATE, permission.isCreate()));
        }
        if (permission.isUpdate() != null) {
            pc.add(permissionManager.createPermission(resourceType, UPDATE, permission.isUpdate()));
        }
        if (permission.isDelete() != null) {
            pc.add(permissionManager.createPermission(resourceType, DELETE, permission.isDelete()));
        }
        if (permission.isBuild() != null) {
            pc.add(permissionManager.createPermission(resourceType, BUILD, permission.isBuild()));
        }
        if (permission.getExceptions() != null) {
            addExceptions(pc, resourceType, permission.getExceptions());
        }
    }

    private void addExceptions(PermissionCollection pc, ResourceType resourceType, List<PermissionException> exceptions) {
        for (PermissionException exception : exceptions) {
            Permission permission = exception.getPermissions();
            String resourceTypeName = resourceType.getName();
            if (isValidResourceType(resourceType, exception.getResourceName())) {
                if (permission.isRead() != null) {
                    final String permissionName = resourceTypeName + "." + READ.getName() + "." + exception.getResourceName();
                    pc.add(permissionManager.createPermission(permissionName, permission.isRead()));
                }
                if (permission.isCreate() != null) {
                    final String permissionName = resourceTypeName + "." + CREATE.getName() + "." + exception.getResourceName();
                    pc.add(permissionManager.createPermission(permissionName, permission.isCreate()));
                }
                if (permission.isUpdate() != null) {
                    final String permissionName = resourceTypeName + "." + UPDATE.getName() + "." + exception.getResourceName();
                    pc.add(permissionManager.createPermission(permissionName, permission.isUpdate()));
                }
                if (permission.isDelete() != null) {
                    final String permissionName = resourceTypeName + "." + DELETE.getName() + "." + exception.getResourceName();
                    pc.add(permissionManager.createPermission(permissionName, permission.isDelete()));
                }
            }
        }
    }

    private boolean isValidResourceType(ResourceType resourceType, String resourceId) {
        return (resourceType.equals(ActivityResourceType.PERSPECTIVE) && restWorkbenchEnties.getAllPerpective().contains(resourceId)) ||
                (resourceType.equals(ActivityResourceType.EDITOR) && restWorkbenchEnties.getAllEditorId().contains(resourceId));
    }

    private User createUserObject(NewUser newUser) {
        final Collection<Role> userRoles = new HashSet<>();
        final Collection<Group> userGroups = new HashSet<>();
        if (newUser.getName() == null) {
            return null;
        }
        if (newUser.getRoles() != null) {
            for (final String roleName : newUser.getRoles()) {
                Role role = roleManagerService.get(roleName);
                if (role != null) {
                    userRoles.add(role);
                }
            }
        }
        if (newUser.getGroups() != null) {
            for (final String groupName : newUser.getGroups()) {
                Group group = groupManagerService.get(groupName);
                if (group != null) {
                    userGroups.add(group);
                }
            }
        }
        return new UserImpl(newUser.getName(), userRoles, userGroups);
    }
}

