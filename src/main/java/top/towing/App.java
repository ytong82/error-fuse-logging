package top.towing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class App {
    static final Logger logger = Logger.getLogger(App.class);
    
	public static void main( String[] args ) {
		DOMConfigurator.configure("src/main/resources/log4j.xml");
		logger.info("Start test.");
		
		// start thread pool to run logging thread
		ExecutorService executor = Executors.newFixedThreadPool(20);
		for (int index=0; index<50; index++) {
			String threadName = "Logger " + index;
			Runnable logger = new LoggingThread(threadName);
			executor.execute(logger);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		
		// sleep for 60 seconds
		try {
			logger.info("Sleep for 60 seconds.");
			Thread.sleep(60 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ExecutorService executor2 = Executors.newFixedThreadPool(1);
		for (int index=0; index<2; index++) {
			String threadName2 = "Logger2 " + index;
			Runnable logger2 = new LoggingThread(threadName2);
			executor2.execute(logger2);
		}
		executor2.shutdown();
		while (!executor2.isTerminated()) {
		}
			
		logger.info("Finished test.");
    }		
}
