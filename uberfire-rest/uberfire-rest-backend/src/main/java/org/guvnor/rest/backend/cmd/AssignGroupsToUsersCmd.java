/*
 *
 *   Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.guvnor.rest.backend.cmd;

import java.util.Map;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.backend.JobResultManager;
import org.guvnor.rest.backend.UserManagementJobRequestHelper;
import org.guvnor.rest.client.AssignGroupsToUserJobRequest;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;

public class AssignGroupsToUsersCmd extends AbstractJobCommand {

    public AssignGroupsToUsersCmd(JobRequestHelper jobRequestHelper, JobResultManager jobResultManager, Map<String, Object> context) {
        super(jobRequestHelper, jobResultManager, context);
    }

    @Override
    protected JobResult internalExecute(JobRequest request) throws Exception {
        UserManagementJobRequestHelper helper = (UserManagementJobRequestHelper) getHelper();
        AssignGroupsToUserJobRequest jobRequest = (AssignGroupsToUserJobRequest) request;

        JobResult result = null;
        try {
            result = helper.assignGroupsToUser(jobRequest.getJobId(),
                                               jobRequest.getUserName(),
                                               jobRequest.getGroups());
        } finally {
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;

            logger.debug("-----assignGroupsToUser--- , User name: {} [{}]",
                         jobRequest.getUserName(),
                         status);
        }
        return result;
    }
}
