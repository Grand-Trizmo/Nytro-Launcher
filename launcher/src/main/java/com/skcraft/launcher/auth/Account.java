/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NonNull;

import java.util.Date;

/**
 * A user account that can be stored and loaded.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account implements Comparable<Account> {
    private StoredSession session;

    private String mojangId;

    private Date lastUsed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!session.getUuid().equalsIgnoreCase(account.session.getUuid())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return session.getUuid().hashCode();
    }

    @Override
    public int compareTo(@NonNull Account o) {
        Date otherDate = o.getLastUsed();

        if (otherDate == null && lastUsed == null) {
            return 0;
        } else if (otherDate == null) {
            return -1;
        } else if (lastUsed == null) {
            return 1;
        } else {
            return -lastUsed.compareTo(otherDate);
        }
    }

    @Override
    public String toString() {
        return this.session == null ? this.getMojangId() : this.session.getName();
    }

    public String getMojangId() {
        return this.mojangId;
    }

}
