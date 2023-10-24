package com.abstractprogrammer.nullnotion.service;

import com.abstractprogrammer.nullnotion.enums.AuthenticationMode;
import com.abstractprogrammer.nullnotion.enums.DatabaseType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
@State(name = "com.abstractprogrammer.nullnotion.service.SettingsState", storages = @Storage("NullNotionSettings.xml"))
public final class SettingsState implements PersistentStateComponent<SettingsState> {
    public String host = "";
    public String port = "";
    public AuthenticationMode authenticationMode;
    public String username;
    public String password;
    public String databaseName;
    public DatabaseType databaseType;

    public static SettingsState getInstance() {
        return ApplicationManager.getApplication().getService(SettingsState.class);
    }

    @Override
    public @NotNull SettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
