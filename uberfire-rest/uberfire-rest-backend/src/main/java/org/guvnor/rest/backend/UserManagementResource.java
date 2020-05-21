/*
 * Copyright 2020s Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.rest.backend;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.guvnor.rest.client.CreateGroupRequest;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.NewGroup;
import org.guvnor.rest.client.RemoveGroupRequest;
import org.guvnor.rest.client.UpdateGroupPermissionJobRequest;
import org.guvnor.rest.client.UpdateRolePermissionJobRequest;
import org.guvnor.rest.client.UpdateSettingRequest;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;

import static org.guvnor.rest.backend.PermissionConstants.ADMIN_ROLE;

/**
 * REST services
 */
@Path("/")
@Named
@ApplicationScoped
public class UserManagementResource {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementResource.class);
    private Variant defaultVariant = getDefaultVariant();

    @Inject
    GroupManagerService groupManagerService;

    @Inject
    RoleManagerService roleManagerService;

    @Inject
    private JobRequestScheduler jobRequestObserver;

    @Inject
    private JobResultManager jobManager;
    private AtomicLong counter = new AtomicLong(0);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    @RolesAllowed({ADMIN_ROLE})
    public Collection<Group> getGroups() {
        logger.debug("-----getGroups--- ");

        final List<Group> results = groupManagerService.getAll();
        return results;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    @RolesAllowed({ADMIN_ROLE})
    public Response createGroup(NewGroup group) {
        logger.debug("-----createGroup--- , Group name: {}, User assigned : {}",
                     group.getName(),
                     group.getUsers());

        final String id = newId();
        final CreateGroupRequest jobRequest = new CreateGroupRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setGroupName(group.getName());
        jobRequest.setUsers(group.getUsers());

        addAcceptedJobResult(id);

        jobRequestObserver.createGroupRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups/{groupName}")
    @RolesAllowed({ADMIN_ROLE})
    public Response deleteGroup(@PathParam("groupName") String groupName) {
        logger.debug("-----deleteGroup--- , Group Name: {}",
                     groupName);

        assertObjectExists(groupManagerService.get(groupName),
                           "group",
                           groupName);

        final String id = newId();
        final RemoveGroupRequest jobRequest = new RemoveGroupRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setGroupName(groupName);
        addAcceptedJobResult(id);

        jobRequestObserver.removeGroupRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups/{groupName}/permissions")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateGroupPermissions(@PathParam("groupName") String groupName, UpdateSettingRequest permissionRequest) {
        logger.debug("-----updateGroupPermissions--- , Group name: {}",
                     groupName);

        assertObjectExists(groupManagerService.get(groupName),
                           "group",
                           groupName);

        final String id = newId();
        final UpdateGroupPermissionJobRequest jobRequest = new UpdateGroupPermissionJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setGroupName(groupName);
        jobRequest.setPermissionsRequest(permissionRequest);

        addAcceptedJobResult(id);

        jobRequestObserver.updateGroupPermissionsRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    @RolesAllowed({ADMIN_ROLE})
    public Collection<Role> getRoles() {
        logger.debug("-----getGroups--- ");

        final List<Role> results = roleManagerService.getAll();
        return results;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/roles/{rolesName}/permissions")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateRolePermissions(@PathParam("rolesName") String rolesName, UpdateSettingRequest permissionRequest) {
        logger.debug("-----updateGroupPermissions--- , Role name: {}",
                     rolesName);

        assertObjectExists(roleManagerService.get(rolesName),
                           "role",
                           rolesName);

        final String id = newId();
        final UpdateRolePermissionJobRequest jobRequest = new UpdateRolePermissionJobRequest();
        jobRequest.setStatus(JobStatus.ACCEPTED);
        jobRequest.setJobId(id);
        jobRequest.setRoleName(rolesName);
        jobRequest.setPermissionsRequest(permissionRequest);

        addAcceptedJobResult(id);

        jobRequestObserver.updateRolePermissionsRequest(jobRequest);

        return createAcceptedStatusResponse(jobRequest);
    }

    protected Variant getDefaultVariant() {
        return Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    }

    protected Response createAcceptedStatusResponse(final JobRequest jobRequest) {
        return Response.status(Response.Status.ACCEPTED).entity(jobRequest).variant(defaultVariant).build();
    }

    protected void assertObjectExists(final Object o,
                                      final String objectInfo,
                                      final String objectName) {
        if (o == null) {
            throw new WebApplicationException(String.format("Could not find %s with name %s.", objectInfo, objectName),
                                              Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    private String newId() {
        return "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }

    private void addAcceptedJobResult(String jobId) {
        JobResult jobResult = new JobResult();
        jobResult.setJobId(jobId);
        jobResult.setStatus(JobStatus.ACCEPTED);
        jobManager.putJob(jobResult);
    }
}
