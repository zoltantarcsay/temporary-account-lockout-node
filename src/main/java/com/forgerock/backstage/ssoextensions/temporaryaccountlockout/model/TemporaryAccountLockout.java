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

package com.forgerock.backstage.ssoextensions.temporaryaccountlockout.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.Objects;

@XmlRootElement(name = "InvalidPassword")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemporaryAccountLockout {

    @XmlElement(name = "InvalidCount")
    private int invalidCount;

    @XmlElement(name = "LastInvalidAt")
    private Instant lastInvalidAt = Instant.EPOCH;

    @XmlElement(name = "LockedoutAt")
    private Instant lockedOutAt = Instant.EPOCH;

    @XmlElement(name = "ActualLockoutDuration")
    private int actualLockoutDuration;

    @XmlElement(name = "NoOfTimesLocked")
    private int noOfTimesLocked;

    public TemporaryAccountLockout() {

    }

    public TemporaryAccountLockout(int invalidCount, Instant lastInvalidAt) {
        this.invalidCount = invalidCount;
        this.lastInvalidAt = lastInvalidAt;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public Instant getLastInvalidAt() {
        return lastInvalidAt;
    }

    public void setLastInvalidAt(Instant lastInvalidAt) {
        this.lastInvalidAt = lastInvalidAt;
    }

    public Instant getLockedOutAt() {
        return lockedOutAt;
    }

    public void setLockedOutAt(Instant lockedOutAt) {
        this.lockedOutAt = lockedOutAt;
    }

    public int getActualLockoutDuration() {
        return actualLockoutDuration;
    }

    public void setActualLockoutDuration(int actualLockoutDuration) {
        this.actualLockoutDuration = actualLockoutDuration;
    }

    public int getNoOfTimesLocked() {
        return noOfTimesLocked;
    }

    public void setNoOfTimesLocked(int noOfTimesLocked) {
        this.noOfTimesLocked = noOfTimesLocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemporaryAccountLockout that = (TemporaryAccountLockout) o;
        return invalidCount == that.invalidCount &&
                lockedOutAt == that.lockedOutAt &&
                actualLockoutDuration == that.actualLockoutDuration &&
                noOfTimesLocked == that.noOfTimesLocked &&
                Objects.equals(lastInvalidAt, that.lastInvalidAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invalidCount, lastInvalidAt, lockedOutAt, actualLockoutDuration, noOfTimesLocked);
    }
}
