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

import java.util.ArrayList;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.guvnor.rest.client.CreateGroupRequest;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.NewGroup;
import org.guvnor.rest.client.Permission;
import org.guvnor.rest.client.RemoveGroupRequest;
import org.guvnor.rest.client.UpdateGroupPermissionJobRequest;
import org.guvnor.rest.client.UpdatePermissionsRequest;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.authz.AuthorizationService;
import org.uberfire.ext.security.management.api.AbstractEntityManager;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.impl.SearchRequestImpl;
import org.uberfire.security.ResourceAction;
import org.uberfire.security.ResourceType;
import org.uberfire.security.authz.AuthorizationPolicy;
import org.uberfire.security.authz.PermissionCollection;
import org.uberfire.security.authz.PermissionManager;
import org.uberfire.security.impl.authz.DotNamedPermission;

import static org.guvnor.rest.backend.PermissionConstants.REST_PROJECT_ROLE;
import static org.guvnor.rest.backend.PermissionConstants.REST_ROLE;

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
    private JobRequestScheduler jobRequestObserver;

    @Inject
    private JobResultManager jobManager;
    private AtomicLong counter = new AtomicLong(0);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Collection<Group> getGroups() {
        logger.debug("-----getGroups--- ");

        final List<Group> results = new ArrayList<Group>();
        AbstractEntityManager.SearchResponse<Group> response = groupManagerService.search(new SearchRequestImpl("",
                                                                                                                1, 10));
        for (Group group : response.getResults()) {
            results.add(group);
        }

        return results;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
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
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response deleteGroup(@PathParam("groupName") String groupName) {
        logger.debug("-----deleteGroup--- , Group Name: {}",
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
    @RolesAllowed({REST_ROLE, REST_PROJECT_ROLE})
    public Response updateGroupPermissions(@PathParam("groupName") String groupName, UpdatePermissionsRequest permissionRequest) {
        logger.debug("-----updateGroupPermissions--- , Group name: {}",
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

    protected Variant getDefaultVariant() {
        return Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build().get(0);
    }

    protected Response createAcceptedStatusResponse(final JobRequest jobRequest) {
        return Response.status(Response.Status.ACCEPTED).entity(jobRequest).variant(defaultVariant).build();
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
