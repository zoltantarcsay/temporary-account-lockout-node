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

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class TemporaryAccountLockoutMarshaller {
    private final JAXBContext context;

    public TemporaryAccountLockoutMarshaller() throws JAXBException {
        context = JAXBContext.newInstance(TemporaryAccountLockout.class);
    }

    public String marshal(TemporaryAccountLockout accountLockout) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(accountLockout, writer);

        return writer.toString();
    }

    public TemporaryAccountLockout unmarshal(String xml) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StreamSource source = new StreamSource(new StringReader(xml));
        JAXBElement<TemporaryAccountLockout> element = unmarshaller.unmarshal(source, TemporaryAccountLockout.class);

        return element.getValue();
    }
}

