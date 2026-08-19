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
        }
    }

    private static void init(String[] args) {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        initFuc();
    }

    private static void add(String[] args) {
        String fileName = args[1];
        if (!join(CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        addFuc(fileName);
    }

    private static void commit(String[] args) {
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        String message = args[1];
        boolean ok = commitFuc(message);
        if (!ok) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static void remove(String[] args) {
        String fileName = args[1];
        boolean ok = removeFuc(fileName);
        if (!ok) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static void log(String[] args) {
        logFuc();
    }

    private static void globalLog(String[] args) {
        globalLogFuc();
    }

    private static void find(String[] args) {
        boolean ok = findFuc(args[1]);
        if (!ok) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    private static void status(String[] args) {
        statusFuc();
    }

    private static void checkout(String[] args) {
        if (Objects.equals(args[1], "--")) {
            System.out.println(1);
            checkoutWithFileNameFuc(args[2]);
        } else if (args.length == 4 && Objects.equals(args[2], "--")) {
            System.out.println(2);
            checkoutWithCommitFuc(args[1], args[3]);
        } else {
            System.out.println(3);
            checkoutWithBranch(args[1]);
        }
    }

    private static void branch(String[] args) {
        if (join(BRANCH_DIR, args[1]).exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        branchFuc(args[1]);
    }

    private static void rmbranch(String[] args) {
        rmbranchFuc(args[1]);
    }

    private static void reset(String[] args) {
        resetFuc(args[1]);
    }
}
