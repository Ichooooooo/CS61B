package gitlet;

import java.io.Serializable;

/** Here is my branch(分支) */

public class Branch implements Serializable {
    private String name;
    private String commitSHA1;

    public Branch(String name) {
        this.name = name;
    }

    public Branch(String name, String commitSHA1) {
        this.name = name;
        this.commitSHA1 = commitSHA1;
    }

    public void movePointer (String newPointer) {
        commitSHA1 = newPointer;
    }

    public String getName() {
        return name;
    }

    public String getCommitSHA1() {
        return commitSHA1;
    }
}
