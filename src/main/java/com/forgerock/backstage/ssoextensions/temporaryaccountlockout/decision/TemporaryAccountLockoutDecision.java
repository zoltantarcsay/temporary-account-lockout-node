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

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutConfig;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.TemporaryAccountLockoutUtils;
import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model.TemporaryAccountLockout;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;

import javax.inject.Inject;
import java.util.List;

@Node.Metadata(outcomeProvider = TemporaryAccountLockoutDecision.OutcomeProvider.class,
        configClass = TemporaryAccountLockoutDecision.Config.class)
public class TemporaryAccountLockoutDecision extends AbstractDecisionNode {

    private final Config config;
    private final TemporaryAccountLockoutUtils utils;

    final static String LOCKED_OUTCOME = "Locked";
    final static String UNLOCKED_OUTCOME = "Unlocked";

    public interface Config extends TemporaryAccountLockoutConfig {
    }

    @Inject
    public TemporaryAccountLockoutDecision(@Assisted Config config, TemporaryAccountLockoutUtils utils) {
        this.config = config;
        this.utils = utils;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            TemporaryAccountLockout accountLockout = utils.getAccountLockoutData(context);
            String outcome = isLockedOut(accountLockout) ? LOCKED_OUTCOME : UNLOCKED_OUTCOME;
            return Action.goTo(outcome).build();
        } catch (Exception e) {
            throw new NodeProcessException(e);
        }
    }

    public static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            return ImmutableList.of(
                    new Outcome(LOCKED_OUTCOME, LOCKED_OUTCOME),
                    new Outcome(UNLOCKED_OUTCOME, UNLOCKED_OUTCOME)
            );
        }
    }

    private boolean isLockedOut(TemporaryAccountLockout accountLockout) {
        boolean attemptsExceeded = accountLockout.getInvalidCount() >= config.maxAttempts();
        boolean isWithinLockoutDuration = utils.isWithinLockoutDuration(accountLockout, config.lockoutDuration());
        return attemptsExceeded && isWithinLockoutDuration;
    }

}
