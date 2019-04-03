/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2019 ForgeRock AS.
 * Portions copyright 2019 Zoltan Tarcsay
 */

package com.forgerock.backstage.ssoextensions.temporaryaccountlockout.incrementer;

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutUtils;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model.TemporaryAccountLockout;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.time.TimeProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.junit.Test;

import static com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutUtils.LOCKOUT_ATTRIBUTE_NAME;
import static com.forgerock.backstage.ssoextensions.temporaryaccountlockout.xml.TemporaryAccountLockoutMarshaller.*;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TemporaryAccountLockoutIncrementerTest {
    private final CoreWrapper mockCoreWrapper = mock(CoreWrapper.class);
    private final AMIdentity mockIdentity = mock(AMIdentity.class);
    private final JsonValue sharedState = JsonValue.json(ImmutableMap.of(USERNAME, "demo", REALM, "/"));
    private final TreeContext mockContext = new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), Collections.emptyList());
    private final TimeProvider mockTimeProvider = mock(TimeProvider.class);
    private final Instant now = Instant.parse("2019-04-01T12:00:00.000Z");

    private final TemporaryAccountLockoutUtils utils = new TemporaryAccountLockoutUtils(mockCoreWrapper, mockTimeProvider);


    public TemporaryAccountLockoutIncrementerTest() {
        when(mockCoreWrapper.getIdentity(anyString(), anyString())).then(invocation -> mockIdentity);
        when(mockTimeProvider.now()).thenReturn(now);
    }

    @Test
    public void shouldSetAttemptsToOneWhenEmpty() throws IdRepoException, SSOException, NodeProcessException {
        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            TemporaryAccountLockout accountLockout = unmarshal(attributes.get(LOCKOUT_ATTRIBUTE_NAME).iterator().next());
            assertEquals(accountLockout.getInvalidCount(), 1);
            assertEquals(accountLockout.getLastInvalidAt(), now);
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(5, 300).process(mockContext);
    }

    @Test
    public void shouldSetAttemptsToOneWhenOld() throws IdRepoException, SSOException, NodeProcessException {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(3, Instant.now().minus(400, ChronoUnit.SECONDS)));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            TemporaryAccountLockout accountLockout = unmarshal(attributes.get(LOCKOUT_ATTRIBUTE_NAME).iterator().next());
            assertEquals(accountLockout.getInvalidCount(), 4);
            assertEquals(accountLockout.getLastInvalidAt(), now);
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(5, 300).process(mockContext);
    }

    @Test
    public void shouldIncrementAttemptsWhenRecent() throws IdRepoException, SSOException, NodeProcessException {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(2, Instant.now()));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            TemporaryAccountLockout accountLockout = unmarshal(attributes.get(LOCKOUT_ATTRIBUTE_NAME).iterator().next());
            assertEquals(accountLockout.getInvalidCount(), 3);
            assertEquals(accountLockout.getLastInvalidAt(), now);
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(5, 300).process(mockContext);
    }

    @Test
    public void shouldSetLockedOutAtWhenLimitReached() throws IdRepoException, SSOException, NodeProcessException {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(4, Instant.now()));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            TemporaryAccountLockout accountLockout = unmarshal(attributes.get(LOCKOUT_ATTRIBUTE_NAME).iterator().next());
            assertEquals(accountLockout.getLockedOutAt(), now);
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(5, 300).process(mockContext);
    }

    private TemporaryAccountLockoutIncrementer node(int maxAttempts, int lockoutDuration) {
        return new TemporaryAccountLockoutIncrementer(new TemporaryAccountLockoutIncrementer.Config() {
            public int maxAttempts() {
                return maxAttempts;
            }

            public int lockoutDuration() {
                return lockoutDuration;
            }
        }, utils, mockTimeProvider);
    }


    private Set<String> getAccountLockoutXml(int invalidCount, Instant lastInvalidAt) {
        String xml = "" +
                "<InvalidPassword>\n" +
                "    <InvalidCount>" + invalidCount + "</InvalidCount>\n" +
                "    <LastInvalidAt>" + lastInvalidAt.toEpochMilli() + "</LastInvalidAt>\n" +
                "    <LockedoutAt>0</LockedoutAt>\n" +
                "    <ActualLockoutDuration>0</ActualLockoutDuration>\n" +
                "    <NoOfTimesLocked>0</NoOfTimesLocked>\n" +
                "</InvalidPassword>";

        return ImmutableSet.of(xml);
    }


}
