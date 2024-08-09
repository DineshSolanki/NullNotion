package com.abstractprogrammer.nullnotion.configurable;

import com.abstractprogrammer.nullnotion.component.SettingsComponent;
import com.abstractprogrammer.nullnotion.service.ConnectionSettings;
import com.abstractprogrammer.nullnotion.service.SettingsState;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Provides controller functionality for application settings.
 */
public class SettingsConfigurable implements Configurable {

  private SettingsComponent mySettingsComponent;

  /**
   * Returns the project currently in context.
   *
   * @return Current project
   */
  private Project getCurrentProject() {
      DataContext dataContext;
      try {
          dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(100);
      } catch (TimeoutException | ExecutionException e) {
          throw new RuntimeException(e);
      }
      return dataContext == null ? null : CommonDataKeys.PROJECT.getData(dataContext);
  }

  // A default constructor with no arguments is required because this implementation
  // is registered as an applicationConfigurable EP

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "NullNotion";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new SettingsComponent();
    return mySettingsComponent.getPanel();
  }

    @Override
    public boolean isModified() {
        SettingsState settings = SettingsState.getInstance();
        return isAnySettingChanged(settings);
    }

    private boolean isAnySettingChanged(SettingsState settings) {
        return !mySettingsComponent.getHostText().equals(settings.host) ||
                !mySettingsComponent.getDatabaseType().equals(settings.databaseType) ||
                !mySettingsComponent.getDatabaseText().equals(settings.databaseName) ||
                !Objects.equals(mySettingsComponent.authenticationModeComboBox.getSelectedItem(), settings.authenticationMode) ||
                !Objects.equals(mySettingsComponent.getUsername(), settings.username) ||
                !Objects.equals(mySettingsComponent.getPassword(), settings.password);
    }

  @Override
  public void apply() {
    SettingsState settings = mySettingsComponent.getSettingsState();
    Project currentProject = getCurrentProject();
    SettingsState.getInstance().loadState(settings);
    String connectionString = mySettingsComponent.getConnectionString();
    Objects.requireNonNull(currentProject).getService(ConnectionSettings.class).setData(connectionString,
            settings.databaseType.name());

  }

  @Override
  public void reset() {
    SettingsState settings = SettingsState.getInstance();
    mySettingsComponent.setSettingsState(settings);
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}