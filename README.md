### minio-demo
Java基于minio开发教程。


### minio 安装
1.进入官网进行下载，只需下载服务端即可。
```
https://min.io/download
```


2.将下载`` minio.exe ``文件指定位置，我这里放在`` D:\Develop\Minio ``目录下。


在该目录下新建`` start.bat ``文件，文件内容如下：
```
minio.exe server D:\Develop\Minio\date
```


`` D:\Develop\Minio\date ``表示minio后续数据存放位置。


3. 双击启动`` bat ``文件即可，控制台会自动显示访问地址以及默认的登录账号。
