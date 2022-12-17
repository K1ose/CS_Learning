---
title: self-driving_02_ROS
top: false
comment: false
lang: zh-CN
date: 2022-11-12 16:30:29
tags:
categories:
  - study
  - course
  - self-driving
---

# ROS

ROS: The Robot Operating System

## 相关概念

为什么使用ROS：

- 点对点设计
- 分布式设计
- 多语言
- 轻量级
- 免费且开源
- 社区完善

ROS 不是传统意义上的操作系统，而是一种系统软件框架，使用了流行的面向服务SOA的软件技术，通过网络协议将节点间数据通信解耦，以集成不同语言不同功能的代码。

ROS 不是编程语言，是一个函数库，除包含客户端外，还包含一个中心服务器、一系列命令行工具、图形化界面工具以及编译环境。

ROS也不是集成开发环境。

Talker&Listener模型

- 消息以一种publish/subscribe的方式传递
- 节点可以在给定的主体中发布/订阅消息
- 一个节点可以订阅/发布多个不同的主题
- 允许多个节点订阅/发布同一个主题
- 订阅节点和发布节点并不知道相互之间的存在

Catkin

- ROS的一个最简单最常见的编译系统
- ROS对CMake进行了扩展
- 适合ROS进行大型项目，多包，多节点的场景下进行批量编译
- 操作简单，一次配置，多次使用
- 跨依赖项目编译

抽象程度为：catkin_ws->CMakeLists.txt->Makefile

由此，Cmake编译单个节点，catkin可以定义节点与节点之间的依赖关系。

Packages & Catkin Workspaces

- Package是ROS系统中最底层最基本的在组织，存放各种文件：库、工具、可执行文件等
- Catkkin workspaces，包的顶层工作目录，一个catkin workspace包含一个工程下面多个ros package

Package.xml

- 每个包的描述文件，都需要放置在包的根目录下，对包的名字/版本/作者/维护者/依赖关系进行说明

CmakeList.txt

- 定义一个包的编译指令
- cmake不会找package.xml文件，依据cmakelists.txt文件编译需要清晰指出头文件和库文件的指向
- catkin_package(CATKIN_DEPENDS roscpp)声明依赖本包同时需要的其他ros包
- find_package(catkin REQUIRED COMPONENTS...)声明编译本包所需要的其他ros包
- add_executable声明编译本包生成的可执行文件
- target_link_libraries 链接可执行文件和依赖库

ROS NODE

- 一个节点是ROS程序包的一个可执行文件
- ROS节点可以使用ROS客户库与其他节点通信
- 节点可以发布或者接受一个话题
- 节点可以提供或使用某项服务
- 常用命令
  - rosnode list 查看当前注册到ros master的所有节点
  - Rosnode info 查看某个节点的具体信息

ROS TOPIC

- 节点之间通过一个ROS topic来互相通信
- 通过publisher声明所发布topic名称
- 通过subscriber声明所需要监听的topic名称
- 常用命令
  - Topic list 查看当前注册到ros master的所有topic列表
  - rostopic echo 把当前topic输出到控制台

ROS PARAM

- Rosparam命令允许在ROS参数服务器上从存储和赋值数据，参数服务器可以存储浮点型、整型、布尔值、字典和列表值
- 小型KV库
- 常用命令
  - rosparam set 设置参数
  - rosparam get 获取参数
  - rosparam list 查看当前服务器上的参数

安装&配置

- 在[这里](https://wiki.ros.org/kinetic/Installation/Ubuntu)安装Ubuntu1604所对应的kinetic版本的ROS。

- ROS Workspace 
  
  ```shell
  mkdir -p ~/catkin_ws/src
  cd ~/catkin_ws
  catkin_make
  
  #在~/.bashrc中添加
  source /home/klose/catkin_ws/devel/setup.bash
  ```

## code

- flora_talker
- flora_listener
- flora_msgs

### flora_talker

```shell
# ~/catkin_ws/src
$ catkin_create_pkg flora_say std_msgs roscpp
```

生成了flora_say文件夹

```cpp
// flora_say/src/ 中新建 flora_say_node.cpp 文件
#include "ros/ros.h"
#include "std_msgs/String.h"
#include "sstream"
#include "flora_msgs/FloraMsg.h"

int main(int argc, char **argv){
    ros::init(argc, argv, "flora_talker");
    ros::NodeHandle n;
    ros::Publisher flora_pub = n.advertise<std_msgs::String>("/flora_topic", 10);
    ros::Publisher flora_pub_new = n.advertise<flora_msgs::FloraMsg>("/flora_topic_new", 10);
    ros::Rate loop_rate(10);
    int count = 0;

    while(ros::ok()){
        std_msgs::String msg;
        std::stringstream ss;
        ss << "hello, flora" << count;
        count ++;
        msg.data = ss.str();
        flora_pub.publish(msg);

        // 参数中心
        std::string param_string;
        n.param<std::string>("myparam", param_string,"love you, flora");

        flora_msgs::FloraMsg floraMsg;
        floraMsg.id = count;
        floraMsg.detail = param_string;

        flora_pub_new.publish(floraMsg);

        loop_rate.sleep();
    }

}
```

修改 CMakeLists.txt ：

```makefile
cmake_minimum_required(VERSION 3.0.2)
project(flora_say)

## Compile as C++11, supported in ROS Kinetic and newer
# add_compile_options(-std=c++11)

## Find catkin macros and libraries
## if COMPONENTS list like find_package(catkin REQUIRED COMPONENTS xyz)
## is used, also find other catkin packages
find_package(catkin REQUIRED COMPONENTS
  roscpp
  std_msgs
        flora_msgs
)

...

###################################
## catkin specific configuration ##
###################################
## The catkin_package macro generates cmake config files for your package
## Declare things to be passed to dependent projects
## INCLUDE_DIRS: uncomment this if your package contains header files
## LIBRARIES: libraries you create in this project that dependent projects also need
## CATKIN_DEPENDS: catkin_packages dependent projects also need
## DEPENDS: system dependencies of this project that dependent projects also need
catkin_package(
  #INCLUDE_DIRS include
  #LIBRARIES flora_say
  CATKIN_DEPENDS roscpp std_msgs flora_msgs
  #DEPENDS system_lib
)

###########
## Build ##
###########

## Specify additional locations of header files
## Your package locations should be listed before other locations
include_directories(
# include
  ${catkin_INCLUDE_DIRS}
)

## Declare a C++ library
# add_library(${PROJECT_NAME}
#   src/${PROJECT_NAME}/flora_say.cpp
# )

## Add cmake target dependencies of the library
## as an example, code may need to be generated before libraries
## either from message generation or dynamic reconfigure
# add_dependencies(${PROJECT_NAME} ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})

## Declare a C++ executable
## With catkin_make all packages are built within a single CMake context
## The recommended prefix ensures that target names across packages don't collide
add_executable(${PROJECT_NAME}_node src/flora_say_node.cpp)

## Rename C++ executable without prefix
## The above recommended prefix causes long target names, the following renames the
## target back to the shorter version for ease of user use
## e.g. "rosrun someones_pkg node" instead of "rosrun someones_pkg someones_pkg_node"
# set_target_properties(${PROJECT_NAME}_node PROPERTIES OUTPUT_NAME node PREFIX "")

## Add cmake target dependencies of the executable
## same as for the library above
add_dependencies(${PROJECT_NAME}_node ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})

## Specify libraries to link a library or executable target against
target_link_libraries(
        ${PROJECT_NAME}_node
        ${catkin_LIBRARIES}
)

```

修改 package.xml，引入依赖

```xml
...
  <!-- Use build_depend for packages you need at compile time: -->
     <build_depend>message_generation</build_depend>
  <!-- Use build_export_depend for packages you need in order to build against this package: -->
  <!--   <build_export_depend>message_generation</build_export_depend> -->
  <!-- Use buildtool_depend for build tool packages: -->
  <!--   <buildtool_depend>catkin</buildtool_depend> -->
  <!-- Use exec_depend for packages you need at runtime: -->
     <exec_depend>message_runtime</exec_depend>
  <!-- Use test_depend for packages you need only for testing: -->
  <!--   <test_depend>gtest</test_depend> -->
  <!-- Use doc_depend for packages you need only for building documentation: -->
  <!--   <doc_depend>doxygen</doc_depend> -->
  <buildtool_depend>catkin</buildtool_depend>
  <build_depend>roscpp</build_depend>
  <build_depend>std_msgs</build_depend>
  <build_depend>flora_msgs</build_depend>
  <build_export_depend>roscpp</build_export_depend>
  <build_export_depend>std_msgs</build_export_depend>
  <exec_depend>roscpp</exec_depend>
  <exec_depend>std_msgs</exec_depend>
  <exec_depend>flora_msgs</exec_depend>
...
```

回到catkin_ws文件夹执行`catkin_make`

### flora_listener

```cpp
#include "ros/ros.h"
#include "std_msgs/String.h"

void floraCallback(const std_msgs::String::ConstPtr& msg){
    ROS_INFO("I heard %s", msg->data.c_str());
}
int main(int argc, char **argv){
    ros::init(argc, argv, "flora_listener");
    ros::NodeHandle n;
    ros::Subscriber sub = n.subscribe("/flora_topic", 10, floraCallback);
    ros::spin();
    return 0;
}
```

修改 CMakeLists.txt ：

```cpp
cmake_minimum_required(VERSION 3.0.2)
project(flora_listen)

## Compile as C++11, supported in ROS Kinetic and newer
# add_compile_options(-std=c++11)

## Find catkin macros and libraries
## if COMPONENTS list like find_package(catkin REQUIRED COMPONENTS xyz)
## is used, also find other catkin packages
find_package(catkin REQUIRED COMPONENTS
  roscpp
  std_msgs
)
    catkin_package(
#  INCLUDE_DIRS include
#  LIBRARIES flora_listen
  CATKIN_DEPENDS roscpp std_msgs
#  DEPENDS system_lib
)

###########
## Build ##
###########

## Specify additional locations of header files
## Your package locations should be listed before other locations
include_directories(
# include
  ${catkin_INCLUDE_DIRS}
)

## Declare a C++ library
# add_library(${PROJECT_NAME}
#   src/${PROJECT_NAME}/flora_listen_node.cpp
# )

## Add cmake target dependencies of the library
## as an example, code may need to be generated before libraries
## either from message generation or dynamic reconfigure
# add_dependencies(${PROJECT_NAME} ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})

## Declare a C++ executable
## With catkin_make all packages are built within a single CMake context
## The recommended prefix ensures that target names across packages don't collide
 add_executable(${PROJECT_NAME}_node src/flora_listen_node.cpp)

## Rename C++ executable without prefix
## The above recommended prefix causes long target names, the following renames the
## target back to the shorter version for ease of user use
## e.g. "rosrun someones_pkg node" instead of "rosrun someones_pkg someones_pkg_node"
# set_target_properties(${PROJECT_NAME}_node PROPERTIES OUTPUT_NAME node PREFIX "")

## Add cmake target dependencies of the executable
## same as for the library above
 add_dependencies(${PROJECT_NAME}_node ${${PROJECT_NAME}_EXPORTED_TARGETS} ${catkin_EXPORTED_TARGETS})

## Specify libraries to link a library or executable target against
 target_link_libraries(${PROJECT_NAME}_node
   ${catkin_LIBRARIES}
 )

```

### flora_msg

```shell
# ~/catkin_ws
$ cat src/flora_msgs/msg/FloraMsg.msg 
string detail
int32 id
```

### 运行发布

ros master

```
roscore
```

ros node

```
rosnode list
rosrun flora_say flora_say_node
```

ros topic

```
rostopic list
rostopic echo /flora_say
```

## docker

[docker hub](hub.docker.com)

[ros kinetic](https://hub.docker.com/_/ros/tags?page=1&name=kinetic)

```
$ sudo docker pull ros:kinetic
kinetic: Pulling from library/ros
61e03ba1d414: Pull complete 
4afb39f216bd: Pull complete 
e489abdc9f90: Pull complete 
999fff7bcc24: Pull complete 
7dee46e5af81: Pull complete 
26d1bbf7dea9: Pull complete 
0662b1269a41: Pull complete 
f759c9d3424a: Pull complete 
defe81202a2c: Pull complete 
6b7c066b85ea: Pull complete 
b8274d96e502: Pull complete 
baea347ac716: Pull complete 
Digest: sha256:29c7cb9305475baba4aee5c0b944dca3514c38c9d7b90c5830dfc72ca454ceab
Status: Downloaded newer image for ros:kinetic
docker.io/library/ros:kinetic
$ sudo docker images
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
ros          kinetic   a3c5711abb29   16 months ago   1.13GB
```

build

```
$ sudo docker run -itd -v$(pwd):/data ros:kinetic
4a9c7b8170285696db2d1575d593fdc53b387127b793c8692f534dd31a21d0de
```

- -itd
- -v$(pwd):/data

```
$ sudo docker ps
CONTAINER ID   IMAGE         COMMAND                  CREATED         STATUS         PORTS     NAMES
4a9c7b817028   ros:kinetic   "/ros_entrypoint.sh …"   2 minutes ago   Up 2 minutes             awesome_einstein
```

创建`catkin_ws`文件夹，用于编译项目：

```
root@4a9c7b817028:/# mkdir catkin_ws
root@4a9c7b817028:/# cp -r /data/src/ ./catkin_ws/
```

将环境变量配置好：

```
root@4a9c7b817028:/catkin_ws# echo 'source /opt/ros/kinetic/setup.bash' >> /root/.bashrc
root@4a9c7b817028:/catkin_ws# source /root/.bashrc 
```

编译

```
root@4a9c7b817028:/catkin_ws# catkin_make
```

image制作分发

```
klose@self-driving:~/catkin_ws$ sudo docker ps -a
CONTAINER ID   IMAGE         COMMAND                  CREATED         STATUS         PORTS     NAMES
4a9c7b817028   ros:kinetic   "/ros_entrypoint.sh …"   8 minutes ago   Up 8 minutes             awesome_einstein
klose@self-driving:~/catkin_ws$ sudo docker commit 4a9c7b817028 myimage
sha256:b17e1ef21afbaaf7e04cc0c8a52f3a8118ece554b568e03c63577f8183429e0f
klose@self-driving:~/catkin_ws$ sudo docker images
REPOSITORY   TAG       IMAGE ID       CREATED         SIZE
myimage      latest    b17e1ef21afb   5 seconds ago   1.13GB
ros          kinetic   a3c5711abb29   16 months ago   1.13GB
```

## 任务

- 建立一个talker listener模型
- 构建自己的message，包含两个int32类型的值
- Talker通过该message发送随即两个数字，Listener收到后分别进行累加后输出
- 使用Docker打包上传共享镜像

