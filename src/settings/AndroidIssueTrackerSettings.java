package settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "AndroidIssueTrackerSettings",
        storages = {
                @Storage("android_issue_tracker.xml")
        }
)
class AndroidIssueTrackerSettings implements PersistentStateComponent<
        AndroidIssueTrackerSettings.AndroidIssueTrackerOptions> {

    public static class AndroidIssueTrackerOptions {
        private int numberOfRetries;
        private String[] columnSpec = DEFAULT_COLUMN_SPEC;

        /**
         * Default Column Spec used by the plugin.
         */
        public static final String[] DEFAULT_COLUMN_SPEC = {
                "Id",
                "Status",
                "Priority",
                "Owner",
                "Summary",
                "Stars",
                "Reporter",
                "Opened",
                "Component",
                "Type",
                "Version"
        };

        public AndroidIssueTrackerOptions() {
            numberOfRetries = 5;
        }
    }

    private AndroidIssueTrackerOptions myBuildOptions = new AndroidIssueTrackerOptions();

    @Nullable
    @Override
    public AndroidIssueTrackerOptions getState() {
        return myBuildOptions;
    }

    @Override
    public void loadState(final AndroidIssueTrackerOptions state) {
        myBuildOptions = state;
    }

    @NotNull
    public static AndroidIssueTrackerSettings getInstance(@NotNull Project project) {
        AndroidIssueTrackerSettings settings =
                ServiceManager.getService(project, AndroidIssueTrackerSettings.class);
        if (settings == null) {
            settings = new AndroidIssueTrackerSettings();
        }
        return settings;
    }

    @NotNull
    public static AndroidIssueTrackerSettings getDefault() {
        return getInstance(ProjectManager.getInstance().getDefaultProject());
    }
}