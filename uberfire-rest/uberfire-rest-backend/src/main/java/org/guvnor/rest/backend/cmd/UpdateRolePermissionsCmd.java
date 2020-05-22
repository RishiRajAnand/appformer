package org.guvnor.rest.backend.cmd;

import java.util.Map;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.backend.JobResultManager;
import org.guvnor.rest.backend.UserManagementJobRequestHelper;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.UpdateRolePermissionJobRequest;

public class UpdateRolePermissionsCmd extends AbstractJobCommand {

    public UpdateRolePermissionsCmd(JobRequestHelper jobRequestHelper, JobResultManager jobResultManager, Map<String, Object> context) {
        super(jobRequestHelper, jobResultManager, context);
    }

    @Override
    protected JobResult internalExecute(JobRequest request) throws Exception {
        UserManagementJobRequestHelper helper = (UserManagementJobRequestHelper) getHelper();
        UpdateRolePermissionJobRequest jobRequest = (UpdateRolePermissionJobRequest) request;

        JobResult result = null;
        try {
            result = helper.updateRolePermissions(jobRequest.getJobId(),
                                                  jobRequest.getRoleName(),
                                                  jobRequest.getPermissionsRequest());
        } finally {
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;

            logger.debug("-----updateRolePermissions--- , Role name: {} [{}]",
                         jobRequest.getRoleName(),
                         status);
        }
        return result;
    }
}
