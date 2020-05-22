package org.guvnor.rest.backend;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.Permission;
import org.guvnor.rest.client.PermissionException;
import org.guvnor.rest.client.UpdateSettingRequest;
import org.guvnor.rest.client.WorkbenchPermission;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.repositories.Repository;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.authz.AuthorizationService;
import org.uberfire.ext.security.management.api.exception.GroupNotFoundException;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;
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
public class UserManagementJobRequestHelper implements JobRequestHelper {

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
    private RoleManagerService roleManagerService;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private PermissionManager permissionManager;

    public JobResult createGroup(final String jobId,
                                 final String groupName,
                                 final List<String> users) {

        JobResult result = new JobResult();
        result.setJobId(jobId);

        if (groupName == null) {
            result.setStatus(JobStatus.BAD_REQUEST);
            result.setResult("Group name cannot be empty");
            return result;
        }
        Group group;
        try {
            group = groupManagerService.get(groupName);
            if (group != null) {
                result.setStatus(JobStatus.BAD_REQUEST);
                result.setResult("Group with name " + groupName + " already exists");
            } else {
                group = groupManagerService.create(new GroupImpl(groupName));
                groupManagerService.assignUsers(groupName, users);
                if (group != null) {
                    result.setResult("Group " + group.getName() + " is created successfully.");
                    result.setStatus(JobStatus.SUCCESS);
                } else {
                    result.setStatus(JobStatus.FAIL);
                }
            }
        } catch (Exception e) {
            result.setStatus(JobStatus.FAIL);
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to create '" + groupName + "': " + e.getMessage();
            result.setResult(errMsg);
            logger.error(errMsg,
                         e);
        }
        return result;
    }

    public JobResult removeGroup(final String jobId,
                                 final String groupName) {
        JobResult result = new JobResult();
        result.setJobId(jobId);

        if (groupName == null) {
            result.setStatus(JobStatus.BAD_REQUEST);
            result.setResult("Group name must be provided");
            return result;
        }

        try {
            groupManagerService.delete(groupName);
            result.setStatus(JobStatus.SUCCESS);
            result.setResult("Group " + groupName + " is deleted successfully.");
        } catch (Exception e) {
            result.setStatus(JobStatus.FAIL);
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to remove '" + groupName + "': " + e.getMessage();
            result.setResult(errMsg);
            logger.error(errMsg,
                         e);
        }

        return result;
    }

    public JobResult updateGroupPermissions(final String jobId,
                                            final String groupName,
                                            final UpdateSettingRequest permissionsRequest) {

        JobResult result = new JobResult();
        result.setJobId(jobId);

        if (groupName == null) {
            result.setStatus(JobStatus.BAD_REQUEST);
            result.setResult("Group name cannot be empty");
            return result;
        }

        try {
            Group group = groupManagerService.get(groupName);
            AuthorizationPolicy authzPolicy = permissionManager.getAuthorizationPolicy();
//        role.admin.permission.project.update=true
            if (permissionsRequest.getHomePage() != null) {
                authzPolicy.setHomePerspective(group, permissionsRequest.getHomePage());
            }
            if (permissionsRequest.getPriority() != null) {
                authzPolicy.setPriority(group, permissionsRequest.getPriority());
            }

            PermissionCollection pc = authzPolicy.getPermissions(group);
            generatePermissionCollection(pc, permissionsRequest);
            authzPolicy.setPermissions(group, pc);

            authorizationService.savePolicy(authzPolicy);

            result.setStatus(JobStatus.SUCCESS);
            result.setResult("Group" + groupName + " permissions are updated successfully.");
        } catch (GroupNotFoundException e) {
            result.setStatus(JobStatus.BAD_REQUEST);
            result.setResult("Group with name " + groupName + "doesn't exists");
        } catch (Exception e) {
            result.setStatus(JobStatus.FAIL);
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to update permissions for  '" + groupName + "': " + e.getMessage();
            result.setResult(errMsg);
            logger.error(errMsg, e);
        }

        return result;
    }

    public JobResult updateRolePermissions(final String jobId,
                                           final String roleName,
                                           final UpdateSettingRequest permissionsRequest) {

        JobResult result = new JobResult();
        result.setJobId(jobId);

        if (roleName == null) {
            result.setStatus(JobStatus.BAD_REQUEST);
            result.setResult("Role name cannot be empty");
            return result;
        }

        try {
            Role role = roleManagerService.get(roleName);
            if (role != null) {
                AuthorizationPolicy authzPolicy = permissionManager.getAuthorizationPolicy();
                if (permissionsRequest.getHomePage() != null) {
                    authzPolicy.setHomePerspective(role, permissionsRequest.getHomePage());
                }
                if (permissionsRequest.getPriority() != null) {
                    authzPolicy.setPriority(role, permissionsRequest.getPriority());
                }

                PermissionCollection pc = authzPolicy.getPermissions(role);
                generatePermissionCollection(pc, permissionsRequest);
                authzPolicy.setPermissions(role, pc);

                authorizationService.savePolicy(authzPolicy);

                result.setStatus(JobStatus.SUCCESS);
                result.setResult("Role" + roleName + " permissions are updated successfully.");
            } else {
                result.setStatus(JobStatus.BAD_REQUEST);
                result.setResult("Role with name " + roleName + "doesn't exists");
            }
        } catch (Exception e) {
            result.setStatus(JobStatus.FAIL);
            String errMsg = e.getClass().getSimpleName() + " thrown when trying to update permissions for  '" + roleName + "': " + e.getMessage();
            result.setResult(errMsg);
            logger.error(errMsg, e);
        }

        return result;
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
