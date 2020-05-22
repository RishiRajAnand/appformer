package org.guvnor.rest.backend.cmd;

import java.util.Map;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.backend.JobResultManager;
import org.guvnor.rest.backend.UserManagementJobRequestHelper;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.UpdateGroupPermissionJobRequest;

public class UpdateGroupPermissionsCmd extends AbstractJobCommand {

    public UpdateGroupPermissionsCmd(JobRequestHelper jobRequestHelper, JobResultManager jobResultManager, Map<String, Object> context) {
        super(jobRequestHelper, jobResultManager, context);
    }

    @Override
    protected JobResult internalExecute(JobRequest request) throws Exception {
        UserManagementJobRequestHelper helper = (UserManagementJobRequestHelper) getHelper();
        UpdateGroupPermissionJobRequest jobRequest = (UpdateGroupPermissionJobRequest) request;

        JobResult result = null;
        try {
            result = helper.updateGroupPermissions(jobRequest.getJobId(),
                                                   jobRequest.getGroupName(),
                                                   jobRequest.getPermissionsRequest());
        } finally {
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;

            logger.debug("-----updateGroupPermissions--- , Group name: {} [{}]",
                         jobRequest.getGroupName(),
                         status);
        }
        return result;
    }
}
