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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.trustedanalytics.cloud.cc.api.CcOrg;
import org.trustedanalytics.cloud.cc.api.CcOrgPermission;
import org.trustedanalytics.uploader.client.UserManagementClient;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RunWith(Parameterized.class)
public class PermissionVerifierTest {

    private static final UUID deniedOrg = UUID.randomUUID();
    private static final UUID grantedOrg = UUID.randomUUID();
    private static final UUID otherGrantedOrg = UUID.randomUUID();

    @Parameterized.Parameters(name = "{index} {0}")
    public static Iterable<Object[]> data() {
        // @formatter:off
        return Arrays.asList(
            new Object[][] {
                {"User assigned to organization with all roles",
                    grantedOrg,
                    mockUserManagement(allOrgRoles(grantedOrg)),
                    null
                },
                {"User assigned to organization with no roles",
                    grantedOrg,
                    mockUserManagement(noOrgRoles(grantedOrg)),
                    null
                },
                {"User assigned to requested and other organizations",
                    grantedOrg,
                    mockUserManagement(allOrgRoles(grantedOrg), allOrgRoles(otherGrantedOrg)),
                    null
                },
                {"User not assigned to any organization",
                    deniedOrg,
                    mockUserManagement(),
                    new AccessDeniedException(OrgPermissionVerifier.ACCESS_DENIED_MSG)
                },
                {"User not assigned to requested organization",
                    deniedOrg,
                    mockUserManagement(allOrgRoles(grantedOrg)),
                    new AccessDeniedException(OrgPermissionVerifier.ACCESS_DENIED_MSG)
                },
                {"User assigned to multiple other organizations",
                    deniedOrg,
                    mockUserManagement(allOrgRoles(grantedOrg), allOrgRoles(otherGrantedOrg)),
                    new AccessDeniedException(OrgPermissionVerifier.ACCESS_DENIED_MSG)
                }
            });
        // @formatter:on
    }


    @Parameterized.Parameter(0)
    public String testName;

    @Parameterized.Parameter(1)
    public UUID org;

    @Parameterized.Parameter(2)
    public UserManagementClient umClient;

    @Parameterized.Parameter(3)
    public Exception exception;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void prepareExceptionHandling() {
        Optional.ofNullable(exception).ifPresent(ex -> {
            thrown.expect(ex.getClass());
            thrown.expectMessage(OrgPermissionVerifier.ACCESS_DENIED_MSG);
        });
    }

    @Test
    public void testUploaderRequest() {
        // given
        final OrgPermissionVerifier verifier = new OrgPermissionVerifier(umClient, auth -> "token");

        // when
        verifier.checkOrganizationAccess(org, mock(Authentication.class));

        // then
        verify(umClient).getPermissions("bearer token");
        verifyNoMoreInteractions(umClient);
    }

    private static UserManagementClient mockUserManagement(CcOrgPermission... permissions) {
        final UserManagementClient umClient = mock(UserManagementClient.class);
        if(permissions == null) {
            when(umClient.getPermissions(anyString())).thenReturn(ImmutableList.of());
        } else {
            when(umClient.getPermissions(anyString())).thenReturn(Arrays.asList(permissions));
        }
        return umClient;
    }

    private static CcOrgPermission allOrgRoles(UUID guid) {
        return new CcOrgPermission(new CcOrg(guid, guid.toString()), true, true, true);
    }

    private static CcOrgPermission noOrgRoles(UUID guid) {
        return new CcOrgPermission(new CcOrg(guid, guid.toString()), false, false, false);
    }

}
