# 本脚本只是示意，原计划使用shell来编写的，后来发现写shell脚本一来麻烦了，二来windows小伙伴还是用不了，
# 所以改用python来编写了，参见 tag.py

tag_version="v1.1"
tag_message="release versin 1.1 升级业务插件，支持引入第3方插件"

# 针对 阶段性的成果打成tag版本，涉及的应用清单如下
# ~/work/gitstudy/alioo-boot-maven-plugin
# ~/work/gitstudy/alioo-container-plugin-x1
# ~/work/gitstudy/alioo-container-plugin-x2
# ~/work/gitstudy/alioo-container
# ~/work/gitstudy/classloadertest

# 依次进入上述几个目录执行下列命令
cd ~/work/gitstudy/alioo-boot-maven-plugin
git tag ${tag_version} -m ${tag_message}
git push origin ${tag_version}


