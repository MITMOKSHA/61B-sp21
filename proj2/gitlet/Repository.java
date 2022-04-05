package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
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
    public static final File GITREFSHEADS_DIR = join(CWD, "refs/heads");
    public static final File GITOBJECTS_DIR = join(CWD, "objects");
    public static Commit commitListHead;

    /* TODO: fill in the rest of this class. */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        GITREFSHEADS_DIR.mkdir();
        File master = join(GITREFSHEADS_DIR, "master"); // read the sha-1 string value of branch master into refs/master
        commitListHead = new Commit("initial commit", null, sha1(serialize(master)), null);
    }

    public static void add(String filename) {
        File in = join(GITLET_DIR, filename);
        if (!in.exists()) {
            System.out.println("File does not exist.");
        }
        File stage = join(GITLET_DIR, "stage");
        if (stage.exists()) {

        }
    }
}
