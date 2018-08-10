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

通过上述的定义，如果在系统日志熔断监视器采样时间一分钟内，某一条的日志打印的次数超过50次，那么日志熔断器会开始工作，对所有这条日志的打印请求进行采样操作，即每10条日志记录的请求，只打印其中的一次。
