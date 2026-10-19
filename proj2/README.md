# Gitlet

基于 UC Berkeley CS61B Spring 2021 Project 2，使用 Java 实现的简易版本控制系统，支持文件快照、版本恢复、历史查询、分支管理和合并。

## 功能

| 功能 | 命令 |
|---|---|
| 初始化仓库 | `init` |
| 暂存、提交与移除文件 | `add`、`commit`、`rm` |
| 查询状态与历史 | `status`、`log`、`global-log`、`find` |
| 恢复文件与切换分支 | `checkout` |
| 创建与删除分支 | `branch`、`rm-branch` |
| 回退提交与合并分支 | `reset`、`merge` |

## 实现概述

- **内容寻址存储**：使用 SHA-1 标识 blob 与 commit，相同文件内容共用一个 blob。
- **仓库持久化**：数据保存在 `.gitlet/` 中，Commit 和 Branch 通过 Java 序列化保存；暂存区保存执行 `add` 时的文件副本。
- **文件级三方合并**：提交历史以 DAG 表示，通过 DFS 收集祖先、BFS 搜索公共祖先，再比较文件版本，处理新增、修改、删除及冲突。
- **公共逻辑复用**：普通提交与合并共用暂存区应用逻辑；分支切换与 reset 共用文件恢复和暂存区清理操作。

类结构、算法与存储布局见 [设计文档](gitlet-design.md)。

## 构建

环境要求：JDK、GNU Make；运行测试另需 Python 3。已在 JDK 21 下验证，Java 源码无第三方依赖。

在 CS61B 仓库根目录执行：

```bash
cd proj2
make
```

编译产物位于 `proj2/gitlet/`。本项目使用 Makefile 构建，`pom.xml` 保留课程配置，不作为本说明的构建入口。

## 使用示例

在一个空目录中执行以下命令，将 `/path/to/CS61B/proj2` 替换为本机 `proj2` 的绝对路径。`-cp` 指定编译后的类所在目录，当前目录则作为 Gitlet 工作区。

```bash
java -cp /path/to/CS61B/proj2 gitlet.Main init
printf 'hello\n' > hello.txt
java -cp /path/to/CS61B/proj2 gitlet.Main add hello.txt
java -cp /path/to/CS61B/proj2 gitlet.Main commit "first commit"
java -cp /path/to/CS61B/proj2 gitlet.Main log
```

## 测试

在 `proj2` 目录执行：

```bash
make check TESTER_FLAGS="--tolerance=0"
```

复用课程测试框架，覆盖暂存快照、缩写 ID、版本恢复、分支操作、多次合并及冲突处理等场景。严格模式下 **37/37 条用例通过**（4 条课程样例、33 条补充用例），包括 reset 无效缩写 ID 的回归测试。

详细结果见 [测试记录](testing/TEST-RESULTS.md)。该结果不代表课程完整评分测试或代码覆盖率。

## 已知限制

- 仅支持当前目录下的普通文件，不支持子目录、远程同步和 `.gitignore`；存储格式不与 Git 兼容。
- 合并采用文件级比较。发生冲突时，会立即创建包含冲突标记的提交，需手动修改后另行提交。
- 切换分支与 merge 会检查未跟踪文件冲突，但可能覆盖已跟踪文件的未暂存修改。操作前请提交或备份重要文件。

## 来源

项目规格、`Utils` 等辅助类、Makefile 和测试框架来自 UC Berkeley CS61B 课程材料。`Main`、`Repository`、`Commit`、`Branch` 的业务逻辑在课程骨架基础上实现。
