package manager;

import com.google.common.io.Files;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.extensions.PluginId;
import com.sun.istack.internal.NotNull;
import index.IssueIndex;
import model.IssueComment;
import model.IssuePost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import util.IDEUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class AndroidIssueManager {
    private static final Log LOG = LogFactory.getLog(
            AndroidIssueManager.class);

    private static final String ISSUE_DIRECTORY_NAME = ".androidissues";
    private static final String ISSUE_LIST_FILE_NAME = "issues.ser";
    private static final String ISSUE_FILE_EXTENSION = ".aitf";

    private static AndroidIssueManager sInstance = new AndroidIssueManager();

    private AndroidIssueManager() {
    }

    public AndroidIssueManager getInstance() {
        return sInstance;
    }

    /**
     * The plugin identifier.
     */
    private static final String PLUGIN_ID = "com.miller.androidissuetracker";

    /**
     * Utility method to get the file system location of the plugin's directory.
     *
     * @return File
     */
    public static File getPluginDirectory() {
        return PluginManager.getPlugin(PluginId.getId(PLUGIN_ID)).getPath();
    }

    /**
     * Utility method to get the issue directory
     * (where issues and threads are stored)
     *
     * @return File
     */
    public static File getIssueDirectory() {
        return new File(getPluginDirectory(), ISSUE_DIRECTORY_NAME);
    }

    /**
     * Utility method to get the issue list file
     *
     * @return
     */
    public static File getIssueListFile() {
        return new File(getIssueDirectory(), ISSUE_LIST_FILE_NAME);
    }

    /**
     * Load locally stored issue list file.
     *
     * @return List of IssuePost
     */
    public static List<IssuePost> getIssueListFromStorage() {
        List<IssuePost> entries = new ArrayList<>();
        File issueListFile = getIssueListFile();
        if (!issueListFile.exists()) {
            return entries;
        }
        try {
            ObjectInputStream reader = new ObjectInputStream(
                    new FileInputStream(issueListFile));
            IssuePost post;
            while ((post = (IssuePost) reader.readObject()) != null) {
                entries.add(post);
            }
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    /**
     * Load locally stored issue thread file
     *
     * @return List of IssueComment
     */
    public static List<IssueComment> getIssueThreadFromId(@NotNull final String issueId) {
        List<IssueComment> entries = new ArrayList<>();
        File threadFile = new File(getIssueDirectory(), issueId + ISSUE_FILE_EXTENSION);
        if (!threadFile.exists()) {
            return entries;
        }
        try {
            List<String> strings = Files.readLines(threadFile, StandardCharsets.UTF_8);
            int numLines = strings.size();
            for (int i = 0; i < numLines; i+= 3) {
                String author = strings.get(i);
                String date = strings.get(i+1);
                String comments = strings.get(i+2);
                entries.add(new IssueComment(author, date, comments));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    /**
     * Load locally stored issue thread file
     *
     * @return List of IssueComment
     */
    public static List<String> getIssueThreadLinesFromId(@NotNull final String issueId) {
        List<String> entries = new ArrayList<>();
        File threadFile = new File(getIssueDirectory(), issueId + ISSUE_FILE_EXTENSION);
        if (!threadFile.exists()) {
            return entries;
        }
        try {
            entries = Files.readLines(threadFile, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return entries;
    }

    /**
     * Write posts to storage (serialized)
     * @param posts
     */
    public static void writePostsToStorage(final List<IssuePost> posts) {
        LOG.debug("Writing issues to directory: " + ISSUE_DIRECTORY_NAME);
        File issueCacheDirectory = getIssueDirectory();
        File issueListFile = new File(issueCacheDirectory, ISSUE_LIST_FILE_NAME);
        try {
            if (!issueCacheDirectory.exists()) {
                if (!issueCacheDirectory.mkdir()) {
                    throw new IOException(
                            "Could not create issue cache directory...");
                }
            }
            if (!issueListFile.exists()) {
                LOG.debug("Issue listing file created: "
                        + issueListFile.createNewFile());
            }

            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(issueListFile));
            for (IssuePost post : posts) {
                oos.writeObject(post);
            }
            oos.close();
        } catch (IOException ex) {
            //TODO: Use IDEUtil here
            Notifications.Bus.notify(new Notification("Android Issue Tracker",
                    "Failed", "Could not write issues locally" +
                    " reason: " + ex.getLocalizedMessage(), NotificationType.INFORMATION));
        }
    }

    /**
     * Write issue thread to storage (separate file for easy indexing)
     * @param issue
     * @param threads
     */
    public static void writeThreadsToStorage(final IssuePost issue,
                                             final List<IssueComment> threads) {
        LOG.debug("Writing issues to directory: " + ISSUE_DIRECTORY_NAME);
        File issueCacheDirectory = getIssueDirectory();
        File threadFile = new File(issueCacheDirectory, issue.getId() + ISSUE_FILE_EXTENSION);
        try {
            //create the issue thread file if it doesn't exist.
            if (!threadFile.exists()) {
                LOG.debug("Thread file created for issue "
                        + issue.getId()
                        + " : "
                        + threadFile.createNewFile());
            }

            FileWriter fos = new FileWriter(threadFile);
            for (IssueComment thread : threads) {
                fos.write("Author: " + thread.getAuthor() + "\n");
                fos.write("Date: " + thread.getDate() + "\n");
                fos.write("Comment: " + thread.getComment() + "\n");
            }
            fos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void clearCacheAndIndex() {
        LOG.debug("Clearing issues and index " + ISSUE_DIRECTORY_NAME);
        File issueCacheDirectory = getIssueDirectory();

        try {
            if (issueCacheDirectory.exists()) {
                if (!issueCacheDirectory.delete() ) {
                    throw new IOException("Could not delete issue cache directory");
                }
            }
            IssueIndex.deleteIndex();
        } catch (IOException | IllegalAccessException ex) {
            //TODO: Use IDEUtil methods
            Notifications.Bus.notify(new Notification("Android Issue Tracker",
                    "Failed", "Could not delete index/issues" +
                    " reason: " + ex.getLocalizedMessage(), NotificationType.INFORMATION));
        }
    }
}
