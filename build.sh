##################################################################################################
# 文件名：build.sh
# 输入的命令格式为： ./build.sh -p {buildType}  -o {outPath}
#
# 1. buildType可以是以下值： debug, release, staging, production。
# 2. outPath为构建结果存放的地方
#
#################################################################################################

#!/bin/sh

echo "param2 : $2 , param4 : $4"

apk_out_path="./app/build/outputs"

if [ "$2" = "debug" ];then
        gradle_arg="assemble360Debug"
elif [ "$2" = "release" ];then
        gradle_arg="assemble360Debug"
elif [ "$2" = "staging" ];then
        gradle_arg="assemble360Staging"
else
        gradle_arg="assembleRelease"
fi
echo $gradle_arg

OUT_PATH="$4"
echo $OUT_PATH

sh ./gradlew clean
sh ./gradlew $gradle_arg

echo "------------mv start------------"
mv $apk_out_path/* $OUT_PATH
echo "------------mv end------------"

echo "------------succeed done----------------"
