package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author icovo
 */
public class Repository {
    /** The structure of .gitlet dir
      .gitlet/ --   top Folder to storage the information needed for gitlet
            Commit/ --  Store the Commit class and Named this by their SHA1
            Blob/ --  Store the Commit tracked files
            Stage/ --
                    Addition/  -- The file needed to add
                    Remove/  -- The file remove from next Commit
            HEAD.txt  --  Store the frontest BranchName
            Branch/ --  Store the Branch class
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
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");

    /*
    auxiliary function for all, set the ways as private
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

    // get FILE SHA1
    public static String getFileSHA1(File file) {
        return sha1((Object) readContents(file));
    }

    // COMPARE FILE
    private static boolean isFileContentSame(File file1, File file2) {
        return getFileSHA1(file1).equals(getFileSHA1(file2));
    }

    // COPY file
    private static void copyFile(File orginfile, File copyfile) {
        byte[] content = readContents(orginfile);
        writeContents(copyfile, (Object) content);
    }

    // Get class from file
    private static Branch getObjectFromBranch(File branchFile) {
        return readObject(branchFile, Branch.class);
    }

    private static Commit getObjectFromCommit(File commitFile) {
        return readObject(commitFile, Commit.class);
    }

    // Get file from BLOB by file SHA1
    private static File getFileFromBLob(String fileSHA1) {
        return join(BLOB_DIR, fileSHA1);
    }

    // Get file from Commit
    private static File getFileFromCommit(Commit commit, String fileName) {
        return getFileFromBLob(commit.getFileId(fileName));
    }

    // Get Commit From Branch
    private static Commit getCommitFromBranch(Branch branch) {
        return getObjectFromCommit(join(COMMIT_DIR, branch.getCommitSHA1()));
    }

    // Get the frontest branch from HEAD
    private static String getFrontestBranchName() {
        return readContentsAsString(HEAD_DIR);
    }

    // Get the HEAD COMMIT
    private static Commit getHEADCommit() {
        Branch frontestBranch = getObjectFromBranch(join(BRANCH_DIR, getFrontestBranchName()));
        return getCommitFromBranch(frontestBranch);
    }

    // MOVE HEAD, Change the HEAD To new Commit, then SETPERSISTENCE
    private static void moveHEAD(Branch branch) {
        writeContents(HEAD_DIR, branch.getName());
    }

    // MOVE Branch to new commit, and move the HEAD to the new branch, then SET PERSISTENCE
    private static void moveBranchAndHEAD(String branchName, Commit commit) {
        Branch branch = getObjectFromBranch(join(BRANCH_DIR, branchName));
        branch.movePointer(commit.getSHA1());
        setPersistence(branch);
        moveHEAD(branch);
    }

    // Get all file name from file dir
    private static List<String> getAllFileName(File file) {
        return plainFilenamesIn(file);
    }

    // Move the file in Commit to CWD
    private static void writeFileInCWDWithCommit(Commit commit, String fileName) {
        File fileInCWD = join(CWD, fileName);
        File fileInCommit = getFileFromCommit(commit, fileName);
        copyFile(fileInCommit, fileInCWD);
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
        writeContents(HEAD_DIR, "");

        // set the first commit then PERSIST
        Commit commit = new Commit(null, "initial commit", new Date(0L));
        setPersistence(commit);

        // set the branch master and HEAD, then point to the commit, then PERSIST
        Branch master = new Branch("master", commit.getSHA1());
        setPersistence(master);
        writeContents(HEAD_DIR, master.getName());
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

    // Judge if file EXIST in STAGE
    private static boolean isFileInStageAdd(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        return stageAddFile.exists();
    }

    private static boolean isFileInStageRem(File file) {
        File stageRemFile = join(STAGE_REMOVE_DIR, file.getName());
        return stageRemFile.exists();
    }

    // If the file have in STAGE/ADD, you need UPDATE the new content
    private static void updateFileInStage(File file) {
        File stageFile = join(STAGE_ADDITION_DIR, file.getName());
        if (isFileContentSame(stageFile, file)) return;

        copyFile(file, stageFile);
    }

    // CREATE new File in STAGE
    private static void storeFileInStageAdd(File file) {
        File stageFile = join(STAGE_ADDITION_DIR, file.getName());
        copyFile(file, stageFile);
    }

    private static void storeFileInStageRemove(File file) {
        File stageRMFile = join(STAGE_REMOVE_DIR, file.getName());
        writeContents(stageRMFile, "");
    }

    // If file exist in STAGE then delete else do nothing
    private static void deleteFileInStage(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        File stageRemFile = join(STAGE_REMOVE_DIR, file.getName());
        if (stageAddFile.exists()) stageAddFile.delete();
        if (stageRemFile.exists()) stageRemFile.delete();
    }

    // Move the file from STAGE/REMOVE to STAGE/ADD
    private static void readdFileInStage(File file) {
        File stageRemoveFile = join(STAGE_REMOVE_DIR, file.getName());
        stageRemoveFile.delete();
        storeFileInStageAdd(file);
    }

    // Move the file from STAGE/ADD to STAGE/REMOVE
    private static void rermFileInStage(File file) {
        File stageAddFile = join(STAGE_ADDITION_DIR, file.getName());
        stageAddFile.delete();
        storeFileInStageRemove(file);
    }

    // Judge if file in COMMIT
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
     *  - Set persist Commit and Remove branch master, and HEAD point to master, then Set persist branch and HEAD
     *
     * @return boolean to check the error that nothing in Stage
     */

    // Add file from STAGE/ADDITION to Commit and then write file to BLOB, finally DELETE file in Stage
    private static boolean addFileFromStageToCommit(Commit commit) {
        List<String> list = getAllFileName(join(STAGE_ADDITION_DIR));
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
        List<String> list = getAllFileName(join(STAGE_REMOVE_DIR));
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
        moveBranchAndHEAD("master", newCommit);
        return true;
    }

    /**
     * RM function as follows :
     *  - Check the Commit, if the file have FOLLOWED :
     *          -- If the file have added then delete it.
     *          -- Add the file to Stage/Remove.
     *          -- Delete from workspace.
     *  - UNFOLLOWED :
     *          -- If the file have added to Stage then delete it from Stage
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

        return (isFileInHEAD || isFileInStage);
    }

    /**
     * Log function as follows :
     *   -  formate the Date
     *   -  print follow the rule
     *   -  use the fatherCommit to come back the commit tree
     */

    private static String formatDate(Date timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);

        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format(
                Locale.ENGLISH,
                "%1$ta %1$tb %2$d %1$tT %1$tY %1$tz",
                timestamp,
                day
        );
    }

    private static void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getSHA1());
        // handle the merge
        if (commit.getSecondFather() != null) {
            System.out.println("Merge: " + commit.getFirstFather().substring(0, 7) + " " + commit.getSecondFather().substring(0, 7));
        }
        System.out.println("Date: " + formatDate(commit.getTimestamp()));
        System.out.println(commit.getMessage());
        System.out.println("");
    }

    public static void logFuc() {
        Commit commit = getHEADCommit();

        printCommit(commit);
        while (commit.getFirstFather() != null) {
            commit = getObjectFromCommit(join(COMMIT_DIR, commit.getFirstFather()));
            printCommit(commit);
        }
    }

    /**
     * global log function as follows :
     *    -  Use function in Utils to foreach the Commit file to print all commit
     */

    public static void globalLogFuc() {
        List<String> list = getAllFileName(COMMIT_DIR);
        for (String commitName: list) {
            printCommit(getObjectFromCommit(join(COMMIT_DIR, commitName)));
        }
    }

    /**
     * Find function as follows :
     *     -  Use the function in Utils to foreach the Commit file to find the satisfying commit
     * @return boolean to find whether some Commit satisfy the same message
     */

    public static boolean findFuc(String message) {
        List<String> list = getAllFileName(COMMIT_DIR);
        boolean isCommitExist = false;
        for (String commitName : list) {
            Commit commit = getObjectFromCommit(join(COMMIT_DIR, commitName));
            if (commit.getMessage().equals(message)) {
                isCommitExist = true;
                System.out.println(commit.getSHA1());
            }
        }
        return isCommitExist;
    }

    /**
     * Status functions as follows :
     * 1. Find the modified but not stage file
     * param TreeSet : use set to collect the fileName and sort.
     * param HashMap : use map to collect the description of file, "modified" or "deleted".
     *      -  Check the commit and get [fileName, fileSHA1], then compare to the WorkFile.
     *              -   If file in CWD, compare the file content, if change but not add to stage, ADD
     *              -   If file do not in CWD and not stage to STAGE/REMOVE, ADD
     *      -  Check the STAGE/ADD and get [file], then compare to the WorkFile.
     *              -   If file in CWD, compare the file content, if change but not add to stage, ADD
     *              -   If file do not in CWD, ADD
     * 2. Find the untracked file
     * param TreeSet : same as forwards
     *      -  Check the file in CWD, if neither the file in commit nor STAGE/ADD, ADD
     */

    /*
     Print the information that all in dir and don't need to select
     */

    private static void printAllBranch() {
        List<String> list = getAllFileName(BRANCH_DIR);
        String frontestBranchName = getFrontestBranchName();
        System.out.println("=== Branches ===");
        for (String branchName : list) {
            if (branchName.equals(frontestBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println("");
    }

    private static void printAllStageAdd() {
        List<String> list = getAllFileName(STAGE_ADDITION_DIR);
        System.out.println("=== Staged Files ===");
        for (String fileName : list) {
            System.out.println(fileName);
        }
        System.out.println("");
    }

    private static void printAllStageRemove() {
        List<String> list = getAllFileName(STAGE_REMOVE_DIR);
        System.out.println("=== Removed Files ===");
        for (String fileName : list) {
            System.out.println(fileName);
        }
        System.out.println("");
    }

    /*
     Try to select the modified but not stage file in COMMIT and STAGE/ADDTION
     */

    private static void selectFileFromCommit(Set<String> modifyButNotStage, HashMap<String, String> mapRecordChange) {
        Commit headCommit = getHEADCommit();
        TreeMap<String, String> trackedFiles = headCommit.getTrackedFiles();
        for (Map.Entry<String, String> entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileSHA1 = entry.getValue();
            File fileInCWD = join(CWD, fileName);
            if (fileInCWD.exists()) {
                if (!isFileContentSame(getFileFromBLob(fileSHA1), fileInCWD)) {
                    if (!isFileInStageAdd(fileInCWD)) {
                        modifyButNotStage.add(fileName);
                        mapRecordChange.put(fileName, "modified");
                    }
                }
            } else {
                if (!isFileInStageRem(fileInCWD)) {
                    modifyButNotStage.add(fileName);
                    mapRecordChange.put(fileName, "deleted");
                }
            }
        }
    }

    private static void selectFileFromStageAdd(Set<String> modifyButNotStage, HashMap<String, String> mapRecordChange) {
        List<String> list = getAllFileName(STAGE_ADDITION_DIR);
        for (String fileName : list) {
            File fileInCWD = join(CWD, fileName);
            File fileInStageAdd = join(STAGE_ADDITION_DIR, fileName);
            if (fileInCWD.exists()) {
                if (!isFileContentSame(fileInCWD, fileInStageAdd)) {
                    modifyButNotStage.add(fileName);
                    mapRecordChange.put(fileName, "modified");
                }
            } else {
                modifyButNotStage.add(fileName);
                mapRecordChange.put(fileName, "deleted");
            }
        }
    }

    /*
     Try to select the untracked file in CWD
     */

    private static void selectFileFromWork(Set<String> untrackedFiles) {
        List<String> list = getAllFileName(CWD);
        Commit headCommit = getHEADCommit();
        for (String fileName : list) {
            File file = join(CWD, fileName);
            if (!isFileInStageAdd(file) && !isFileInCommit(headCommit, file)) {
                untrackedFiles.add(fileName);
            }
        }
    }

    private static void printModifyButNotStage() {
        Set<String> modifyButNotStage = new TreeSet<>();
        HashMap<String, String> mapRecordChange = new HashMap<>();
        selectFileFromCommit(modifyButNotStage, mapRecordChange);
        selectFileFromStageAdd(modifyButNotStage, mapRecordChange);

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modifyButNotStage) {
            System.out.println(fileName + " " + "(" + mapRecordChange.get(fileName) + ")");
        }
        System.out.println("");
    }

    private static void printUntrackedFiles() {
        Set<String> untrackedFiles = new TreeSet<>();
        selectFileFromWork(untrackedFiles);

        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
        }
        System.out.println("");
    }

    public static void statusFuc() {
        printAllBranch();
        printAllStageAdd();
        printAllStageRemove();
        printModifyButNotStage();
        printUntrackedFiles();
    }

    /**
     * checkout function as follows :
     * - [file name]
     *          --  Replace or add a file taken from HEAD branch by file name
     *          --  if file not exist  [error information]
     * - [commit id] [file name]
     *          --  Replace or add a file taken from given commit by file name
     *          --  if file not exist  [error information]
     * - [branch name]
     *          -- Check out given branch to find the specific commit, write all file in specific commit to CWD, there are some situations:
     *                      - file not exist in CWD, add
     *                      - file exist in CWD and tracked by HEAD, replace
     *                      - file exist in CWD and untracked by HEAD, error [error information]
     *                      // notice that file in CWD and untracked by both HEAD and BRANCH , do nothing
     *          -- Check out the file in CWD, delete the file that tracked by HEAD but untracked by specific commit, delete
     *          -- Move the HEAD to specific commit
     *          -- Clear the stage
     */

    // replace file in CWD with commit, contains [error information] because the file should firstly exist in commit
    private static void replaceFileInCWDWithCommit(Commit commit, String fileName) {
        if (!commit.isFileExist(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeFileInCWDWithCommit(commit, fileName);
    }

    //  The first functions
    //  The ERRO HANDLE is in function [replaceFileInCWDWithCommit]
    public static void checkoutWithFileNameFuc(String fileName) {
        replaceFileInCWDWithCommit(getHEADCommit(), fileName);
    }

    // The second functions
    public static void checkoutWithCommitFuc(String commitSHA1, String fileName) {
        File commitFile = join(COMMIT_DIR, commitSHA1);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        replaceFileInCWDWithCommit(getObjectFromCommit(commitFile), fileName);
    }

    // Check whether there exist the file in CWD that head commit do not have but the branch commit have
    private static boolean checkUntrackedFile(Commit checkCommit) {
        Commit headCommit = getHEADCommit();
        List<String> filesInCWD = getAllFileName(CWD);
        for (String fileName : filesInCWD) {
            File fileInCWD = join(CWD, fileName);
            if (!isFileInCommit(headCommit, fileInCWD) && isFileInCommit(checkCommit, fileInCWD)) {
                return true;
            }
        }
        return false;
    }

    // Delete the stage
    private static void deleteStage() {
        List<String> fileInStageAdd = getAllFileName(STAGE_ADDITION_DIR);
        List<String> fileInStageRem = getAllFileName(STAGE_REMOVE_DIR);
        for (String fileName : fileInStageAdd) {
            deleteFileInStage(join(STAGE_ADDITION_DIR, fileName));
        }
        for (String fileName : fileInStageRem) {
            deleteFileInStage(join(STAGE_REMOVE_DIR, fileName));
        }
    }

    // The third functions
    public static void checkoutWithBranch(String branchName) {
        File branchFile = join(BRANCH_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(getFrontestBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Branch branch = getObjectFromBranch(branchFile);
        Commit commit = getCommitFromBranch(branch);
        // Check and print error information
        if (checkUntrackedFile(commit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        // Commit -> CWD
        TreeMap<String, String> trackedFiles = commit.getTrackedFiles();
        for (Map.Entry<String, String> entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            writeFileInCWDWithCommit(commit, fileName);
        }
        // Delete file -> HEAD && file !-> Commit
        List<String> filesInCWD = getAllFileName(CWD);
        Commit headCommit = getHEADCommit();
        for (String fileName : filesInCWD) {
            File fileInCWD = join(CWD, fileName);
            if (isFileInCommit(headCommit, fileInCWD) && !isFileInCommit(commit, fileInCWD)) {
                fileInCWD.delete();
            }
        }
        // HEAD -> branch
        moveHEAD(branch);
        // Stage -> null
        deleteStage();
    }
}
