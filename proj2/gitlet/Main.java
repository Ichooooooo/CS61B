package gitlet;

import java.util.ArrayList;
import java.util.List;

import static gitlet.Repository.*;
import static gitlet.Utils.join;
import static gitlet.Utils.plainFilenamesIn;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author icovo
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  Runs one of here commands:
     *  Init  --  init the basic repo
     *                  .gitlet / -- top level folder
     *                          Blob / -- dir
     *                          Commit / -- dir
     *                          Stage / -- dir
     *                          Branch / -- dir
     *                          HEAD.txt / -- file
     *
     *  Add [file name]  --  add file to Stage
     *
     *  commit [message]  -- commit
     *
     *  rm [file name]  -- rm
     *
     *  log  -- log
     *
     *  global-log   -- global-log
     *
     *  find [commit message]  -- find
     *
     *  status  --  status
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
            case "status":
                status(args);
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
}
