## 支持断点续传的多文件、多线程的下载器

SpringBoot + （JDK）ScheduledThreadPoolExecutor 

### 1.背景说明

基于HTTP协议在网络上传输大文件有以下弊端（含部分个人理解）

1. HTTP没有规定传输内容的大小，但是随着传输内容的增大，网络链路距离的增加基于TCP传输协议的HTTP报文就会越不稳定

2. 传输大文件会占用昂贵的网络带宽资源

3. 基于spring和tomcat的web应用处理HTTP请求是采用链式处理的，一个过大的HTTP请求会占用服务器的内存资源，导致内存不够用等问题

4. 如果网络波动，重传也是会浪费网络带宽等资源

5. 原始传输不支持断点续传，大文件传到99%失败，要重新下载实则浪费资源等

6. 参见知乎回答，[大文件传输主要技术瓶颈都有哪些?如何处理的? - ZeroOne的回答 - 知乎](
   https://www.zhihu.com/question/39593108/answer/250480994)

   ​

### 2.设计思路

1. 文件分块的方式请求与传输。每个HTTP请求只会请求一个文件块去下载。

2. 多线程下载文件块，减少因网络IO带来的负面效率影响。

3. 客户端维护待下载文件以及其对应的文件块和下载状态，以便记忆用户下载记录、文件下载完成判断、多文件同时下载的支持、服务端挂了，客户端梯度行的尝试下载与中断下载逻辑判断、随机文件读写的方式保存文件块到本地。

   #### 详细设计

   结构图如下

   ![](http://onhavnxj2.bkt.clouddn.com/%E5%A4%9A%E7%BA%BF%E7%A8%8B%E4%B8%8B%E8%BD%BD%E5%99%A8%E6%9E%B6%E6%9E%84%E5%9B%BE.jpg)

   ##### 整体思路：

   初始化工作，下载器downloader接收到传入的文件名（服务端文件标示）和大小之后，对文件进行分块，每一块文件包含文件的属性，文件块之间相互独立。把文件块全部丢进待下载队列waitBlocks中，同时初始化filesTotal（每个文件名与总块数的映射），filesCurrent（每个文件名与成功下载的文件块数的映射），dropBlock（每个文件名与丢弃的文件块数之间的映射）键值对变量。

   ​

   初始化完成后即启动3个下载线程（downloadThread），一个写线程（writeThread），1个重任务线程（reTaskThread），1个善后线程（dropStoreThread），它们的工作就像上图描述的那样，职责单一的处理任务。详细如下：

   下载线程：从待下载队列中取出文件块去下载，下载成功把文件块添加到下载成功队列中，下载失败则把文件块添加到下载失败队列中，失败包含两种：其一为返回的response状态吗不是ok，待下载的服务端文件可能被修改或则不存在了，这个时候启动计数器，失败5次则丢弃文件块（避免浪费服务端连接资源），不把文件块加入到下载失败队列中，同时dropBlock映射更新；另外一种是获取不到服务端的响应，可能服务端挂了，网络链路有问题，这个时候会暂停所有下载线程，同时把对应的文件块加入到下载失败队列中，reTaskThread会用一定的策略（梯度增加时间的休眠，唤醒后打开下载线程尝试连接下载）把下载失败的文件块放回到待下载队列中，这样能控制下载线程和服务器的交互，避免网络中断还不间断的请求服务器。

   写线程就比较单一，只负责把下载成功队列中的文件块取出来，写入到磁盘，及时释放内存，同时更新filesCurrent状态。

   善后线程工作比较重要，它有两种工作状态：其一，当filesTotal和filesCurrent中维护的相同文件的文件块数相等时，表示成功下载文件，这时需要把写线程写的临时文件更名，同时和服务器交互，文件已经下载完成，服务端把下载标志位置为有效，以后获取的下载任务将不包含这个文件，和服务器成功交互后，会删除filesTotal和filesCurrent维护的对应文件映射。另一个是，如果dropBlock所维护的文件块不为0，并且dropblock的数量加上filesCurrent中对应文件的文件块数量等于filesTotal总数量时，说明此文件下载失败，会把writeThread写入的临时文件删除，同时把三个映射列表更新（移除对应的文件及其映射），等待下次获取待下载文件时下载。

   ##### 数据结构说明：

   下载器控制类ControlBean

   ```java
   /**
    * 控制bean，用来表示提示文件下载成功，用来处理成功关闭程序
    * @author wangyonghao
    */
   public class ControlBean {
       /**每一个文件的存储名和总块数的映射关系*/
       public static ConcurrentHashMap<String,AtomicInteger> filesTotal = new ConcurrentHashMap<String,AtomicInteger>();
       /**当前每一个文件的存储名和下载成功数的映射关系*/
       public static ConcurrentHashMap<String,AtomicInteger> filesCurrent = new ConcurrentHashMap<String,AtomicInteger>();
       /**每一个文件的丢弃文件块数*/
       public static ConcurrentHashMap<String,AtomicInteger> dropBlock = new ConcurrentHashMap<String,AtomicInteger>();
       /**待下载文件块队列*/
       public static LinkedBlockingQueue<DownloadBlock> waitBlocks = new LinkedBlockingQueue<DownloadBlock>();
       /**成功下载文件块队列*/
       public static LinkedBlockingQueue<DownloadBlock> successBlocks = new LinkedBlockingQueue<DownloadBlock>();
       /**失败下载文件块队列*/
       public static LinkedBlockingQueue<DownloadBlock> loseBlocks = new LinkedBlockingQueue<DownloadBlock>();
       /**继续下载标志（中断下载的变量）*/
       public static volatile boolean continueDownload = true;
   }
   ```

   filestotal：服务端文件名<------>文件分块的总块数

   filesCurrent:服务端文件名<------>成功下载的文件块数

   dropBlock：服务端文件名<------>已下载失败丢掉的文件块数

   waitBlocks：包含所有待下载文件块的阻塞队列

   successBlocks：包含成功下载的文件块阻塞队列

   loseBlocks：包含下载失败或响应失败的文件块的阻塞队列

   #### 文件块结构DownloadBlock

   ```java
   /**
    * 用来标记待下载文件的文件块信息
    * url              ： 下载文件的url
    * serverFilePath   ： 文件保存在服务端的路径（路径+文件名）
    * storeFIlePath    ： 文件保存在客户端的路径（路径+文件名）
    * number           ： 文件快的编号
    * start            ： 文件块的偏移量
    * end              ： 文件块的结束位置
    * content          ： 文件块的内容
    * blockState       ： 文件块的状态
    * @author wangyonghao
    */
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class DownloadBlock {

       /**下载文件的url*/
       private String url;
       /**文件保存在服务端的路径（路径+文件名）*/
       private String serverFilePath;
       /**文件保存在客户端的路径（路径+文件名）*/
       private String storeFilePath;
       /**文件块的编号（一个文件一套编号）*/
       private int number;
       /**文件快的开始位置*/
       private int start;
       /**文件快的结束位置*/
       private int end;
       /**文件块的文件内容*/
       private byte[] content;
       /**文件块下载失败次数*/
       private AtomicInteger failCount;
   }
   ```

   ​



