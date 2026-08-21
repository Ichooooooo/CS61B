# Gitlet 设计文档

Name：icovo

## 类与数据结构

### Main

`Main` 是 Gitlet 的命令行入口类。它不保存仓库状态，主要负责解析命令行参数、进行基础的参数数量检查和仓库初始化检查，并将合法命令分发给 `Repository`。

#### 字段

`Main` 没有需要持久化的字段，也没有实例字段。

### Repository

`Repository` 包含 Gitlet 的主要业务逻辑。大多数方法都是静态方法，因为每次 Gitlet 运行时，只会执行一个命令，而仓库状态保存在磁盘中。

#### 静态字段

1. `CWD`  
   当前工作目录，也就是 Gitlet 正在运行的目录。

2. `GITLET_DIR`  
   `.gitlet` 目录。该目录是否存在，用于判断当前目录是否已经初始化为 Gitlet 仓库。

3. `COMMIT_DIR`  
   用于保存序列化后的 `Commit` 对象。每个文件都以对应 commit 的 SHA-1 值命名。

4. `BLOB_DIR`  
   用于保存文件内容对应的 blob。每个 blob 文件以其内容的 SHA-1 值命名。

5. `STAGE_DIR`  
   暂存区的父目录。

6. `STAGE_ADDITION_DIR`  
   用于保存已经暂存、准备加入下一次 commit 的文件完整内容。文件名与工作目录中的原文件名相同。

7. `STAGE_REMOVE_DIR`  
   用于保存准备从下一次 commit 中删除的文件标记。这里保存的是空标记文件，文件名就是要删除的文件名。

8. `BRANCH_DIR`  
   用于保存序列化后的 `Branch` 对象。每个文件以 branch 名称命名。

9. `HEAD_DIR`  
   一个普通文本文件，保存当前所在 branch 的名称。

### Commit

`Commit` 是一个可序列化的快照对象。它记录一次提交的元数据、父提交关系，以及当前提交所跟踪的文件与 blob 之间的映射关系。

#### 字段

1. `message`  
   commit message。

2. `timestamp`  
   commit 创建的时间。初始 commit 使用 Unix epoch。

3. `trackedFiles`  
   一个 `TreeMap<String, String>`，将每个被跟踪的文件名映射到保存该版本文件内容的 blob SHA-1。

4. `firstFather`  
   第一个父 commit 的 SHA-1。对于初始 commit，该值为 `null`。

5. `secondFather`  
   merge commit 的第二个父 commit 的 SHA-1。对于普通 commit，该值为 `null`。

6. `SHA1`  
   一个 transient 字段，用于表示 commit ID。实际实现中，`getSHA1()` 会序列化 commit 对象，并对序列化结果计算 SHA-1。

### Branch

`Branch` 是一个可序列化、可移动的 commit 指针。

#### 字段

1. `name`  
   branch 名称。

2. `commitSHA1`  
   当前 branch 指向的 commit SHA-1。

## 算法

### Main 命令分发

`Main.main` 首先检查是否至少存在一个命令行参数，并将第一个参数作为命令名称。

除了 `init` 以外，所有命令在执行前都会检查 `.gitlet` 是否存在，以确认当前目录已经初始化。

每个命令对应的小型包装函数负责检查参数数量，并调用 `Repository` 中对应的方法。由于 `checkout` 有三种合法形式，因此它需要单独判断参数格式。

`Main` 本身不保存任何 Gitlet 状态。命令分发完成后，所有仓库操作都由 `Repository` 完成。

### Repository 辅助操作

`Repository` 中的辅助方法将文件系统操作与具体命令逻辑分离。

- Commit 和 branch 对象通过 `Utils.writeObject` 序列化，通过 `Utils.readObject` 恢复。
- Blob ID 通过文件字节内容计算 SHA-1。
- 恢复一个被跟踪的文件时，先通过 commit 的 `trackedFiles` 找到 blob ID，再把对应 blob 写回工作目录。
- `getHEADBranchName`、`getHEADBranch` 和 `getHEADCommit` 依次从 `HEAD` 找到当前 branch，再找到 branch 指向的 commit。
- 缩写 commit ID 通过扫描 commit 文件名，寻找以给定前缀开头的 SHA-1。
- checkout 和 reset 会复用检查 untracked file、恢复完整 commit、删除旧文件和清空暂存区等辅助逻辑。

### init

`initFuc` 首先检查 `.gitlet` 是否已经存在。如果存在，则拒绝重复初始化。

之后创建 Gitlet 所需的目录结构，并创建初始 commit：

- message 为 `initial commit`；
- timestamp 为 `new Date(0L)`；
- 没有父 commit；
- `trackedFiles` 为空。

初始 commit 被序列化保存到 `Commit/`。

随后创建 `master` branch，让它指向初始 commit，并将 branch 序列化保存到 `Branch/master`。

最后，将字符串 `master` 写入 `HEAD`。

### add

`addFuc` 首先检查工作目录中的目标文件是否存在。

接着，将该文件与当前 HEAD commit 中被跟踪的版本进行比较：

- 如果 HEAD 已经跟踪该文件，并且文件内容完全相同，则删除该文件可能存在的 addition 或 removal 暂存记录，因为当前文件已经与 commit 一致。
- 如果该文件已经位于 addition stage 中，则只有当工作区文件再次发生变化时，才替换暂存副本。
- 如果该文件位于 removal stage 中，则取消 removal 标记，并将当前工作区版本加入 addition stage。
- 其他情况下，直接把工作区文件复制到 `Stage/Addition/`。

这样做的关键是：暂存区保存的是执行 `add` 时文件的实际字节内容，而不是仅仅保存一个指向工作区文件的引用。

### commit

`commitFuc` 首先拒绝空的 commit message。

普通 commit 创建时，会复制当前 HEAD commit 的 `trackedFiles`，并把 HEAD commit 设置为新 commit 的第一个父 commit。因此，没有发生变化的文件可以直接继承原来的 blob 引用。

之后处理 `Stage/Addition/` 中的文件：

1. 在新 commit 的 `trackedFiles` 中新增或替换文件名到 blob ID 的映射。
2. 将暂存文件复制到 `Blob/`，并使用文件内容的 SHA-1 作为 blob 文件名。
3. 删除对应的暂存文件。

接着处理 `Stage/Remove/`：

- 从新 commit 的 `trackedFiles` 中删除对应文件；
- 删除 removal marker。

如果 addition stage 和 removal stage 都没有变化，则拒绝创建 commit。

否则：

1. 序列化新 commit；
2. 将当前 branch 指针移动到新 commit；
3. 重新序列化当前 branch。

### rm

`removeFuc` 会同时检查文件是否被 HEAD commit 跟踪，以及文件是否位于 addition stage。

如果 HEAD 正在跟踪该文件：

- 将其加入 removal stage；
- 删除工作目录中的对应文件。

如果该文件之前还位于 addition stage，则移除 addition 记录并转为 removal 标记。

如果 HEAD 不跟踪该文件，但文件位于 addition stage，则只取消 addition 暂存。

如果两种情况都不满足，则输出没有理由删除该文件的错误信息。

### log、global-log 和 find

`logFuc` 从 HEAD commit 开始，不断沿着 `firstFather` 向前遍历，直到初始 commit。每个 commit 都会被反序列化并打印。

如果当前 commit 是 merge commit，还会额外打印两个父 commit SHA-1 的缩写。

`globalLogFuc` 会遍历 `Commit/` 中所有 commit 文件，并打印所有 commit，而不考虑它们是否还能够从某个 branch 到达。

`findFuc` 同样遍历所有 commit，但只输出 commit message 与用户输入完全相同的 commit ID。

### status

`statusFuc` 输出五个部分。

Branches、Staged Files 和 Removed Files 可以直接从对应的持久化目录读取。

当前 branch 通过比较 branch 文件名和 `HEAD` 中保存的 branch 名称确定。

对于 “Modifications Not Staged For Commit”，算法使用一个排序后的 `TreeSet` 保存文件名，并使用一个 map 记录状态是 `modified` 还是 `deleted`。

主要比较两类文件：

1. HEAD commit 中跟踪的文件。  
   如果工作区版本发生变化，而且没有重新 add，则标记为 modified；如果工作区文件被删除，而且没有 stage for removal，则标记为 deleted。

2. addition stage 中的文件。  
   如果文件在 add 之后又发生变化，则标记为 modified；如果 add 之后工作区文件被删除，则标记为 deleted。

对于 “Untracked Files”，算法遍历工作目录中的普通文件，并输出既没有被 HEAD 跟踪、也没有位于 addition stage 中的文件。

### checkout

`checkoutFuc` 区分三种形式。

#### 从 HEAD checkout 文件

加载 HEAD commit，在 `trackedFiles` 中查找对应文件名，然后根据 blob ID 找到 blob，并覆盖或创建工作目录中的文件。

#### 从指定 commit checkout 文件

首先解析完整或缩写的 commit ID，并加载目标 commit。

然后找到该 commit 对应文件的 blob，并复制回工作目录。

#### checkout branch

首先加载目标 branch 和它所指向的 commit。

修改工作区之前，会检查是否存在 untracked file，并且这个 untracked file 会被目标 commit 中的文件覆盖。

如果不存在这种危险情况，则：

1. 将目标 commit 中跟踪的所有文件恢复到工作目录。
2. 删除旧 HEAD commit 跟踪、但目标 commit 不再跟踪的文件。
3. 将目标 branch 名称写入 `HEAD`。
4. 清空 addition stage 和 removal stage。

### branch 和 rm-branch

`branchFuc` 创建新的 `Branch` 对象，其 commit 指针初始化为当前 HEAD commit。

创建 branch 不会复制任何 commit。

`rmbranchFuc` 只删除对应的 branch 文件，并不会删除 commit 或 blob。因此删除 branch 不会直接销毁仓库中的历史对象。

当前 branch 不允许被删除。

### reset

`resetFuc` 首先解析完整或缩写的 commit ID，并加载目标 commit。

执行之前，同样检查是否存在会被覆盖的 untracked file。

如果安全，则：

1. 将目标 commit 中跟踪的所有文件恢复到工作目录。
2. 删除旧 HEAD commit 跟踪、但目标 commit 不再跟踪的文件。
3. 将当前 branch 的指针移动到目标 commit，并重新序列化 branch。
4. 清空 addition stage 和 removal stage。

与 checkout branch 不同，`reset` 不改变当前 branch 的名字。

### merge

Merge 被拆分为四个主要阶段：

1. 前置条件检查；
2. split point 搜索；
3. 文件状态比较；
4. merge commit 创建。

#### Merge 前置条件

在修改任何文件之前，`mergeFuc` 检查：

- 暂存区必须为空；
- given branch 必须存在；
- given branch 不能是当前 branch；
- 不能存在会被 merge 覆盖的 untracked file。

然后加载：

- current branch head；
- given branch head；
- split point commit。

如果 given branch head 本身就是 split point，说明 given branch 的内容已经包含在当前 branch 中，因此无需 merge。

如果 current branch head 是 split point，则当前实现会执行给定 branch 的 checkout，并报告 fast-forward。

#### Split Point 搜索

由于 merge commit 可能存在两个父节点，因此 commit 历史实际上构成一个有向无环图，而不只是单链表。

`markCommitList` 从当前 branch head 开始执行深度优先遍历，沿着 first parent 和 second parent 搜索所有祖先 commit，并记录它们的 SHA-1。

然后 `findNearestFather` 从 given branch head 开始执行广度优先搜索。

当 BFS 第一次遇到一个同时存在于 current branch 祖先集合中的 commit 时，就将其作为 split point。

算法使用 visited set 防止 DAG 中的节点被重复处理。

#### Split Point 中已经存在的文件

`handleSpExist` 遍历 split point 跟踪的所有文件。

对于每个文件，比较 current commit 和 given commit 相对于 split point 的状态，将其分类为 unchanged、changed 或 deleted。

主要情况如下：

- 两边都没有变化：不做任何操作；
- current 未修改，given 修改：checkout given 版本并暂存；
- current 修改，given 未修改：保留 current 版本；
- current 删除，given 未修改：保留删除结果；
- current 未修改，given 删除：删除工作区文件并 stage removal；
- 两边都修改成相同内容：不做任何操作；
- 两边修改成不同内容：生成 conflict 文件并暂存；
- 一边修改、一边删除：生成 conflict 文件并暂存；
- 两边都删除：不做任何操作。

#### Split Point 中不存在的文件

`handleSpNotExist` 处理 split point 之后新创建的文件。

- 如果两个 branch 都创建了该文件，而且内容相同，则不做操作。
- 如果两个 branch 都创建了该文件，但内容不同，则产生 conflict。
- 如果只有 current branch 创建，则保留 current 版本。
- 如果只有 given branch 创建，则把 given 版本复制到工作目录并暂存。

#### Conflict 文件

`handleConflictFile` 使用如下结构构造 conflict 文件：

```text
<<<<<<< HEAD
[current branch contents]
=======
[given branch contents]
>>>>>>>
```

如果文件在其中一边不存在，则对应部分不写入文件内容。

生成后的 conflict 文件会被加入 addition stage。

Merge 过程中会记录是否发生过 conflict，以便 merge commit 完成后只输出一次 conflict 提示。

#### Merge Commit

所有文件处理完成之后，`mergeCommit` 创建一个拥有两个父节点的 commit：

- first parent：merge 前的 HEAD commit；
- second parent：given branch head。

新 commit 首先复制第一个父 commit 的 `trackedFiles`，然后像普通 commit 一样应用 addition stage 和 removal stage 中的修改。

完成后：

1. 序列化 merge commit；
2. 将当前 branch 指向新的 merge commit。

Merge commit message 的格式为：

```text
Merged [given branch] into [current branch].
```

## 持久化

Gitlet 必须保证不同的 `java gitlet.Main ...` 运行之间仍然能够保存仓库状态。

本实现将所有跨命令需要保存的状态都存储在 `.gitlet` 目录中。

### 目录结构

```text
.gitlet/
├── Commit/
│   └── <commit SHA-1>        序列化的 Commit 对象
├── Blob/
│   └── <blob SHA-1>          被跟踪文件某一版本的原始内容
├── Stage/
│   ├── Addition/
│   │   └── <file name>       add 或 merge 暂存的完整文件内容
│   └── Remove/
│       └── <file name>       空的删除标记文件
├── Branch/
│   └── <branch name>         序列化的 Branch 对象
└── HEAD                      当前 branch 名称，普通文本
```

### Commit 与 Blob 的持久化

每个 commit 都被序列化保存到：

```text
Commit/<commit SHA-1>
```

Commit 的 `trackedFiles` 不直接保存文件内容，而是保存文件名到 blob SHA-1 的映射。

Blob 使用内容寻址方式保存。

如果两个 commit 中某个文件的内容完全相同，它们计算出的 SHA-1 也相同，因此两个 commit 可以同时引用同一个 blob。

因此，即使程序已经退出，之后的命令仍然可以通过：

```text
Commit
→ trackedFiles
→ blob SHA-1
→ Blob 文件
```

恢复任意一个被跟踪的文件版本。

### 暂存区持久化

执行 `add` 时，Gitlet 会把当时文件的完整字节内容复制到：

```text
Stage/Addition/<file name>
```

这一点非常重要，因为执行 `add` 的 Java 进程随后就会结束。

即使用户之后再次修改工作目录中的文件，下一次执行 `commit` 时，仍然会提交当初真正被 add 的版本，而不是修改后的版本。

类似地，`rm` 会在：

```text
Stage/Remove/
```

中创建持久化的 removal marker。

因此，即使执行 `rm` 的进程已经退出，之后的 `commit` 仍然知道哪些文件需要停止跟踪。

成功 commit，或者执行明确要求清空暂存区的操作之后，这些暂存文件会被删除。

### Branch 与 HEAD 的持久化

每个 branch 独立序列化保存到：

```text
Branch/<branch name>
```

其中保存 branch 当前指向的 commit ID。

`HEAD` 只保存当前活动 branch 的名称。

因此之后重新运行 Gitlet 时，可以按照如下路径恢复当前状态：

```text
HEAD 文本
→ Branch/<name>
→ branch.commitSHA1
→ Commit/<SHA-1>
```

移动 branch 时，需要重新序列化更新后的 `Branch` 对象。

切换 branch 时，则需要重写 `HEAD`。

### 不同命令之间的持久化示例

对于先执行：

```text
add wug.txt
```

然后在之后另一次程序运行中执行：

```text
commit "modify wug"
```

过程如下：

1. `add` 把当时 `wug.txt` 的完整内容复制到 `Stage/Addition/wug.txt`。
2. `add` 进程退出，但暂存文件仍然保存在磁盘上。
3. 后续 `commit` 读取该暂存副本，计算 blob SHA-1。
4. 将暂存内容保存到 `Blob/`。
5. 在新 commit 中记录对应 blob ID。
6. 序列化新 commit。
7. 移动当前 branch 指针。

对于先执行：

```text
branch feature
```

之后再执行：

```text
checkout feature
```

过程如下：

1. `branch` 将 `Branch/feature` 序列化到磁盘，其中保存当前 commit ID。
2. 程序退出。
3. 后续 `checkout feature` 读取该 branch 对象。
4. 根据 branch 指针加载目标 commit。
5. 使用 commit 对应的 blob 恢复工作区。
6. 将 `HEAD` 改写为 `feature`。

对于执行 `commit` 后，在之后另一次程序运行中执行 `log`：

1. 新 commit 及其父 commit ID 被序列化到以 SHA-1 命名的文件中。
2. 当前 branch 被重新序列化，使它指向新的 commit。
3. 后续 `log` 读取 `HEAD`。
4. 再通过 branch 找到最新 commit。
5. 通过各 commit 持久化保存的 first parent ID 不断向前遍历。

对于 `merge`：

1. 两个 branch 指针以及构成 commit DAG 的 commit 都已经持久化存在。
2. Merge 生成的暂存文件继续使用普通 add/rm 相同的磁盘暂存机制。
3. 最终 merge commit 保存两个父 commit ID，因此 DAG 结构可以跨程序运行继续存在。
4. 当前 branch 被重新序列化，并移动到新的 merge commit。

因此，本实现中的 Java 进程本身不需要在不同命令之间保留任何内存状态。

Gitlet 的权威状态始终来自 `.gitlet` 目录中的持久化数据。
