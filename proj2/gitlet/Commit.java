package gitlet;

// TODO: any imports you need here

import jdk.jshell.execution.Util;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.SimpleTimeZone;
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author moksha
 */

public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timeStamp;
    private TreeMap<String, String> blobMapToFileName;  // map file obj to Sha-1.
    private String ownRef;         // string represents sha-1 hash string.
    private String parentRef;
    private String secondParentRef;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, TreeMap<String, String> stageArea,
                  String parentRef, String secondParentRef) {
//        LocalDateTime current = LocalDateTime.now();
//        UTC
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // PST
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy Z");
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss yyyy Z");
        this.message = message;
        this.timeStamp = ZonedDateTime.now().format(formatter);
        this.blobMapToFileName = stageArea;
        this.parentRef = parentRef;
        this.secondParentRef = secondParentRef;
        this.ownRef = Utils.sha1(Utils.serialize(this));
    }

    public Commit() {
        this.message = null;
        this.timeStamp = null;
        this.blobMapToFileName = null;
        this.parentRef = null;
        this.secondParentRef = null;
        this.ownRef = null;
    }

    public String getOwnRef() {
        return ownRef;
    }

    // set current commit parentRef as m.parentRef
    public String setParentRef(String Ref) { return this.parentRef = Ref; }

    public String getParentRef() {
        return parentRef;
    }

    public String getSecondParentRef() {
        return secondParentRef;
    }

    public TreeMap<String, String> getStageArea() {
        return blobMapToFileName;
    }

    public String getMessage() {
        return message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }


}
