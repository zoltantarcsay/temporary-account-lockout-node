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

package com.forgerock.backstage.ssoextensions.temporaryaccountlockout.xml;

import com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model.TemporaryAccountLockout;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.time.Instant;

import static org.junit.Assert.*;

public class TemporaryAccountLockoutMarshallerTest {
    private final int INVALID_COUNT = 3;
    private final String TIMESTAMP = "2019-04-03T09:27:39.674Z";
    private final long TIMESTAMP_MILLIS = 1554283659674L;
    private final TemporaryAccountLockoutMarshaller marshaller = new TemporaryAccountLockoutMarshaller();

    public TemporaryAccountLockoutMarshallerTest() throws JAXBException {
    }

    @Test
    public void shouldMarshalAndMatchExpectedString() throws JAXBException, IOException {
        assertEquals(marshaller.marshal(getTemporaryAccountLockout()), getXml());
    }

    @Test
    public void shouldUnMarshalAndMatchExpectedObject() throws JAXBException, IOException {
        assertEquals(marshaller.unmarshal(getXml()), getTemporaryAccountLockout());

    }

    private String getXml() {
        return "" +
                "<InvalidPassword>\n" +
                "    <InvalidCount>" + INVALID_COUNT + "</InvalidCount>\n" +
                "    <LastInvalidAt>" + TIMESTAMP_MILLIS + "</LastInvalidAt>\n" +
                "    <LockedoutAt>0</LockedoutAt>\n" +
                "    <ActualLockoutDuration>0</ActualLockoutDuration>\n" +
                "    <NoOfTimesLocked>0</NoOfTimesLocked>\n" +
                "</InvalidPassword>";
    }

    private TemporaryAccountLockout getTemporaryAccountLockout() {
        return new TemporaryAccountLockout(INVALID_COUNT, Instant.parse(TIMESTAMP));
    }
}