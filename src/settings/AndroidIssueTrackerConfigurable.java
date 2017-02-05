package settings;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import settings.AndroidIssueTrackerSettings.AndroidIssueTrackerOptions;
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

    AndroidIssueTrackerConfigurable(@NotNull final Project project,
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
        final AndroidIssueTrackerOptions configuration = getConfiguration();
        return false;
    }


    public void apply() throws ConfigurationException {
        final AndroidIssueTrackerOptions configuration = getConfiguration();

    }

    final AndroidIssueTrackerOptions getConfiguration() {
        return ServiceManager.getService(project, AndroidIssueTrackerOptions.class);
    }


    public void reset() {
        final AndroidIssueTrackerOptions configuration = getConfiguration();

    }

    public void disposeUIResources() {
        // do nothing
    }
}
