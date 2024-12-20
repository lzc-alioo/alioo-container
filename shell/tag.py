import subprocess
import os

# 定义标签版本和消息
tag_version = "v1.2"
tag_message = "release version 1.2 升级业务插件，引入import.index配置，支持这部分类统一由AliooClassLoader"
commit_message=tag_message

# 定义应用清单
directories = [
    "/Users/mac/work/gitstudy/alioo-boot-maven-plugin",
    "/Users/mac/work/gitstudy/alioo-container-plugin-x1",
    "/Users/mac/work/gitstudy/alioo-container-plugin-x2",
    "/Users/mac/work/gitstudy/alioo-container",
    "/Users/mac/work/gitstudy/classloadertest"
]

def check_local_tag_exists(directory, tag_version):
    try:
        # 检查本地标签是否存在
        subprocess.run(["git", "rev-parse", tag_version], cwd=directory, check=True)
        return True
    except subprocess.CalledProcessError:
        return False

def check_remote_tag_exists(directory, tag_version):
    try:
        # 检查远程标签是否存在
        subprocess.run(["git", "ls-remote", "--tags", "origin", tag_version], cwd=directory, check=True)
        return True
    except subprocess.CalledProcessError:
        return False

def delete_local_tag(directory, tag_version):
    try:
        # 删除本地标签
        subprocess.run(["git", "tag", "-d", tag_version], cwd=directory, check=True)
        print(f"Deleted existing local tag {tag_version} in {directory}")
    except subprocess.CalledProcessError as e:
        print(f"Failed to delete local tag {tag_version} in {directory}: {e}")

def delete_remote_tag(directory, tag_version):
    try:
        # 删除远程标签
        subprocess.run(["git", "push", "origin", f":refs/tags/{tag_version}"], cwd=directory, check=True)
        print(f"Deleted existing remote tag {tag_version} in {directory}")
    except subprocess.CalledProcessError as e:
        print(f"Failed to delete remote tag {tag_version} in {directory}: {e}")

def commit_and_push_changes(directory, branch_name, commit_message):
    try:
        # 添加所有更改到暂存区
        subprocess.run(["git", "add", "."], cwd=directory, check=True)

        # 提交更改
        subprocess.run(["git", "commit", "-m", commit_message], cwd=directory, check=True)

        # 推送更改到远程仓库
        subprocess.run(["git", "push", "origin", branch_name], cwd=directory, check=True)

        print(f"Successfully committed and pushed changes for {directory}")
    except subprocess.CalledProcessError as e:
        print(f"Error committing and pushing changes for {directory}: {e}")

# 遍历每个目录并执行标签创建和推送操作
for directory in directories:
    try:
        # 切换到目标目录
        os.chdir(directory)

        # 提交和推送更改
        commit_and_push_changes(directory, "main", commit_message)

        # 检查本地标签是否存在，如果存在则删除
        if check_local_tag_exists(directory, tag_version):
            delete_local_tag(directory, tag_version)

        # 检查远程标签是否存在，如果存在则删除
        if check_remote_tag_exists(directory, tag_version):
            delete_remote_tag(directory, tag_version)

        # 创建标签
        subprocess.run(["git", "tag", tag_version, "-m", tag_message], cwd=directory, check=True)

        # 推送标签到远程仓库
        subprocess.run(["git", "push", "origin", tag_version], cwd=directory, check=True)

        print(f"Successfully tagged and pushed {tag_version} for {directory}")
    except subprocess.CalledProcessError as e:
        print(f"Error processing {directory}: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
