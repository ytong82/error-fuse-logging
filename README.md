# error-fuse-logging
Error Fuse Logging Library

整个系统在于设计一个在出现大量日志请求的情况下，可以自动熔断并进入采样模式的日志模块。

系统的设计逻辑在于，用户通过自定义在application.properties文件下的三个参数，它们分别是：

fuse.fusing.time 日志熔断监视器的采样时间

fuse.fuse.threshold 日志熔断监视器的采样阀值

fuse.sampling.ratio 系统熔断之后的采样频率

以模板中配置的参数为例

fuse.fusing.time = 60

fuse.fuse.threshold = 50

fuse.sampling.ratio = 10

通过上述的定义，如果在系统日志熔断监视器采样时间一分钟内，某一条日志打印的次数超过50次，那么日志熔断器会开始工作，对所有这条日志的打印请求进行采样操作，即每10条日志记录的请求，只打印其中的一次。
我们对于相同日志条目的认定逻辑是，如果文件名，类名和行数三个参数一致，我们认为是同一条日志条目的请求。

当日志熔断器没有工作的时候，日志的输出如：

    2018-08-10 17:59:19,185 INFO  () [main] App - Sleep for 60 seconds.
    2018-08-10 18:00:20,097 DEBUG () [pool-2-thread-1] LoggingThread - [Logger2 0] Print log entry 0

当日志熔断器工作的时候，被采样器忽略的日志输出如：

    2018-08-10 17:59:19,185 DEBUG () [pool-1-thread-18] LoggingThread - [SAMPLING][SHOULD BE FILTERED OUT] [Logger 47] Print log entry 19

未被采样器忽略的日志输出如：
    
    2018-08-10 17:59:15,668 DEBUG () [pool-1-thread-15] LoggingThread - [SAMPLING] [Logger 48] Print log entry 18

运行程序，请在程序根目录下输入maven:package
在maven生命周期中，会自动调用定义的Junit测试用例。

下一步的计划是补上Junit测试用例中的断言，保证输出的日志和我们的设计一致。

David Tong (ytong82@aliyun.com)
