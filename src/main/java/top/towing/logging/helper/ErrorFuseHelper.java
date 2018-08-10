package top.towing.logging.helper;

import org.apache.log4j.spi.LoggingEvent;

public interface ErrorFuseHelper {
	public boolean checkIfFused(LoggingEvent loggingEvent);
	public int getSamplingRatio();
}
