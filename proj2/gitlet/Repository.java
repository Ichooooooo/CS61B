package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author icovo
 */
public class Repository {
    /**
     *
     *
     * .gitlet/ -- top Folder to storage the information needed for gitlet
     *    - Commit/ --Stage the Commit class and Named this by their SHA1
     *    - Blob/ --
     *    - Stage/ --
     *          - Addition/  -- The file needed to add
     *          - Remove/  -- The file remove from next Commit
     *    - Branch/ --
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet and other directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMIT_DIR = join(GITLET_DIR, "Commit");
    public static final File BLOB_DIR = join(GITLET_DIR, "Blob");
    public static final File STAGE_DIR = join(GITLET_DIR, "Stage");
    public static final File STAGE_ADDITION_DIR = join(STAGE_DIR, "Addition");
    public static final File STAGE_REMOVE_DIR = join(STAGE_DIR, "Remove");
    public static final File BRANCH_DIR = join(GITLET_DIR, "Branch");
    /** The HEAD branch */
    public static Branch HEAD;

    /*
    auxiliary function for all, TODO : pivate or public?
     */

    // Persistence function
    private static void setPersistence(Commit commit) {
        File commitFile = join(COMMIT_DIR, commit.getSHA1());
        writeObject(commitFile, commit);
    }

    private static void setPersistence(Branch branch) {
        File branchFile = join(BRANCH_DIR, branch.getName());
        writeObject(branchFile, branch);
    }

    // Add file from Stage to Blob then delete from Stage
    private static void writeFileFromStageToBlob(File file) {
        File blobFile = join(BLOB_DIR, getFileSHA1(file));
        copyFile(file, blobFile);
        file.delete();
    }

    // Compare file with file function
    public static String getFileSHA1(File file) {
        return sha1(readContents(file));
    }

    private static boolean isFileContentSame(File file1, File file2) {
        return getFileSHA1(file1).equals(getFileSHA1(file2));
    }

    // Copy file
    private static void copyFile(File file, File copyfile) {
        byte[] content = readContents(file);
        writeContents(copyfile, content);
    }

    // Get class from file
    private static Branch getObjectFromBranch(File branchFile) {
        return readObject(branchFile, Branch.class);
    }

    private static Commit getObjectFromCommit(File commitFile) {
        return readObject(commitFile, Commit.class);
    }

    // Get file FROM Blob by file SHA1
    private static File getFileFromBLob(String fileSHA1) {
        return join(BLOB_DIR, fileSHA1);
    }

    // Get the HEAD commit
    private static Commit getHEADCommit() {
        Branch HEAD = getObjectFromBranch(join(BRANCH_DIR, "HEAD"));
        Commit commit = getObjectFromCommit(join(COMMIT_DIR, HEAD.getCommitSHA1()));
        return commit;
    }

    // Change the HEAD branch To new Commit, then SETPERSISTENCE
    private static void moveHEAD(Commit commit) {
        Branch HEAD = getObjectFromBranch(join(BRANCH_DIR, "HEAD"));
        HEAD.movePointer(commit.getSHA1());
        setPersistence(HEAD);
    }

    /**
     * Init : init the repo
     * - Set the basic Folder
     * - Set the first Commit
     * - Set the branch master and HEAD
     */

    public static void initFuc() {
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOB_DIR.mkdir();
        STAGE_DIR.mkdir();
        STAGE_ADDITION_DIR.mkdir();
        STAGE_REMOVE_DIR.mkdir();
        BRANCH_DIR.mkdir();

        // set the first commit then PERSIST
        Commit commit = new Commit(null, "initial commit", new Date(0L));
        setPersistence(commit);

        // set the branch master and HEAD, then point to the commit, then PERSIST
        Branch master = new Branch("master", commit.getSHA1());
        HEAD = new Branch("HEAD", commit.getSHA1());
        setPersistence(master);
        setPersistence(HEAD);
    }

    /**
     * Add : add the workspace file to Stage
     * Check whether the file in HEAD commit
     *          --  if file have add
     *              -- delete from Stage (both add or remove)
     *          --  else do nothing
     * Check whether the file in Stage
     *          --  update file
     * Check the rm
     *          -- delete from StageRem and readd
     */

    /*
     The FUNCTION in STAGE as follows :
             - boolean isFileInStageAdd(File file)
             - boolean isFileInStageRem(File file)
             - updateFileInStage(File file)
             - storeFileInStage(File file)
             - readdFileInStage(File file)
     */

    private static boolean isFileInStageAdd(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        if (stageAddFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isFileInStageRem(File file) {
        File stageRemFile = join(STAGE_REMOVE_DIR, file.getName());
        if (stageRemFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static void updateFileInStage(File file) {
        File stageFile = join(STAGE_ADDITION_DIR, file.getName());
        if (isFileContentSame(stageFile, file)) return;

        copyFile(file, stageFile);
    }

    private static void storeFileInStageAdd(File file) {
        File stageFile = join(STAGE_ADDITION_DIR, file.getName());
        copyFile(file, stageFile);
    }

    private static void storeFileInStageRemove(File file) {
        File stageRMFile = join(STAGE_REMOVE_DIR, file.getName());
        writeContents(stageRMFile, "");
    }

    // If file exist then delete else do nothing
    private static void deleteFileInStage(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        File stageRemFile = join(STAGE_REMOVE_DIR, file.getName());
        if (stageAddFile.exists()) stageAddFile.delete();
        if (stageRemFile.exists()) stageRemFile.delete();
    }

    private static void readdFileInStage(File file) {
        File stageRemoveFile = join(STAGE_REMOVE_DIR, file.getName());
        stageRemoveFile.delete();
        storeFileInStageAdd(file);
    }

    private static void rermFileInStage(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        stageAddFile.delete();
        storeFileInStageRemove(file);
    }

    private static boolean isFileInCommit(Commit commit, File file) {
        return commit.isFileExist(file.getName()) && isFileContentSame(file, getFileFromBLob(commit.getFileId(file.getName())));
    }

    public static void addFuc(String fileName) {
        Commit commit = getHEADCommit();
        File file = join(CWD, fileName);

        if (isFileInCommit(commit, file)) {
            deleteFileInStage(file);
            return;
        }

        if (isFileInStageAdd(file)) {
            updateFileInStage(file);
        }

        if (isFileInStageRem(file)) {
            readdFileInStage(file);
        }

        storeFileInStageAdd(file);
    }

    /**
     * Commit Function is Follow
     *  - Init the new Commit
     *  - For each Stage to add or remove file in Commit, and Write the file to BLOB, then DELETE the file
     *  - Set persist Commit and Remove HEAD, then Set persist HEAD
     *
     * @return boolean to check the error that nothing in Stage
     */

    // Add file from STAGE/ADDITION to Commit and then write file to BLOB, finally DELETE file in Stage
    private static boolean addFileFromStageToCommit(Commit commit) {
        List<String> list = plainFilenamesIn(join(STAGE_ADDITION_DIR));
        if (list.isEmpty()) return false;

        for (String fileName : list) {
            File file = join(STAGE_ADDITION_DIR, fileName);
            if (commit.isFileExist(fileName)) {
                commit.changeFile(file);
            } else {
                commit.addFile(file);
            }
            writeFileFromStageToBlob(file);
        }
        return true;
    }

    // Remove file in STAGE/REMOVE from Commit and DELETE file in Stage
    private static boolean removeFileFromStageToCommit(Commit commit) {
        List<String> list = plainFilenamesIn(join(STAGE_REMOVE_DIR));
        if (list.isEmpty()) return false;

        for (String fileName : list) {
            File file = join(STAGE_REMOVE_DIR, fileName);
            commit.removeFile(file);
            file.delete();
        }
        return true;
    }

    public static boolean commitFuc(String message) {
        Commit headCommit = getHEADCommit();
        Commit newCommit = new Commit(message, headCommit);
        boolean ok1 = addFileFromStageToCommit(newCommit);
        boolean ok2 = removeFileFromStageToCommit(newCommit);
        if (!(ok1 || ok2)) return false;
        setPersistence(newCommit);
        moveHEAD(newCommit);
        return true;
    }

    /**
     * RM function as follows :
     *
     */

    public static boolean removeFuc(String fileName) {
        Commit commit = getHEADCommit();
        File file = join(CWD, fileName);
        boolean isFileInHEAD = false, isFileInStage = false;
        if (commit.isFileExist(fileName)) {
            isFileInHEAD = true;
            if (isFileInStageAdd(file)) {
                rermFileInStage(file);
            } else {
                storeFileInStageRemove(file);
            }
            file.delete();
        }
        if (isFileInStageAdd(file)) {
            isFileInStage = true;
            deleteFileInStage(file);
        }

        if (!(isFileInHEAD || isFileInStage)) return false;
        return true;
    }
}
