/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.jobmanager.core.internal;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.impl.ResourceManagerApiServiceImpl;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;

import java.util.Map;

public class ResourceNodeMonitor implements Runnable {

    private static final Logger LOG = Logger.getLogger(ResourceNodeMonitor.class);

    @Override
    public void run() {
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        Map<String, ResourceNode> resourceNodeMap = resourcePool.getResourceNodeMap();
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        if (resourceNodeMap != null) {
            long currentTimestamp = System.currentTimeMillis();
            resourceNodeMap.values().forEach(resourceNode -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Current timestamp: " + currentTimestamp);
                    LOG.debug("Resource Node Last ping timestamp: " + resourceNode.getLastPingTimestamp());
                    LOG.debug("Time difference " + (currentTimestamp - resourceNode.getLastPingTimestamp()));
                    LOG.debug("Failed attempts " + resourceNode.getFailedPingAttempts());
                }
                if (currentTimestamp - resourceNode.getLastPingTimestamp() >= deploymentConfig.
                        getHeartbeatInterval() * 2) {
                    resourceNode.incrementFailedPingAttempts();
                    if (resourceNode.getFailedPingAttempts() > deploymentConfig.getHeartbeatMaxRetry()) {
                        resourcePool.removeResourceNode(resourceNode.getId());
                    }
                } else {
                    resourceNode.resetFailedPingAttempts();
                }
            });
        }
    }
}
