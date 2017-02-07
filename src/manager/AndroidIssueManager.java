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
        ObjectInputStream reader = null;
        try {
            reader = new ObjectInputStream(
                    new FileInputStream(issueListFile));
            IssuePost post;
            while ((post = (IssuePost) reader.readObject()) != null) {
                entries.add(post);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }
        return entries;
    }

    public static List<IssuePost> getFilteredIssueList(final ArrayList<String> issueIds) {
        if (issueIds == null) {
            return getIssueListFromStorage();
        }
        ArrayList<IssuePost> filteredPosts = new ArrayList<>();
        ArrayList<IssuePost> allPosts = (ArrayList<IssuePost>) getIssueListFromStorage();
        for (IssuePost post : allPosts) {
            if (issueIds.contains(post.getId())) {
                filteredPosts.add(post);
            }
        }
        return filteredPosts;
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
            for (int i = 0; i < numLines; i += 3) {
                String author = strings.get(i);
                String date = strings.get(i + 1);
                String comments = strings.get(i + 2);
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
            IDEUtil.displaySimpleNotification(NotificationType.ERROR,
                    null, "Failed", "Could not write issues locally, reason: "
                            + ex.getLocalizedMessage());

        }
    }

    /**
     * Write issue thread to storage (separate file for easy indexing)
     * @param issueId
     * @param threads
     */
    public static void writeThreadToStorage(@NotNull final String issueId,
                                             final List<IssueComment> threads) {
        LOG.debug("Writing issue #" + issueId + " thread to directory: "
                + ISSUE_DIRECTORY_NAME);
        File issueCacheDirectory = getIssueDirectory();
        File threadFile = new File(issueCacheDirectory, issueId + ISSUE_FILE_EXTENSION);
        FileWriter fos = null;
        try {
            //create the issue thread file if it doesn't exist.
            if (!threadFile.exists()) {
                LOG.debug("Thread file created for issue #"
                        + issueId
                        + " : "
                        + threadFile.createNewFile());
            } else {
                LOG.debug("Updating file created for issue #"
                        + issueId);
            }

            fos = new FileWriter(threadFile);
            for (IssueComment thread : threads) {
                fos.write("Author: " + thread.getAuthor() + "\n");
                fos.write("Date: " + thread.getDate() + "\n");
                fos.write("Comment: " + thread.getComment() + "\n");
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete the cached issue files (.aitf files) and issue index file.
     */
    public static void clearCacheAndIndex() {
        LOG.debug("Clearing issues and index " + ISSUE_DIRECTORY_NAME);
        File issueCacheDirectory = getIssueDirectory();
        try {
            deleteDirectory(issueCacheDirectory);
            IssueIndex.deleteIndex();
        } catch (IOException | IllegalAccessException ex) {
            ex.printStackTrace();
            //TODO: Use IDEUtil methods
            Notifications.Bus.notify(new Notification("Android Issue Tracker",
                    "Failed", "Could not delete index/issues" +
                    " reason: " + ex.getLocalizedMessage(), NotificationType.INFORMATION));
        }
    }

    public static String getIssueIdFromPath(final String path) {
        File file = new File(path);
        return file.getName().replace(ISSUE_FILE_EXTENSION, "");
    }

    /**
     * Utility method to delete a directory.
     * @param directory
     * @return
     */
    private static boolean deleteDirectory(final File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    }
                    else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }
}
