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
     *  Runs one of three commands:
     *  init  --  init the basic repo
     *
     *  add [file name]
     *
     *  commit [message]
     *
     *  rm [file name]
     *
     *  log
     *
     *  global-log
     *
     *  find [commit message]
     *
     *  status
     *
     *  checkout -- [file name]
     *  checkout [commit id] -- [file name]
     *  checkout [branch name]
     *
     *  branch [branch name]
     *
     *  rm-branch [branch name]
     *
     *  reset [commit id]
     *
     *  merge [branch name]
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
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        initFuc();
    }

    private static void add(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        addFuc(args[1]);
    }

    private static void commit(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        commitFuc(args);
    }

    private static void remove(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        removeFuc(args[1]);
    }

    private static void log(String[] args) {
        isGitletExist();
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        logFuc();
    }

    private static void globalLog(String[] args) {
        isGitletExist();
        if (args.length > 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        globalLogFuc();
    }

    private static void find(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        findFuc(args[1]);
    }

    private static void status(String[] args) {
        isGitletExist();
        if (args.length > 1) {
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
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        branchFuc(args[1]);
    }

    private static void rmbranch(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        rmbranchFuc(args[1]);
    }

    private static void reset(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        resetFuc(args[1]);
    }

    private static void merge(String[] args) {
        isGitletExist();
        if (args.length > 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        mergeFuc(args[1]);
    }
}
