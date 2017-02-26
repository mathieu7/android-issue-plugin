package settings;

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

    private UserSettings userSettings;

    public IssueTrackerConfigurable(@NotNull final Project project) {
        this(project, new ConfigurationPanel(project));
    }

    private IssueTrackerConfigurable(@NotNull final Project project,
                                     @NotNull final ConfigurationPanel configPanel) {
        this.project = project;
        this.userSettings = UserSettings.getInstance(project);
        this.configPanel = configPanel;
    }

    public String getDisplayName() {
        return ID;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return configPanel;
    }

    @Override
    public boolean isModified() {
        if (configPanel.getNumberOfRetries() != userSettings.getNumberOfRetries())
            return true;
        if (configPanel.hasDirtySelectedProperties())
            return true;
        return false;
    }

    public void apply() throws ConfigurationException {
        userSettings.setSelectedIssueProperties(configPanel.getSelectedIssueProperties());
        userSettings.setNumberOfRetries(configPanel.getNumberOfRetries());
    }


    @Override
    public void reset() {
        configPanel.setSelectedColumnSpecs(
                userSettings.getSelectedIssueProperties());
        configPanel.setNumberOfRetries(userSettings.getNumberOfRetries());
        configPanel.initializeConfigPanel();
    }

    public void disposeUIResources() {
        // do nothing
    }
}
