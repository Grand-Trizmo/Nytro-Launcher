/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * A list of accounts that can be stored to disk.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(converter = AccountList.AccountListConverter.class)
public class AccountList extends AbstractListModel implements ComboBoxModel {

    @JsonProperty
    private Map<String, Account> accounts = new HashMap<>();

    @JsonIgnore
    private List<Account> list = new ArrayList<>();

    @JsonIgnore
    private List<Consumer<Account>> listeners = new ArrayList<>();

    @JsonProperty
    private String activeAccount;
    
    private transient Account selected;

    /**
     * Add a new account.
     *
     * <p>If there is already an existing account with the same ID, then the
     * new account will not be added.</p>
     *
     * @param account the account to add
     */
    public synchronized void add(@NonNull Account account) {
        if (!accounts.containsKey(account.getMojangId())) {
            accounts.put(account.getMojangId(), account);
            list.add(account);

            fireContentsChanged(this, 0, accounts.size());
        }
    }

    /**
     * Remove an account.
     *
     * @param account the account
     */
    public synchronized void remove(@NonNull Account account) {
        Iterator<Account> it = accounts.values().iterator();
        while (it.hasNext()) {
            Account other = it.next();
            if (other.equals(account)) {
                it.remove();
                Collections.sort(this.list);
                fireContentsChanged(this, 0, accounts.size() + 1);
                break;
            }
        }
    }

    public void addListener(Consumer<Account> listener) {
        this.listeners.add(listener);
    }

    private void fireListeners(Account account) {
        for (Consumer<Account> consumer : this.listeners) {
            consumer.accept(account);
        }
    }

    @Override
    @JsonIgnore
    public synchronized int getSize() {
        return accounts.size();
    }

    @Override
    public synchronized Account getElementAt(int index) {
        try {
            return list.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if (item == null) {
            this.selected = null;
            return;
        }

        if (item instanceof Account) {
            this.selected = (Account) item;
        }
    }

    @Override
    @JsonIgnore
    public Account getSelectedItem() {
        return selected;
    }

    public synchronized void forgetSessions() {
        this.accounts.clear();
        this.list.clear();
    }

    public void setSelectedAccount(Account account) {
        this.activeAccount = account.getMojangId();

        this.fireListeners(account);
    }

    public Account getActiveAccount() {
        if (this.activeAccount == null) {
            return null;
        }

        return this.accounts.get(this.activeAccount);
    }

    public List<Account> getAllAccounts() {
        return this.list;
    }

    public static class AccountListConverter extends StdConverter<AccountList, AccountList> {
        public AccountListConverter() { }

        @Override
        public AccountList convert(AccountList value) {
            Iterator<Map.Entry<String, Account>> iterator = value.accounts.entrySet().iterator();

            while (iterator.hasNext()) {
                Account account = iterator.next().getValue();

                if (account.getMojangId() == null) {
                    iterator.remove();
                }

                value.getAllAccounts().add(account);
            }

            return value;
        }
    }
}
