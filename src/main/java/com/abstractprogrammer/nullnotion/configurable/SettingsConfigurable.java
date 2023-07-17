package com.abstractprogrammer.nullnotion.configurable;

import com.abstractprogrammer.nullnotion.component.SettingsComponent;
import com.abstractprogrammer.nullnotion.service.SettingsState;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides controller functionality for application settings.
 */
public class SettingsConfigurable implements Configurable {

  private SettingsComponent mySettingsComponent;

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
    boolean modified = !mySettingsComponent.getHostText().equals(settings.host);
    modified |= mySettingsComponent.getPortText().equals(settings.port);
    return modified;
  }

  @Override
  public void apply() {
    SettingsState settings = mySettingsComponent.getSettingsState();
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