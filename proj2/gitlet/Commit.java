package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Repository.getFileSHA1;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  @author icovo
 */
public class Commit implements Serializable {

    // The five quality that make SHA1 for Commit
    private String message;
    private Date timestamp;
    private TreeMap<String, String> trackedFiles;
    private String firstFather;
    private String secondFather;
    private transient String SHA1;

    public Commit(String firstFather, String message, Date timestamp) {
        this.firstFather = firstFather;
        this.message = message;
        this.trackedFiles = new TreeMap<>();
        this.timestamp = new Date(timestamp.getTime());
        SHA1 = sha1(serialize(this));
    }

    public Commit(String message, Commit parent) {
        this.firstFather = parent.getSHA1();
        this.message = message;
        this.timestamp = new Date();
        this.trackedFiles = new TreeMap<>(parent.getTrackedFiles());
        SHA1 = sha1(serialize(this));
    }

    private String calculateSHA1() {
        return sha1(serialize(this));
    }

    // get SHA1
    public String getSHA1() {
        if (SHA1 == null) {
            SHA1 = calculateSHA1();
        }
        return SHA1;
    }

    public String getFileId(String fileName) {
        return trackedFiles.get(fileName);
    }

    public TreeMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getFirstFather() {
        return firstFather;
    }

    public String getSecondFather() {
        return secondFather;
    }

    public boolean isFileExist(String fileName) {
        return trackedFiles.containsKey(fileName);
    }

    public void addFile(File file) {
        trackedFiles.put(file.getName(), getFileSHA1(file));
    }

    public void changeFile(File file) {
        trackedFiles.replace(file.getName(), getFileSHA1(file));
    }

    public void removeFile(File file) {
        trackedFiles.remove(file.getName());
    }
}
