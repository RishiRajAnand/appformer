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

import java.util.Arrays;

import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.UpdateSettingRequest;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.backend.authz.AuthorizationService;
import org.uberfire.ext.security.management.api.RestWorkbenchEnties;
import org.uberfire.ext.security.management.api.exception.GroupNotFoundException;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;
import org.uberfire.security.authz.AuthorizationPolicy;
import org.uberfire.security.authz.PermissionManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementJobRequestHelperTest {

    @InjectMocks
    UserManagementJobRequestHelper helper;
    @Mock
    GroupManagerService groupManagerService;
    @Mock
    private RoleManagerService roleManagerService;
    @Mock
    private RestWorkbenchEnties restWorkbenchEnties;
    @Mock
    AuthorizationService authorizationService;
    @Mock
    PermissionManager permissionManager;

    @Test
    public void testUpdateGroupPermission() {
        when(permissionManager.getAuthorizationPolicy()).thenReturn(mock(AuthorizationPolicy.class));
        when(groupManagerService.get("testGroup")).thenReturn(new GroupImpl("testGroup"));
        UpdateSettingRequest request = mock(UpdateSettingRequest.class);
        JobResult jobResult = helper.updateGroupPermissions(null,
                                                            "testGroup",
                                                            request);
        assertEquals(JobStatus.SUCCESS,
                     jobResult.getStatus());
    }

    @Test
    public void testGroupNotFoundWhenUpdateGroupPermission() {
        doThrow(GroupNotFoundException.class).when(groupManagerService).get("testGroup");
        JobResult jobResult = helper.updateGroupPermissions(null,
                                                            "testGroup",
                                                            mock(UpdateSettingRequest.class));

        assertEquals(JobStatus.BAD_REQUEST,
                     jobResult.getStatus());
    }

    @Test
    public void testCreateGroup() {
        when(groupManagerService.create(new GroupImpl("testGroup"))).thenReturn(mock(Group.class));
        JobResult jobResult = helper.createGroup(null, "testGroup", Arrays.asList("testUser"));

        assertEquals(JobStatus.SUCCESS,
                     jobResult.getStatus());
    }

    @Test
    public void testRemoveGroup() {
        JobResult jobResult = helper.removeGroup(null, "group1");

        assertEquals(JobStatus.SUCCESS,
                     jobResult.getStatus());
    }

    @Test
    public void testUpdateRolePermission() {
        when(permissionManager.getAuthorizationPolicy()).thenReturn(mock(AuthorizationPolicy.class));
        when(roleManagerService.get("testRole")).thenReturn(new RoleImpl("testRole"));
        UpdateSettingRequest request = mock(UpdateSettingRequest.class);
        JobResult jobResult = helper.updateRolePermissions(null,
                                                           "testRole",
                                                           request);
        assertEquals(JobStatus.SUCCESS,
                     jobResult.getStatus());
    }
}
