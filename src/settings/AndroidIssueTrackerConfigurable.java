package settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import ui.AndroidIssueTrackerConfigPanel;

import javax.swing.*;

/**
 * The "configurable component" required by IntelliJ IDEA to provide a Swing form for inclusion into the 'Settings'
 * dialog. Registered in {@code plugin.xml} as a {@code projectConfigurable} extension.
 */
public class AndroidIssueTrackerConfigurable implements Configurable
{
    private final Project project;

    private final AndroidIssueTrackerConfigPanel configPanel;

    public AndroidIssueTrackerConfigurable(@NotNull final Project project) {
        this(project, new AndroidIssueTrackerConfigPanel(project));
    }

    private AndroidIssueTrackerConfigurable(@NotNull final Project project,
                                    @NotNull final AndroidIssueTrackerConfigPanel configPanel) {
        this.project = project;
        this.configPanel = configPanel;
    }

    public String getDisplayName() {
        return "Android Issue Tracker";
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
        final AndroidIssueTrackerSettings configuration = getConfiguration();
        return false;
    }


    public void apply() throws ConfigurationException {
        final AndroidIssueTrackerSettings configuration = getConfiguration();

    }

    final AndroidIssueTrackerSettings getConfiguration() {
        return ServiceManager.getService(project, AndroidIssueTrackerSettings.class);
    }

    @Override
    public void reset() {
        final AndroidIssueTrackerOptions configuration = getConfiguration().getState();
        configPanel.setSelectedColumnSpecs(configuration.getSelectedColumnSpecs());
        configPanel.setNumberOfRetries(configuration.getNumberOfRetries());
        configPanel.initializeConfigPanel();
    }

    public void disposeUIResources() {
        // do nothing
    }
}
