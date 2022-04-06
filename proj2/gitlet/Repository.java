package gitlet;

import edu.princeton.cs.algs4.ST;
import org.junit.Test;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Moksha
 */

public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File GITHEADS_DIR = join(GITLET_DIR, "heads");
    public static final File GITSTAGEAREA = join(GITLET_DIR, "stage");
    public static final File GITOBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File GIT_REMOVE_TRACK = join(GITLET_DIR, "removedTrack");
    // TODO refines branch dir.
    public static final File GIT_BRANCH_DIR = join(GITLET_DIR, "branches");
    public static TreeMap<String, String> stageArea = new TreeMap<>();  // stage area.

    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();  // Create .gitlet directory
        GITHEADS_DIR.mkdir();  // Create head pointer(branch).
        // TODO
        GIT_BRANCH_DIR.mkdir();  // Create branches directory.

        File master = join(GITHEADS_DIR, "master"); // Read the sha-1 string value of branch master into refs/master
        Commit m = new Commit("initial commit", stageArea, null, null);
        // serialize the object to byte stream and store it into master file.
        writeObject(master, m);

        // TODO
        File masterInBranches = join(GIT_BRANCH_DIR, "master");
        writeObject(masterInBranches, m);

        writeObject(master, m);
        String shaId = m.getOwnRef();

        File commitObject = join(GITOBJECTS_DIR);
        commitObject.mkdir();
        File commitObjectFile = join(commitObject, shaId);
        writeObject(commitObjectFile, m);  // serialize the commit to object/ directory.

        writeObject(GITSTAGEAREA, stageArea);

        writeObject(GIT_REMOVE_TRACK, new TreeMap<String, String>());
    }

    @SuppressWarnings("unchecked")  // ignore warning
    public static void add(String filename) {
        File in = join(CWD, filename);
        if (!in.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String blobId = sha1(serialize(in));
        // deserialize the byte stream to Commit object and align to master object.
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);

        TreeMap<String, String> removedSets = readObject(GIT_REMOVE_TRACK, TreeMap.class);
        // If current added file is in the removed sets, it will not be stored in removed sets.
        if (removedSets.containsKey(blobId)) {
            removedSets.remove(blobId);
        }
        writeObject(GIT_REMOVE_TRACK, removedSets);

        // If the current working version of the file is identical to the version in the current commit
        if (master.getTrack().containsKey(blobId)) {
            // If the blob is contained in stage area, remove it from stage area.
            if (stageArea.containsKey(blobId)) {
                stageArea.remove(blobId);
            }
        } else {
            //  Otherwise, add the mapping filename to blob into stage area.
            stageArea.put(blobId, filename);
        }
        // Write back to .gitlet/stage file.
        writeObject(GITSTAGEAREA, stageArea);
    }

    @SuppressWarnings("unchecked")  // ignore warning
    public static void commit(String message) {
        // Read removed set.
        TreeMap<String, String> removed = readObject(GIT_REMOVE_TRACK, TreeMap.class);
        // Read stage.
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);
        // If no files have been staged, print message and abort.
        if (stageArea.isEmpty() && removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Commit newCommit = new Commit(message, stageArea, null, null);
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);

        // Add the previous commit tracks into current commit.
        newCommit.addPreviousCommitTrack(master.getTrack());

        // If removed set is not empty, remove from current Commit. And then clear it.
        if (!removed.isEmpty()) {
            newCommit.removeTracks(removed);
            removed.clear();
        }
        // Update removed set.
        writeObject(GIT_REMOVE_TRACK, removed);


        newCommit.setParentRef(master.getOwnRef());  // set current commit as the new commit parent.
        // update master Object then write back to master file.
        master = newCommit;

        // update master file(head pointer).
        writeObject(join(GITHEADS_DIR, "master"), master);

        // add commit object to object file.
        String shaId = newCommit.getOwnRef();
        writeObject(join(GITOBJECTS_DIR, shaId), newCommit);
        // Clear stage area after commit.
        stageArea.clear();
        // update stage area.
        writeObject(GITSTAGEAREA, stageArea);
    }

    @SuppressWarnings("unchecked")  // ignore warning
    public static void rm(String filename) {
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        File in = join(CWD, filename);
        // get file sha-1 id.
        String blobId = sha1(serialize(in));
        if (stageArea.containsKey(blobId)) {
            stageArea.remove(blobId);  // Remove the blob from stage area.
        } else {
            // If the file is not tracked by the head commit, print error message.
            if (!master.getTrack().containsKey(blobId)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            } else {  // tracked
                TreeMap<String, String> tracks = readObject(GIT_REMOVE_TRACK, TreeMap.class);
                // add track to the list to be removed.
                tracks.put(blobId, filename);
                writeObject(GIT_REMOVE_TRACK, tracks);
                in.delete();  // delete file
            }
        }
        // update stageArea.
        writeObject(GITSTAGEAREA, stageArea);
    }

    public static void log() {
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        while (true) {
            System.out.println("===");
            System.out.println("commit " + master.getOwnRef());
            System.out.println("Date: " + master.getTimeStamp());
            System.out.println(master.getMessage());
            String shaId = master.getParentRef();
            // If parent of current commit object not exist, quit print log.
            if (shaId == null) {
                break;
            }
            File commitObjectFile = join(GITOBJECTS_DIR, shaId);
            master = readObject(commitObjectFile, Commit.class);
        }
    }

    public static void globalLog() {
        List<String> commitList = plainFilenamesIn(GITOBJECTS_DIR);
        for (String commitId : commitList) {
            // Get commit object.
            Commit m = readObject(join(GITOBJECTS_DIR, commitId), Commit.class);
            System.out.println("===");
            System.out.println("commit " + m.getOwnRef());
            System.out.println("Date: " + m.getTimeStamp());
            System.out.println(m.getMessage());
        }
    }

    public static void find(String commitMessage) {
        List<String> commitList = plainFilenamesIn(GITOBJECTS_DIR);
        boolean exist = false;
        for (String commitId : commitList) {
            // Get commit object.
            Commit m = readObject(join(GITOBJECTS_DIR, commitId), Commit.class);
            if (m.getMessage().equals(commitMessage)) {
                System.out.println("===");
                System.out.println("commit " + m.getOwnRef());
                System.out.println("Date: " + m.getTimeStamp());
                System.out.println(m.getMessage());
                exist = true;
            }
        }
        if (!exist) {
            System.out.println("Found no commit with that message.");
        }
    }

    @SuppressWarnings("unchecked")  // ignore warning
    public static void status() {
        System.out.println("=== Branches ===");
        // plainFilesIn function return lexicographic order.
        List<String> branches = plainFilenamesIn(GIT_BRANCH_DIR);
        String head = plainFilenamesIn(GITHEADS_DIR).get(0);
        for (String branch : branches) {
            // If head.
            if (head.equals(branch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> stagedFiles = readObject(GITSTAGEAREA, TreeMap.class);
        for (Map.Entry<String, String> entry : stagedFiles.entrySet()) {
            // Print filename.
            System.out.println(entry.getValue());
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        TreeMap<String, String> removedFiles = readObject(GIT_REMOVE_TRACK, TreeMap.class);
        for (Map.Entry<String, String> entry : removedFiles.entrySet()) {
            // Print filename.
            System.out.println(entry.getValue());
        }
        System.out.println();
        // TODO add "Modifications Not Staged For Commit" and "Untracked Files" information.
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
}
