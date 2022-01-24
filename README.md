### 一、Minio-demo
minio的Java API开发教程。


官方教程文档：https://docs.min.io/?ref=con


### 二、Minio 安装
1.进入官网进行下载，只需下载服务端即可。
```
https://min.io/download
```


2.将下载`` minio.exe ``文件指定位置，我这里放在`` D:\Develop\Minio ``目录下。


3.在该目录下新建`` start.bat ``文件，文件内容如下：
```
minio.exe server D:\Develop\Minio\date
```
其中`` D:\Develop\Minio\date ``表示minio后续数据存放位置。


4.双击启动`` bat ``文件即可，控制台会自动显示访问地址以及默认的登录账号。


### 三、Minio 后台运行
后台启动的话就无需前台一直开着命令行窗口，更便捷。


1.WinSW下载
https://github.com/winsw/winsw/releases


2.创建配置文件
xml文件必须和WinSW文件名一致，将`` WinSW.xml ``文件和`` WinSW.exe ``置于`` minio.exe ``同级目录。
```xml
<service>
    <id>MinioServer</id>
    <name>MinioServer</name>
    <description>minio文件存储服务器</description>
    <!-- 可设置环境变量 -->
    <env name="HOME" value="%BASE%"/>
    <executable>%BASE%\minio.exe</executable>
    <arguments>server "%BASE%\data"</arguments>
    <!-- <logmode>rotate</logmode> -->
    <logpath>%BASE%\logs</logpath>
    <log mode="roll-by-size-time">
      <sizeThreshold>10240</sizeThreshold>
      <pattern>yyyyMMdd</pattern>
      <autoRollAtTime>00:00:00</autoRollAtTime>
      <zipOlderThanNumDays>5</zipOlderThanNumDays>
      <zipDateFormat>yyyyMMdd</zipDateFormat>
    </log>
</service>
```


3.在该目录下新建命令窗口，输入：`` WinSW.exe install ``。


4.Windows进入服务找到`` WinSW ``服务启动即可。


5.访问路径：`` localhost:9000 ``，默认账号密码：`` minioadmin，minioadmin ``


6.修改账号密码
进入目录：minio\data\.minio.sys\config，修改config.json文件中的信息即可。
```js
[{"key":"access_key","value":"username"},{"key":"secret_key","value":"password"}]
```


7.关闭Minio服务直接关闭 WinSW 服务即可。