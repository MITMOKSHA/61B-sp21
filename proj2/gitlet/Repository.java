package gitlet;

import org.junit.Test;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public static TreeMap<String, String> stageArea = new TreeMap<>();  // stage area.

    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();  // Create .gitlet directory
        GITHEADS_DIR.mkdir();  // Create head pointer(branch).

        File master = join(GITHEADS_DIR, "master"); // Read the sha-1 string value of branch master into refs/master
        Commit m = new Commit("initial commit", stageArea, null, null);
        // serialize the object to byte stream and store it into master file.
        writeObject(master, m);
        String shaId = m.getOwnRef();

        File commitObject = join(GITOBJECTS_DIR);
        commitObject.mkdir();
        File commitObjectFile = join(commitObject, shaId);
        writeObject(commitObjectFile, m);  // serialize the commit to object/ directory.

        writeObject(GITSTAGEAREA, stageArea);
    }

    public static void add(String filename) {
        File in = join(CWD, filename);
        if (!in.exists()) {
            System.out.println("File does not exist.");
        }
        String blobId = sha1(serialize(in));
        // deserialize the byte stream to Commit object and align to master object.
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);
        // If reference of blob is same as reference of commit.
        if (blobId.equals(master.getOwnRef())) {
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

    public static void commit(String message) {
        // read stage file
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);
        // If no files have been staged, print message and abort.
        if (stageArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        // Clear stage area after commit.
        Commit newCommit = new Commit(message, stageArea, null, null);
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        newCommit.setParentRef(master.getOwnRef());  // set current commit as the new commit parent.
        // update master Object then write back to master file.
        master = newCommit;

        // update master file(head pointer).
        writeObject(join(GITHEADS_DIR, "master"), master);

        // add commit object to object file.
        String shaId = newCommit.getOwnRef();
        writeObject(join(GITOBJECTS_DIR, shaId), newCommit);
        stageArea.clear();
        // update stage area.
        writeObject(GITSTAGEAREA, stageArea);
    }

    public static void rm(String filename) {
        stageArea = readObject(GITSTAGEAREA, TreeMap.class);
        Commit master = readObject(join(GITHEADS_DIR, "master"), Commit.class);
        File in = join(CWD, filename);
        // get file sha-1 id.
        String blobId = sha1(serialize(in));
        if (stageArea.containsKey(blobId)) {
            stageArea.remove(blobId);  // Remove the blob from stage area.
            // update stageArea.
            writeObject(GITSTAGEAREA, stageArea);
        } else {
            // If the file is not tracked by the head commit, print error message.
            if (!master.getStageArea().containsKey(blobId)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
        }
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
}
