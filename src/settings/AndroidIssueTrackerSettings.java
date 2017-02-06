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
public class AndroidIssueTrackerSettings implements PersistentStateComponent<AndroidIssueTrackerOptions> {

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
    public static AndroidIssueTrackerSettings getInstance(final @NotNull Project project) {
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