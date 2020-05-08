package org.guvnor.rest.backend.cmd;

import java.util.Map;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.backend.JobResultManager;
import org.guvnor.rest.client.CreateGroupRequest;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.SpaceRequest;

public class CreateGroupCmd extends AbstractJobCommand{

    public CreateGroupCmd(JobRequestHelper jobRequestHelper, JobResultManager jobResultManager, Map<String, Object> context) {
        super(jobRequestHelper, jobResultManager, context);
    }

    @Override
    public JobResult internalExecute(JobRequest request) throws Exception {
        JobRequestHelper helper = getHelper();
        CreateGroupRequest jobRequest = (CreateGroupRequest) request;

        JobResult result = null;
        try {
            result = helper.createGroup(jobRequest.getJobId(),
                                        jobRequest.getGroupName(),
                                        jobRequest.getUsers());
        } finally {
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;

            logger.debug("-----createGroup--- , Group name: {}, User assigned : {} [{}]",
                         jobRequest.getGroupName(),
                         jobRequest.getUsers(),
                         status);
        }
        return result;
    }
}
