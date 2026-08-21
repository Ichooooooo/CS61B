package gitlet;

import javax.swing.*;
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

    // Get commit From SHA1
    private static Commit getCommitFromSHA1(String SHA1) {
        return getObjectFromCommit(join(COMMIT_DIR, SHA1));
    }

    // Get Commit From Branch
    private static Commit getCommitFromBranch(Branch branch) {
        return getCommitFromSHA1(branch.getCommitSHA1());
    }

    // Get the frontest branchName from HEAD
    private static String getHEADBranchName() {
        return readContentsAsString(HEAD_DIR);
    }

    // Get the frontest branch from HEAD
    private static Branch getHEADBranch() {
        return getObjectFromBranch(join(BRANCH_DIR, getHEADBranchName()));
    }

    // Get the HEAD COMMIT
    private static Commit getHEADCommit() {
        Branch frontestBranch = getHEADBranch();
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

    // Move or Write the file in Commit to CWD
    private static void writeFileInCWDWithCommit(Commit commit, String fileName) {
        File fileInCWD = join(CWD, fileName);
        File fileInCommit = getFileFromCommit(commit, fileName);
        copyFile(fileInCommit, fileInCWD);
    }

    // Check whether there exist the file in CWD that head commit do not have but the target commit have
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

    // Find commit with commitId, (maybe uncompleted commitSHA1 I called commitId) if not find return NULL
    private static File findCommitWithId(String commitId) {
        List<String> commitList = getAllFileName(COMMIT_DIR);
        int length = commitId.length();
        for (String commitSHA1 : commitList) {
            if (commitSHA1.length() < length) continue;
            String abbrCommitSHA1 = commitSHA1.substring(0, length);
            if (abbrCommitSHA1.equals(commitId)) {
                return join(COMMIT_DIR, commitSHA1);
            }
        }
        return null;
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

    // delete the file in CWD that HEAD commit tracked but target commit untracked, Delete file -> HEAD && file !-> Commit
    private static void deleteFileInCWDCommitNotExist(Commit commit) {
        List<String> filesInCWD = getAllFileName(CWD);
        Commit headCommit = getHEADCommit();
        for (String fileName : filesInCWD) {
            File fileInCWD = join(CWD, fileName);
            if (isFileInCommit(headCommit, fileInCWD) && !isFileInCommit(commit, fileInCWD)) {
                fileInCWD.delete();
            }
        }
    }

    // Checkout all file in commit to CWD
    private static void checkoutAllCommit(Commit commit) {
        Map<String, String> trackedFiles = commit.getTrackedFiles();
        for (Map.Entry<String, String> entry : trackedFiles.entrySet()) {
            String fileName = entry.getKey();
            writeFileInCWDWithCommit(commit, fileName);
        }
    }

    // Check if file in CWD
    private static boolean ifFileInCWD(String fileName) {
        File file = join(CWD, fileName);
        return file.exists();
    }
    /**
     * Init : init the repo
     * - Set the basic Folder
     * - Set the first Commit
     * - Set the branch master and HEAD
     */

    public static void initFuc() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

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
        return commit.isFileExist(file.getName());
    }

    public static void addFuc(String fileName) {
        if (!join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Commit commit = getHEADCommit();
        File file = join(CWD, fileName);

        if (isFileInCommit(commit, file) && isFileContentSame(getFileFromCommit(commit, fileName), file)) {
//            System.out.println("file is same");
            deleteFileInStage(file);
            return;
        }

        if (isFileInStageAdd(file)) {
//            System.out.println("have add");
            updateFileInStage(file);
        } else if (isFileInStageRem(file)) {
//            System.out.println("have delete readd");
            readdFileInStage(file);
        } else {
//            System.out.println("new add");
            storeFileInStageAdd(file);
        }
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

    public static void commitFuc(String[] args) {
        String message = args[1];
        if (message == "") {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit headCommit = getHEADCommit();
        Commit newCommit = new Commit(message, headCommit);
        boolean ok1 = addFileFromStageToCommit(newCommit);
        boolean ok2 = removeFileFromStageToCommit(newCommit);
        if (!(ok1 || ok2)) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        setPersistence(newCommit);
        moveBranchAndHEAD(getHEADBranchName(), newCommit);
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

    public static void removeFuc(String fileName) {
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

        if (!(isFileInHEAD || isFileInStage)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
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
        System.out.println();
    }

    public static void logFuc() {
        Commit commit = getHEADCommit();

        printCommit(commit);
        while (commit.getFirstFather() != null) {
            commit = getCommitFromSHA1(commit.getFirstFather());
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
     * boolean to find whether some Commit satisfy the same message
     */

    public static void findFuc(String message) {
        List<String> list = getAllFileName(COMMIT_DIR);
        boolean isCommitExist = false;
        for (String commitName : list) {
            Commit commit = getObjectFromCommit(join(COMMIT_DIR, commitName));
            if (commit.getMessage().equals(message)) {
                isCommitExist = true;
                System.out.println(commit.getSHA1());
            }
        }
        if (!isCommitExist) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
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
        String frontestBranchName = getHEADBranchName();
        System.out.println("=== Branches ===");
        for (String branchName : list) {
            if (branchName.equals(frontestBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
    }

    private static void printAllStageAdd() {
        List<String> list = getAllFileName(STAGE_ADDITION_DIR);
        System.out.println("=== Staged Files ===");
        for (String fileName : list) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    private static void printAllStageRemove() {
        List<String> list = getAllFileName(STAGE_REMOVE_DIR);
        System.out.println("=== Removed Files ===");
        for (String fileName : list) {
            System.out.println(fileName);
        }
        System.out.println();
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
        System.out.println();
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
    private static void checkoutWithFileNameFuc(String fileName) {
        replaceFileInCWDWithCommit(getHEADCommit(), fileName);
    }

    // The second functions
    private static void checkoutWithCommitFuc(String commitId, String fileName) {
        File commitFile;
        if (commitId.length() >= 40) {
            commitFile = join(COMMIT_DIR, commitId);
        } else {
            commitFile = findCommitWithId(commitId);
        }
        if (commitFile == null || !commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        replaceFileInCWDWithCommit(getObjectFromCommit(commitFile), fileName);
    }

    // The third functions
    private static void checkoutWithBranch(String branchName) {
        File branchFile = join(BRANCH_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(getHEADBranchName())) {
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
        checkoutAllCommit(commit);
        // Delete file -> HEAD && file !-> Commit
        deleteFileInCWDCommitNotExist(commit);
        // HEAD -> branch
        moveHEAD(branch);
        // Stage -> null
        deleteStage();
    }

    public static void checkoutFuc(String[] args) {
        if (args.length == 2) {
            checkoutWithBranch(args[1]);
        } else if (args.length == 3) {
            if (Objects.equals(args[1], "--")) {
                checkoutWithFileNameFuc(args[2]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else if (args.length == 4) {
            if (Objects.equals(args[2], "--")) {
                checkoutWithCommitFuc(args[1], args[3]);
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    /**
     * branch functions as follows :
     * -  create a new branch and point to HEAD commit
     */

    public static void branchFuc(String branchName) {
        if (join(BRANCH_DIR, branchName).exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newBranch = new Branch(branchName, getHEADCommit().getSHA1());
        setPersistence(newBranch);
    }

    /**
     * rm-branch functions as follows :
     * -  delete the branch only
     */

    public static void rmbranchFuc(String branchName) {
        File deleteBranch = join(BRANCH_DIR, branchName);
        if (!deleteBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getHEADBranchName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        deleteBranch.delete();
    }

    /**
     * reset functions as follows :
     * - write all files in target commit to CWD
     *      -- if file untracked by HEAD commit but tracked by target commit [ERRO INFORMATION]
     * - delete the file tracked by HEAD commit but untracked by target commit
     * - change the branch, point to target commit
     */

    public static void resetFuc(String commitId) {
        File targetCommitFile;
        if (commitId.length() >= 40) {
            targetCommitFile = join(COMMIT_DIR, commitId);
        } else {
            targetCommitFile = findCommitWithId(commitId);
        }
        if (!targetCommitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit targetCommit = getObjectFromCommit(targetCommitFile);
        if (checkUntrackedFile(targetCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }
        // Add all ommitFile -> CWD
        checkoutAllCommit(targetCommit);
        // Delete file -> HEAD, file !-> target commit
        deleteFileInCWDCommitNotExist(targetCommit);
        // Branch -> target commit, notice PERSISTENCE
        Branch nowBranch = getHEADBranch();
        nowBranch.movePointer(targetCommit.getSHA1());
        setPersistence(nowBranch);
        // Delete STAGE
        deleteStage();
    }

    /**
     * merge function as follows :
     * There are two branch : HEAD branch, Merge branch
     * There are three commit : Split Point Commit, HEAD Commit, Merge Commit
     *
     * [ERROR INFORMATION] : file in CWD, in Merge, NOT in HEAD
     *
     * 1. File tracked by SP commit
     * - There are three situation file in other commit compare to SP : same(1), change(2), delete(3)
     *      - File same in HEAD commit, same in Merge commit (11)
     *              - do nothing
     *      - File changed in HEAD commit, same in Merge commit (12)
     *              - do nothing
     *      - File same in HEAD commit, changed in Merge commit (12)
     *              - checkout the file in Merge Commit
     *              - stage file to add
     *      - File delete in HEAD commit, same in Merge commit (13)         [ERROR INFORMATION]
     *              - do nothing
     *      - File same in HEAD commit, delete in Merge commit (13)
     *              - delete the file
     *              - stage file to delete
     *      - File changed in HEAD commit, changed in Merge commit (22)
     *              - if two changed file is same
     *                      - do nothing
     *              - if two changed file is different
     *                      - using new content replace the conflict file
     *                      - stage file to add
     *                      - print conflict message to bash (ONLY ONCE even there maybe many conflicts)
     *      - File changed in ..., but delete by ...(23, 23)        [ERROR INFORMATION]
     *              - using new content replace the conflict file
     *              - stage file to add
     *              - print conflict message to bash (ONLY ONCE even there maybe many conflicts)
     *      - File deleted by HEAD commit, deleted by Merge commit (33)
     *              - do nothing
     *
     * 2. File untracked by SP commit
     * - There are three situation compare HEAD file to Merge file : content same(1), content different(2), one have another not(3)
     *      - File same in two (1)
     *              - do nothing
     *      - File content different in two (2)
     *              - using new content replace the conflict file
     *              - stage file to add
     *              - print conflict message to bash (ONLY ONCE even there maybe many conflicts)
     *      - File create in HEAD commit, Merge commit not (3)
     *              - do nothing
     *      - File create in Merge commit, HEAD commit not (3)         [ERROR INFORMATION]
     *              - check out the file in Merge commit
     *              - stage file to add
     */

    // Mark the branch list from start commit list using set
    private static void markCommitList(Set<String> set, Commit commit) {
        if (!set.add(commit.getSHA1())) {
            return;
        }
        if (commit.getFirstFather() != null) {
            markCommitList(set, getCommitFromSHA1(commit.getFirstFather()));
        }
        if (commit.getSecondFather() != null) {
            markCommitList(set, getCommitFromSHA1(commit.getSecondFather()));
        }
    }

    // Find the nearest first father of two commit
    private static Commit findNearestFather(Commit commit1, Commit commit2) {
        Set<String> commitSet = new HashSet<>();
        markCommitList(commitSet, commit1);

        Queue<Commit> queue = new ArrayDeque<>();
        queue.add(commit2);
        Set<String> visited = new HashSet<>();
        while(!queue.isEmpty()) {
            Commit commit = queue.poll();
            if (visited.contains(commit.getSHA1())) continue;
            visited.add(commit.getSHA1());

            if (commitSet.contains(commit.getSHA1())) {
                return commit;
            }
            if (commit.getFirstFather() != null) {
                queue.add(getCommitFromSHA1(commit.getFirstFather()));
            }
            if (commit.getSecondFather() != null) {
                queue.add(getCommitFromSHA1(commit.getSecondFather()));
            }
        }
        return null;
    }

    // Find the Split Point of two branch
    private static Commit getSplitPoint(Branch br1, Branch br2) {
        return findNearestFather(getCommitFromSHA1(br1.getCommitSHA1()), getCommitFromSHA1(br2.getCommitSHA1()));
    }

    // return rebuild content from two conflict file
    private static String handleConflictFile(File HEADFile, File mergeFile) {
        String newContent = "<<<<<<< HEAD\n";
        if (HEADFile != null) {
            newContent = newContent + readContentsAsString(HEADFile);
        }
        newContent = newContent + "=======\n";
        if (mergeFile != null) {
            newContent = newContent + readContentsAsString(mergeFile);
        }
        newContent = newContent + ">>>>>>>\n";
        return newContent;
    }

    // handle the file that tracked by split point, return boolean to judge if conflict
    private static boolean handleSpExist(Commit spCommit, Commit HEADCommit, Commit mergeCommit) {
        TreeMap<String, String> spTrackedFiles = spCommit.getTrackedFiles();
        boolean isConflict = false;
        for (Map.Entry<String, String> entry : spTrackedFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileSHA1 = entry.getValue();
            // same : file is same
            // change : file is exist but changed
            // lose : file is deleted
            boolean same1 = false, change1 = false, delete1 = false;
            boolean same2 = false, change2 = false, delete2 = false;
            // judge two commit situation
            if (HEADCommit.isFileExist(fileName)) {
                if (fileSHA1.equals(HEADCommit.getFileId(fileName)))  same1 = true;
                else  change1 = true;
            } else {
                delete1 = true;
            }
            if (mergeCommit.isFileExist(fileName)) {
                if (fileSHA1.equals(mergeCommit.getFileId(fileName)))  same2 = true;
                else  change2 = true;
            } else {
                delete2 = true;
            }

            // handle situation below :
            if (same1 && same2) {
                // File is all same in two commit
                continue;
            } else if (same1 && change2) {
                // File changed in merge, same in HEAD
                checkoutWithCommitFuc(mergeCommit.getSHA1(), fileName);
                File fileInCWD = join(CWD, fileName);
                storeFileInStageAdd(fileInCWD);
            } else if (same2 && change1) {
                // File changed in HEAD, same in merge
                continue;
            } else if (same2 && delete1) {
                // File delete in HEAD, same in merge
                continue;
            } else if (same1 && delete2) {
                // File delete in merge, same in HEAD
                File fileInCWD = join(CWD, fileName);
                fileInCWD.delete();
                storeFileInStageRemove(fileInCWD);
            } else if (change1 && change2) {
                // File change in both, if content is same do nothing, else headle conflict
                if (HEADCommit.getFileId(fileName).equals(mergeCommit.getFileId(fileName))) {
                    continue;
                }
                isConflict = true;
                String newContent = handleConflictFile(getFileFromCommit(HEADCommit, fileName), getFileFromCommit(mergeCommit, fileName));
                File file = join(CWD, fileName);
                writeContents(file, (Object) newContent);
                storeFileInStageAdd(file);
            } else if (change1 && delete2) {
                // File change in HEAD, delete in merge
                isConflict = true;
                String newContent = handleConflictFile(getFileFromCommit(HEADCommit, fileName), null);
                File file = join(CWD, fileName);
                writeContents(file, (Object) newContent);
                storeFileInStageAdd(file);
            } else if (change2 && delete1) {
                // File changed in merge, delete in HEAD
                isConflict = true;
                String newContent = handleConflictFile(null, getFileFromCommit(mergeCommit, fileName));
                File file = join(CWD, fileName);
                writeContents(file, newContent);
                storeFileInStageAdd(file);
            } else if (delete1 && delete2) {
                continue;
            }
        }

        return isConflict;
    }

    // handle the file not tracked by split point, return boolean to judge conflict
    private static boolean handleSpNotExist(Commit spCommit, Commit HEADCommit, Commit mergeCommit) {
        boolean isConflict = false;
        Map<String, String> HEADTrackedFiles = HEADCommit.getTrackedFiles();

        // compare file from HEAD
        for (Map.Entry<String, String> entry : HEADTrackedFiles.entrySet()) {
            String fileName = entry.getKey();

            // have handled file skip
            if (spCommit.isFileExist(fileName)) continue;

            if (mergeCommit.isFileExist(fileName)) {
                // if file exist in merge
                if (HEADCommit.getFileId(fileName).equals(mergeCommit.getFileId(fileName))) {
                    // if content same, do nothing
                    continue;
                } else {
                    // if content different, handle conflict
                    isConflict = true;
                    String newContent = handleConflictFile(getFileFromCommit(HEADCommit, fileName), getFileFromCommit(mergeCommit, fileName));
                    File file = join(CWD, fileName);
                    writeContents(file, newContent);
                    storeFileInStageAdd(file);
                }
            } else {
                // file not exit in merge
                continue;
            }
        }

        Map<String, String> mergeTrackedFiles = mergeCommit.getTrackedFiles();
        // compare file from merge
        for (Map.Entry<String, String> entry : mergeTrackedFiles.entrySet()) {
            String fileName = entry.getKey();

            // have handled file skip
            if (spCommit.isFileExist(fileName)) continue;

            if (HEADCommit.isFileExist(fileName)) {
                // the file have handle before
                continue;
            } else {
                // file not exit in HEAD
                File fileInCWD = join(CWD, fileName);
                copyFile(getFileFromCommit(mergeCommit, fileName), fileInCWD);
                storeFileInStageAdd(fileInCWD);
            }
        }

        return isConflict;
    }

    private static boolean handleErrorInformation(Commit spCommit, Commit HEADCommit, Commit mergeCommit) {
        for (Map.Entry<String, String> entry : mergeCommit.getTrackedFiles().entrySet()) {
            String fileName = entry.getKey();
            if (HEADCommit.isFileExist(fileName)) {
                continue;
            }
            if (!ifFileInCWD(fileName)) {
                continue;
            }
            if (!spCommit.isFileExist(fileName)) {
                return true;
            }
            if (!spCommit.getFileId(fileName)
                    .equals(mergeCommit.getFileId(fileName))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isStageEmpty() {
        return plainFilenamesIn(STAGE_ADDITION_DIR).isEmpty() && plainFilenamesIn(STAGE_REMOVE_DIR).isEmpty();
    }

    private static void mergeCommit(String firstFather, String secondFather, String message) {
        Commit newCommit = new Commit(getCommitFromSHA1(firstFather), secondFather, message);
        addFileFromStageToCommit(newCommit);
        removeFileFromStageToCommit(newCommit);
        setPersistence(newCommit);
        moveBranchAndHEAD(getHEADBranchName(), newCommit);
    }

    public static void mergeFuc(String branchName) {
        if (!isStageEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!join(BRANCH_DIR, branchName).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getHEADBranchName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Branch HEADBranch = getObjectFromBranch(join(BRANCH_DIR, getHEADBranchName()));
        Branch mergeBranch = getObjectFromBranch(join(BRANCH_DIR, branchName));
        Commit spCommit = getSplitPoint(HEADBranch, mergeBranch);
        Commit HEADCommit = getCommitFromBranch(HEADBranch);
        Commit mergeCommit = getCommitFromBranch(mergeBranch);

        if (handleErrorInformation(spCommit, HEADCommit, mergeCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        if (spCommit.getSHA1().equals(mergeCommit.getSHA1())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (spCommit.getSHA1().equals(HEADCommit.getSHA1())) {
            checkoutWithBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        boolean isConflict =  handleSpExist(spCommit, HEADCommit, mergeCommit);
        isConflict = (isConflict | handleSpNotExist(spCommit, HEADCommit, mergeCommit));

        if (isStageEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        String mergeMessage = "Merged " + branchName + " into " + getHEADBranchName() + ".";
        mergeCommit(HEADBranch.getCommitSHA1(), mergeBranch.getCommitSHA1(), mergeMessage);
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }
}
