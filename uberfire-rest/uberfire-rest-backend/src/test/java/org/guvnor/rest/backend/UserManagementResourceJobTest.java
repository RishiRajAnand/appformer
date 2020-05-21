package org.guvnor.rest.backend;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.NewGroup;
import org.guvnor.rest.client.UpdateSettingRequest;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.soup.commons.util.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.security.management.api.service.GroupManagerService;
import org.uberfire.ext.security.management.api.service.RoleManagerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserManagementResourceJobTest {

    @Mock
    GroupManagerService groupManagerService;

    @Mock
    private RoleManagerService roleManagerService;

    @Mock
    private JobRequestScheduler jobRequestObserver;

    @Mock
    private JobResultManager jobManager;

    @Captor
    private ArgumentCaptor<JobResult> jobResultArgumentCaptor;

    @InjectMocks
    UserManagementResource userManagementResource = new UserManagementResource() {
        protected Variant getDefaultVariant() {
            return null;
        }

        protected void assertObjectExists(final Object o,
                                          final String objectInfo,
                                          final String objectName) {

        }

        protected Response createAcceptedStatusResponse(final JobRequest jobRequest) {
            return null;
        }
    };

    @Test
    public void getAllGroupTest() throws Exception {
        when(groupManagerService.getAll()).thenReturn(new Lists.Builder<Group>()
                                                              .add(new GroupImpl("testGroup"))
                                                              .build());
        assertThat(userManagementResource.getGroups()).isNotNull();
    }

    @Test
    public void createGroupTest() throws Exception {

        userManagementResource.createGroup(new NewGroup());

        verify(jobManager).putJob(jobResultArgumentCaptor.capture());
        assertEquals(JobStatus.ACCEPTED, jobResultArgumentCaptor.getValue().getStatus());
    }

    @Test
    public void removeGroupTest() throws Exception {

        userManagementResource.deleteGroup("testGroup");

        verify(jobManager).putJob(jobResultArgumentCaptor.capture());
        assertEquals(JobStatus.ACCEPTED, jobResultArgumentCaptor.getValue().getStatus());
    }

    @Test
    public void updateGroupSettingTest() throws Exception {

        userManagementResource.updateGroupPermissions("groupName", new UpdateSettingRequest());

        verify(jobManager).putJob(jobResultArgumentCaptor.capture());
        assertEquals(JobStatus.ACCEPTED, jobResultArgumentCaptor.getValue().getStatus());
    }

    @Test
    public void getAllRoleTest() throws Exception {
        when(roleManagerService.getAll()).thenReturn(new Lists.Builder<Role>()
                                                              .add(new RoleImpl("testRole"))
                                                              .build());
        assertThat(userManagementResource.getRoles()).isNotNull();
    }

    @Test
    public void updateRoleSettingTest() throws Exception {

        userManagementResource.updateRolePermissions("roleName", new UpdateSettingRequest());

        verify(jobManager).putJob(jobResultArgumentCaptor.capture());
        assertEquals(JobStatus.ACCEPTED, jobResultArgumentCaptor.getValue().getStatus());
    }
}
