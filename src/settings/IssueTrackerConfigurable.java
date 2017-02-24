package settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import ui.ConfigurationPanel;

import javax.swing.*;

/**
 * The "configurable component" required by IntelliJ IDEA to provide a Swing form for inclusion into the 'Settings'
 * dialog. Registered in {@code plugin.xml} as a {@code projectConfigurable} extension.
 */
public class IssueTrackerConfigurable implements Configurable
{
    public static final String ID = "Android Issue Tracker";

    private final Project project;

    private final ConfigurationPanel configPanel;

    public IssueTrackerConfigurable(@NotNull final Project project) {
        this(project, new ConfigurationPanel(project));
    }

    private IssueTrackerConfigurable(@NotNull final Project project,
                                     @NotNull final ConfigurationPanel configPanel) {
        this.project = project;
        this.configPanel = configPanel;
    }

    public String getDisplayName() {
        return ID;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        reset();
        return configPanel;
    }

    @Override
    public boolean isModified() {
        final UserSettings configuration = getConfiguration();
        if (configPanel.getNumberOfRetries() != configuration.getNumberOfRetries())
            return true;

        return false;
    }

    public void apply() throws ConfigurationException {
        final UserSettings configuration = getConfiguration();

    }

    final UserSettings getConfiguration() {
        return ServiceManager.getService(project, UserSettings.class);
    }

    @Override
    public void reset() {
        final UserOptions configuration = getConfiguration().getState();
        configPanel.setSelectedColumnSpecs(
                configuration.getSelectedIssueProperties());
        configPanel.setNumberOfRetries(configuration.getNumberOfRetries());
        configPanel.initializeConfigPanel();
    }

    public void disposeUIResources() {
        // do nothing
    }
}
