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
        name = "UserSettings",
        storages = {
                @Storage("android_issue_tracker.xml")
        }
)
public class UserSettings implements PersistentStateComponent<UserOptions> {

    private UserOptions mUserOptions = new UserOptions();

    @Nullable
    @Override
    public UserOptions getState() {
        return mUserOptions;
    }

    @Override
    public void loadState(final UserOptions state) {
        mUserOptions = state;
    }

    @NotNull
    public static UserSettings getInstance(final @NotNull Project project) {
        UserSettings settings =
                ServiceManager.getService(project, UserSettings.class);
        if (settings == null) {
            settings = new UserSettings();
        }
        return settings;
    }

    @NotNull
    public static UserSettings getDefault() {
        return getInstance(ProjectManager.getInstance().getDefaultProject());
    }
}