Maven中央仓库地址整理
一直发现常用的oschina maven源一直都没有反应，后面发现原来oschina竟然关闭了maven源服务，后面经同事推荐了阿里云的maven源，这速度杠杠的

Maven 中央仓库地址：
1、http://www.sonatype.org/nexus/  私服nexus工具使用
2、http://central.maven.org/maven2/（推荐）
2、http://mvnrepository.com/ （推荐）
3、http://repo1.maven.org/maven2
4、http://maven.aliyun.com/nexus/content/groups/public/  阿里云  （强力推荐）
5、http://repo2.maven.org/maven2/ 私服nexus工具使用
6、http://uk.maven.org/maven2/
7、http://repository.jboss.org/nexus/content/groups/public
8、http://mirrors.ibiblio.org/maven2/

其实，国内maven镜像虽然快，但是更新比较慢，国外的仓库由于国内网络的原因，下载简直不能忍，但是更新很快，可以根据自身的情况选择，有些人会花些钱开代理访问外网比较快，建议使用原装。下面是maven库配置

<mirror>
<id>oschina-repo</id>
<name>开源中国镜像</name>
<mirrorOf>central</mirrorOf>
<url>可以根据自己的网络情况选填上面的url</url>
</mirror>

其实不管是配置哪个地址，个人觉得还是创建一个私人nexus仓库比较好~ 这样团队能够分享，也不用过多的下载版本