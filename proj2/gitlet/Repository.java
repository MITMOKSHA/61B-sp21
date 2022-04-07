package gitlet;

import edu.princeton.cs.algs4.ST;
import org.junit.Test;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public static final File GITSTAGEAREA = join(GITLET_DIR, "index");  // Stage area.
    public static final File GITOBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File GIT_REMOVE_TRACK = join(GITLET_DIR, "removed_track");
    public static final File GIT_BLOB_DIR = join(GITLET_DIR, "blobs");
    public static final File GIT_BLOB_STAGE = join(GITLET_DIR, "blob_stage");
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
        GIT_BLOB_DIR.mkdir();
        // TODO
        GIT_BRANCH_DIR.mkdir();  // Create branches directory.

        File head = join(GITHEADS_DIR, "head"); // Read the sha-1 string value of branch head into refs/head
        Commit m = new Commit("initial commit", stageArea, null, null);
        // serialize the object to byte stream and store it into master file.
        writeObject(head, m);

        // TODO: not update the commitId of master in refs/ directory.
        File masterInBranches = join(GIT_BRANCH_DIR, "master");
        writeObject(masterInBranches, m);

        writeObject(head, m);
        String shaId = m.getOwnRef();

        GITOBJECTS_DIR.mkdir();
        File commitObjectFile = join(GITOBJECTS_DIR, shaId);
        writeObject(commitObjectFile, m);  // serialize the commit to object/2id/ directory.

        writeObject(GITSTAGEAREA, stageArea);

        // Initialize.
        writeObject(GIT_REMOVE_TRACK, new TreeMap<String, String>());
    }

    @SuppressWarnings("unchecked")  // ignore warning
    public static void add(String filename) {
        File in = join(CWD, filename);
        if (!in.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String blobId = sha1(filename, serialize(in));
        // deserialize the byte stream to Commit object and align to master object.
        Commit head = readObject(join(GITHEADS_DIR, "head"), Commit.class);
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);

        TreeMap<String, String> removedSets = readObject(GIT_REMOVE_TRACK, TreeMap.class);
        // If current added file is in the removed sets, it will not be stored in removed sets.
        if (removedSets.containsKey(blobId)) {
            removedSets.remove(blobId);
            // TODO: remove blob from blob_stage dir.
        }
        writeObject(GIT_REMOVE_TRACK, removedSets);

        // If the current working version of the file is identical to the version in the current commit
        if (head.getTrack().containsKey(blobId)) {
            // If the blob is contained in stage area, remove it from stage area.
            if (stageArea.containsKey(blobId)) {
                stageArea.remove(blobId);

            }
        } else {
            //  Otherwise, add the mapping filename to blob into stage area.
            stageArea.put(blobId, filename);
            // TODO: add the Blob according to blobId into blob_stage dir.
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
        // TODO: add blob_stage files into blobs/ dir.
        Commit head = readObject(join(GITHEADS_DIR, "head"), Commit.class);

        // Add the previous commit tracks into current commit.
        newCommit.addPreviousCommitTrack(head.getTrack());

        // If removed set is not empty, remove from current Commit. And then clear it.
        if (!removed.isEmpty()) {
            newCommit.removeTracks(removed);
            // TODO: search blobs dir, if commit track removed blobId, remove it from blobs/ dir.
            removed.clear();
        }
        // Update removed set.
        writeObject(GIT_REMOVE_TRACK, removed);

        newCommit.setParentRef(head.getOwnRef());  // set current commit as the new commit parent.
        // update master Object then write back to master file.
        head = newCommit;

        // update master file(head pointer).
        writeObject(join(GITHEADS_DIR, "head"), head);

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
        Commit head = readObject(join(GITHEADS_DIR, "head"), Commit.class);
        File in = join(CWD, filename);
        // get file sha-1 id.
        String blobId = sha1(filename, serialize(in));
        if (stageArea.containsKey(blobId)) {
            stageArea.remove(blobId);  // Remove the blob from stage area.
        } else {
            // If the file is not tracked by the head commit, print error message.
            if (!head.getTrack().containsKey(blobId)) {
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
        Commit head = readObject(join(GITHEADS_DIR, "head"), Commit.class);
        while (true) {
            System.out.println("===");
            System.out.println("commit " + head.getOwnRef());
            System.out.println("Date: " + head.getTimeStamp());
            System.out.println(head.getMessage());
            System.out.println();
            String shaId = head.getParentRef();
            // If parent of current commit object not exist, quit print log.
            if (shaId == null) {
                break;
            }
            File commitObjectFile = join(GITOBJECTS_DIR, shaId);
            head = readObject(commitObjectFile, Commit.class);
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
            System.out.println();
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
                System.out.println();
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
        String currentBranchName = plainFilenamesIn(GITHEADS_DIR).get(0);
        for (String branchName : branches) {
            // If head.
            if (currentBranchName.equals(branchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
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

        List<String> filesInDir = plainFilenamesIn(CWD);
        // Get current commit tracks.
        Commit headCommit = readObject(join(GITHEADS_DIR, "head"), Commit.class);
        TreeMap<String, String> tracks = headCommit.getTrack();
        // TODO add "Modifications Not Staged For Commit" and "Untracked Files" information.
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for (String fileName : filesInDir) {
            // If commit not track and not add this file to stage area.
            if (!tracks.containsValue(fileName) && !stagedFiles.containsValue(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public static void checkout(String args[]) {
        Commit currCommit = readObject(join(GITHEADS_DIR, "head"), Commit.class);
        String currentBranchName = plainFilenamesIn(join(GITHEADS_DIR, "head")).get(0);
        TreeMap<String, String> tracks = currCommit.getTrack();
        List<String> branches = plainFilenamesIn(GIT_BRANCH_DIR);

        // checkout -- fileName
        if (args.length == 3) {
            String fileName = args[2];

            // if file not in current commit.
            if (!tracks.containsValue(fileName)) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        // checkout commitId -- branchName
        } else if (args.length == 4) {
            String commitId = args[1];
            String branchName = args[3];

            // If current commit tracks do not contain the commit id, print message.
            if (!tracks.containsKey(commitId)) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        // checkout branchName
        } else if (args.length == 2) {
            String branchName = args[1];

            // Do not exist such a branch in branch directory, print message.
            if (!branches.contains(branchName)) {
                System.out.println("No such branch exists.");
                System.exit(0);
            }

            if (currentBranchName.equals(branchName)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }


}
