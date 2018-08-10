package top.towing;

import java.util.Random;
import org.apache.log4j.Logger;

public class LoggingThread implements Runnable {	
	static final Logger logger = Logger.getLogger(LoggingThread.class);
	
	static Random sleepRandom = new Random(); 
	private String threadName;
	
	public LoggingThread(String threadName) {
		this.threadName = threadName;
	}
	
	public void run() {
		int counter = 0;
		while(counter < 20) {
			try {
				Thread.sleep(sleepRandom.nextInt(1000));
				logger.debug(String.format("[%s] Print log entry %s", this.threadName, counter++));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
