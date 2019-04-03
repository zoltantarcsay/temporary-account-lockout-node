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

package com.forgerock.backstage.ssoextensions.temporaryaccountlockout;

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model.TemporaryAccountLockout;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.time.TimeProvider;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.xml.TemporaryAccountLockoutMarshaller;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

public class TemporaryAccountLockoutUtils {

    private final CoreWrapper coreWrapper;
    private final TimeProvider timeProvider;
    private final TemporaryAccountLockoutMarshaller marshaller;

    public final static String LOCKOUT_ATTRIBUTE_NAME = "sunAMAuthInvalidAttemptsData";

    @Inject
    public TemporaryAccountLockoutUtils(CoreWrapper coreWrapper, TimeProvider timeProvider, TemporaryAccountLockoutMarshaller marshaller) {
        this.coreWrapper = coreWrapper;
        this.timeProvider = timeProvider;
        this.marshaller = marshaller;
    }

    public TemporaryAccountLockout getAccountLockoutData(TreeContext context) throws IdRepoException, SSOException, JAXBException {
        AMIdentity userIdentity = getUserIdentity(context);

        if (userIdentity == null) {
            throw new SSOException("User not found");
        }

        Set<String> attribute = userIdentity.getAttribute(LOCKOUT_ATTRIBUTE_NAME);
        return attribute.isEmpty() ? new TemporaryAccountLockout() : marshaller.unmarshal(attribute.iterator().next());
    }

    public void setAccountLockoutData(TreeContext context, TemporaryAccountLockout accountLockout) throws IdRepoException, SSOException, JAXBException {
        String data = marshaller.marshal(accountLockout);
        AMIdentity userIdentity = getUserIdentity(context);
        userIdentity.setAttributes(ImmutableMap.of(LOCKOUT_ATTRIBUTE_NAME, ImmutableSet.of(data)));
        userIdentity.store();
    }

    public boolean isWithinLockoutDuration(TemporaryAccountLockout accountLockout, int lockoutDuration) {
        Instant startOfLockoutDuration = timeProvider.now().minus(lockoutDuration, ChronoUnit.SECONDS);
        if (accountLockout.getLastInvalidAt() == null) {
            return false;
        }
        return accountLockout.getLastInvalidAt().compareTo(startOfLockoutDuration) >= 0;
    }

    private AMIdentity getUserIdentity(TreeContext context) {
        String username = context.sharedState.get(USERNAME).asString();
        String realm = context.sharedState.get(REALM).asString();
        return coreWrapper.getIdentity(username, realm);
    }

}
