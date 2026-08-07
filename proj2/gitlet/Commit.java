package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Repository.getFileSHA1;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  @author icovo
 */
public class Commit implements Serializable {

    // The five quality that make SHA1 for Commit
    private String message;
    private Date timestamp;
    private HashMap<String, String> trackedFiles = new HashMap<>();
    private String firstFather;
    private String secondFather;
    private String SHA1;

    public Commit(String firstFather, String message, Date timestamp) {
        this.firstFather = firstFather;
        this.message = message;
        this.timestamp = timestamp;
        SHA1 = sha1(serialize(this));
    }

    public Commit(String message, Commit parent) {
        this.firstFather = parent.getSHA1();
        this.message = message;
        this.timestamp = new Date();
        this.trackedFiles = new HashMap<>(parent.getTrackedFiles());
        SHA1 = sha1(serialize(this));
    }

    // get SHA1
    public String getSHA1() {
        return SHA1;
    }

    public boolean isFileExist(String fileName) {
        return trackedFiles.containsKey(fileName);
    }

    public String getFileId(String fileName) {
        return trackedFiles.get(fileName);
    }

    public HashMap<String, String> getTrackedFiles() { return trackedFiles; }

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
