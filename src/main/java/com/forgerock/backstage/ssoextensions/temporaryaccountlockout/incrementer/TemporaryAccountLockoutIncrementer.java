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

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutConfig;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutUtils;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model.TemporaryAccountLockout;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.time.TimeProvider;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.openam.auth.node.api.*;

import javax.inject.Inject;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class,
        configClass = TemporaryAccountLockoutIncrementer.Config.class)
public class TemporaryAccountLockoutIncrementer extends SingleOutcomeNode {
    private final Config config;
    private final TemporaryAccountLockoutUtils utils;
    private final TimeProvider timeProvider;

    public interface Config extends TemporaryAccountLockoutConfig {
    }

    @Inject
    public TemporaryAccountLockoutIncrementer(@Assisted Config config, TemporaryAccountLockoutUtils utils, TimeProvider timeProvider) {
        this.config = config;
        this.utils = utils;
        this.timeProvider = timeProvider;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            TemporaryAccountLockout accountLockout = utils.getAccountLockoutData(context);
            int attempts = utils.isWithinLockoutDuration(accountLockout, config.lockoutDuration())
                    ? accountLockout.getInvalidCount() + 1
                    : 1;
            accountLockout.setInvalidCount(attempts);
            accountLockout.setLastInvalidAt(timeProvider.now());

            if (attempts == config.maxAttempts()) {
                accountLockout.setLockedOutAt(timeProvider.now());
                accountLockout.setActualLockoutDuration(config.lockoutDuration() * 1000);
                accountLockout.setNoOfTimesLocked(1);
            }

            utils.setAccountLockoutData(context, accountLockout);
            return goToNext().build();
        } catch (Exception e) {
            throw new NodeProcessException(e);
        }
    }
}
