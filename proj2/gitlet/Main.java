package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static gitlet.Repository.*;
import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author icovo
 */
public class Main {

    /**
     *  [ERROR INFORMATION]
     *      -  not enter a command
     *          - [Please enter a command.]
     *      -  enter a no meaning command
     *          - [No command with that name exists.]
     *      -  enter an incorrect command, nums is wrong or format wrong
     *          - [Incorrect operands.]
     *      -  not init (no .gitlet dir) but the command need firstly init
     *          - [Not in an initialized Gitlet directory.]
     *
     *  [.GITLET DIR]
     *  .gitlet/ -- top level folder for all persistent data for gitlet
     *      - Blob/ -- folder contains all files tracked by commit
     *      - Branch/ -- folder contains all branch created
     *      - Commit/ -- folder contains all commit created
     *      - Stage/
     *          - Addition/ -- folder store the file need to add to commit
     *          - Remove/ -- folder store the file need to delete from commit
     *      - HEAD.txt  -- store the name of HEAD branch
     *
     *  [COMMANDS] - Runs one of their commands:
     *
     *  init    -- init .gitlet directory
     *
     *  add [file name]    -- tracked a file new situation, add the file to .gitlet/Stage
     *
     *  commit [message]    -- based on the stage information, create a new commit, tracked or untracked some file
     *
     *  rm [file name]      -- remove the file you tracked
     *
     *  log     -- print from HEAD commit to first commit list information
     *
     *  global-log      -- print all commit information
     *
     *  find [commit message]       -- find all commit that have input message
     *
     *  status        -- print all branch and  print the file in stage additon, in stage remove, changed but not tracked new situation, untracked
     *
     *  checkout -- [file name]         -- write the file with file name in HEAD commit to CWD, not stage it
     *  checkout [commit id] -- [file name]         -- write the file with file name in given commit to CWD, not stage it
     *  checkout [branch name]          -- check out the given branch commit to CWD, changed the HEAD branch to given branch
     *
     *  branch [branch name]        -- create a new branch pointed to HEAD commit
     *
     *  rm-branch [branch name]         -- remove the branch (only branch not move file or commit)
     *
     *  reset [commit id]       -- check out the given commit to CWD, changed the located branch commit to given commit
     *
     *  merge [branch name]         -- merge the given branch to HEAD branch then commit, HEAD branch move to new commit
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init(args);
                break;
            case "add":
                add(args);
                break;
            case "commit":
                commit(args);
                break;
            case "rm":
                remove(args);
                break;
            case "log":
                log(args);
                break;
            case "global-log":
                globalLog(args);
                break;
            case "find":
                find(args);
                break;
            case "status":
                status(args);
                break;
            case "checkout":
                checkout(args);
                break;
            case "branch":
                branch(args);
                break;
            case "rm-branch":
                rmbranch(args);
                break;
            case "reset":
                reset(args);
                break;
            case "merge":
                merge(args);
                break;
            default:
                noSuchCommand();
        }
    }

    private static void noSuchCommand() {
        System.out.println("No command with that name exists.");
        System.exit(0);
    }

    private static void isGitletExist() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void init(String[] args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        initFuc();
    }

    private static void add(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        addFuc(args[1]);
    }

    private static void commit(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        commitFuc(args);
    }

    private static void remove(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        removeFuc(args[1]);
    }

    private static void log(String[] args) {
        isGitletExist();
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        logFuc();
    }

    private static void globalLog(String[] args) {
        isGitletExist();
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        globalLogFuc();
    }

    private static void find(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        findFuc(args[1]);
    }

    private static void status(String[] args) {
        isGitletExist();
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        statusFuc();
    }

    private static void checkout(String[] args) {
        isGitletExist();
        checkoutFuc(args);
    }

    private static void branch(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        branchFuc(args[1]);
    }

    private static void rmbranch(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        rmbranchFuc(args[1]);
    }

    private static void reset(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        resetFuc(args[1]);
    }

    private static void merge(String[] args) {
        isGitletExist();
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        mergeFuc(args[1]);
    }
}
