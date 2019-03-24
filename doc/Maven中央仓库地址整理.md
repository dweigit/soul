## Maven中央仓库地址整理
Maven是官方的库在国外，下载速度很慢。国内oschina的maven服务器很早之前就关了。今天发现阿里云的一个中央仓库，亲测可用。

##### Maven中央仓库地址：
-  http://central.maven.org/maven2/（推荐）
-  http://repo1.maven.org/maven2
-  http://repo2.maven.org/maven2/ 私服nexus工具使用
-  http://maven.aliyun.com/nexus/content/groups/public/  阿里云（强力推荐）
-  http://mvnrepository.com/ （推荐）
-  http://www.sonatype.org/nexus/ 私服nexus工具使用
-  http://uk.maven.org/maven2/
-  http://repository.jboss.org/nexus/content/groups/public
-  http://mirrors.ibiblio.org/maven2/

##### AliRepo Repositories View
Repository|Type|Policy|Path
-----|------|----|------
apache snapshots	|proxy	|SNAPSHOT	|https://maven.aliyun.com/repository/apache-snapshots
central	|proxy	|RELEASE	|https://maven.aliyun.com/repository/central
google	|proxy	|RELEASE	|https://maven.aliyun.com/repository/google
gradle-plugin	|proxy	|RELEASE	|https://maven.aliyun.com/repository/gradle-plugin
jcenter	|proxy	|RELEASE	|https://maven.aliyun.com/repository/jcenter
spring	|proxy	|RELEASE	|https://maven.aliyun.com/repository/spring
spring-plugin	|proxy	|RELEASE	|https://maven.aliyun.com/repository/spring-plugin
public	|group	|RELEASE	|https://maven.aliyun.com/repository/public
releases	|hosted	|RELEASE	|https://maven.aliyun.com/repository/releases
snapshots	|hosted	|SNAPSHOT	|https://maven.aliyun.com/repository/snapshots
grails-core	|proxy	|RELEASE	|https://maven.aliyun.com/repository/grails-core
mapr-public	|proxy	|RELEASE	|https://maven.aliyun.com/repository/mapr-public

国内maven镜像虽然快，但是更新比较慢，国外的仓库由于国内网络的原因，下载简直不能忍，但是更新很快，可以根据自身的情况选择，有些人会花些钱开代理访问外网比较快，建议使用原装。下面是maven库配置

    <mirror>
        <id>oschina-repo</id>
        <name>开源中国镜像</name>
        <mirrorOf>central</mirrorOf>
        <url>可以根据自己的网络情况选填上面的url</url>
    </mirror> 

其实不管是配置哪个地址，个人觉得还是创建一个私人nexus仓库比较好~ 这样团队能够分享，也不用过多的下载版本

#### Maven的setting.xml文件示例
```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <mirrors>
        <!-- 阿里云仓库 -->
        <mirror>
            <id>alimaven</id>
            <mirrorOf>central</mirrorOf>
            <name>aliyun maven</name>
            <url>http://maven.aliyun.com/nexus/content/repositories/central/</url>
        </mirror>
    
        <!-- 中央仓库1 -->
        <mirror>
            <id>repo1</id>
            <mirrorOf>central</mirrorOf>
            <name>Human Readable Name for this Mirror.</name>
            <url>http://repo1.maven.org/maven2/</url>
        </mirror>
    
        <!-- 中央仓库2 -->
        <mirror>
            <id>repo2</id>
            <mirrorOf>central</mirrorOf>
            <name>Human Readable Name for this Mirror.</name>
            <url>http://repo2.maven.org/maven2/</url>
        </mirror>
    </mirrors> 
</settings>
```