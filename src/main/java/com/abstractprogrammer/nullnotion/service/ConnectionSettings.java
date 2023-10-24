package com.abstractprogrammer.nullnotion.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
@State(name = "com.abstractprogrammer.nullnotion.service.ConnectionSettings", storages = @Storage("connectionSettings.xml"))
public final class ConnectionSettings implements PersistentStateComponent<ConnectionSettings.State> {
    private State state = new State();

    public static class State {
        public String connectionString;
        public String databaseName;
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public void setData(String connectionString, String databaseName) {
        state.connectionString = connectionString;
        state.databaseName = databaseName;
    }
}

