package org.guvnor.rest.backend.cmd;

import java.util.Map;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.backend.JobResultManager;
import org.guvnor.rest.backend.UserManagementJobRequestHelper;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.RemoveGroupRequest;

public class RemoveGroupCmd extends AbstractJobCommand {

    public RemoveGroupCmd(final JobRequestHelper jobRequestHelper,
                          final JobResultManager jobResultManager,
                          final Map<String, Object> context) {
        super(jobRequestHelper,
              jobResultManager,
              context);
    }

    @Override
    public JobResult internalExecute(JobRequest request) throws Exception {
        UserManagementJobRequestHelper helper = (UserManagementJobRequestHelper) getHelper();
        RemoveGroupRequest jobRequest = (RemoveGroupRequest) request;

        JobResult result = null;
        try {
            result = helper.removeGroup(jobRequest.getJobId(),
                                        jobRequest.getGroupName());
        } finally {
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;
            logger.debug("-----removeGroup--- , Group name: {}",
                         jobRequest.getGroupName(),
                         status);
        }
        return result;
    }
}
