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

package com.forgerock.backstage.ssoextensions.temporaryaccountlockout.decision;

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutUtils;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.time.TimeProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.identity.idm.AMIdentity;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import static com.forgerock.backstage.ssoextensions.temporaryaccountlockout.decision.TemporaryAccountLockoutDecision.LOCKED_OUTCOME;
import static com.forgerock.backstage.ssoextensions.temporaryaccountlockout.decision.TemporaryAccountLockoutDecision.UNLOCKED_OUTCOME;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TemporaryAccountLockoutDecisionTest {
    private final CoreWrapper mockCoreWrapper = mock(CoreWrapper.class);
    private final AMIdentity mockIdentity = mock(AMIdentity.class);
    private final JsonValue sharedState = JsonValue.json(ImmutableMap.of(USERNAME, "demo", REALM, "/"));
    private final TreeContext mockContext = new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), Collections.emptyList());
    private final TimeProvider mockTimeProvider = mock(TimeProvider.class);
    private final TemporaryAccountLockoutUtils utils = new TemporaryAccountLockoutUtils(mockCoreWrapper, mockTimeProvider);
    private final Instant now = Instant.parse("2019-04-01T12:00:00.000Z");

    public TemporaryAccountLockoutDecisionTest() {
        when(mockCoreWrapper.getIdentity(anyString(), anyString())).then(invocation -> mockIdentity);
        when(mockTimeProvider.now()).thenReturn(now);
    }

    @Test
    public void shouldReturnUnlockedIfThereIsNoData() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(Collections.emptySet());
        assertEquals(node(5, 300).process(mockContext).outcome, UNLOCKED_OUTCOME);
    }

    @Test
    public void shouldReturnUnlockedIfTheMaxAttemptsAreNotExceeded() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(1, now));
        assertEquals(node(5, 300).process(mockContext).outcome, UNLOCKED_OUTCOME);
    }

    @Test
    public void shouldReturnUnlockedIfTheLastAttemptIsOld() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(6, now.minus(301, ChronoUnit.SECONDS)));
        assertEquals(node(5, 300).process(mockContext).outcome, UNLOCKED_OUTCOME);
    }

    @Test
    public void shouldReturnLockedIfTheMaxAttemptsAreExceeded() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(getAccountLockoutXml(6, now));
        assertEquals(node(5, 300).process(mockContext).outcome, LOCKED_OUTCOME);
    }

    private TemporaryAccountLockoutDecision node(int maxAttempts, int lockoutDuration) {
        return new TemporaryAccountLockoutDecision(new TemporaryAccountLockoutDecision.Config() {
            public int maxAttempts() {
                return maxAttempts;
            }

            public int lockoutDuration() {
                return lockoutDuration;
            }
        }, utils);
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
