/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.uploader.security;

import org.trustedanalytics.uploader.client.UserManagementClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.UUID;
import java.util.function.Function;

public class OrgPermissionVerifier implements PermissionVerifier {

    private final UserManagementClient userManagementClient;
    private final Function<Authentication, String> tokenExtractor;

    @Autowired
    public OrgPermissionVerifier(UserManagementClient userManagementClient,
        Function<Authentication, String> tokenExtractor) {
        this.userManagementClient = userManagementClient;
        this.tokenExtractor = tokenExtractor;
    }

    @Override
    public boolean isOrgAccessible(UUID orgGuid, Authentication auth) {
        return userManagementClient.getPermissions("bearer " + tokenExtractor.apply(auth))
            .stream()
            .map(ccOrgPermission -> ccOrgPermission.getOrganization().getMetadata().getGuid())
            .filter(orgGuid::equals)
            .findFirst()
            .isPresent();
    }
}
