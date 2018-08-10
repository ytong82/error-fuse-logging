package top.towing.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

import top.towing.logging.helper.ErrorFuseHelper;
import top.towing.logging.helper.impl.ErrorFuseHelperImpl;
import top.towing.logging.utils.PropertyUtils;

public class TowingAppenderWrapper extends AppenderSkeleton implements AppenderAttachable {
	private final List<Appender> appenders = new ArrayList<Appender>();
	private Random samplingRandom = new Random();
	private ErrorFuseHelper errorFuseHelper;
	
	public TowingAppenderWrapper() {
		setupFuseService();
	}
	
	private void setupFuseService() {
		int fusingTime = Integer.parseInt(PropertyUtils.getProperty("fuse.fusing.time", "300"));
		int fuseThreshold = Integer.parseInt(PropertyUtils.getProperty("fuse.fuse.threshold", "100"));
		int samplingRatio = Integer.parseInt(PropertyUtils.getProperty("fuse.sampling.ratio", "10"));
		
		this.errorFuseHelper = new ErrorFuseHelperImpl(fusingTime, fuseThreshold, samplingRatio);
	}
	
	public void close() {
		synchronized (appenders) {
			for (Appender appender : appenders) {
				appender.close();
			}
		}
	}

	public boolean requiresLayout() {
		return false;
	}

	public void addAppender(Appender appender) {
		synchronized (appenders) {
            appenders.add(appender);
        }
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAllAppenders() {		
		return Collections.enumeration(appenders);
	}

	public Appender getAppender(String name) {
		synchronized (appenders) {
            for (Appender appender : appenders) {
                if (appender.getName().equals(name)) {
                    return appender;
                }
            }
        }
        return null;
	}

	public boolean isAttached(Appender appender) {
		synchronized (appenders) {
            for (Appender wrapped : appenders) {
                if (wrapped.equals(appender)) {
                    return true;
                }
            }
            return false;
        }
	}

	public void removeAllAppenders() {
		synchronized (appenders) {
            appenders.clear();
        }
	}

	public void removeAppender(Appender appender) {
		synchronized (appenders) {
            for (Iterator<Appender> i = appenders.iterator(); i.hasNext(); ) {
                if (i.next().equals(appender)) {
                    i.remove();
                }
            }
        }
	}

	public void removeAppender(String name) {
		synchronized (appenders) {
            for (Iterator<Appender> i = appenders.iterator(); i.hasNext(); ) {
                if (i.next().getName().equals(name)) {
                    i.remove();
                }
            }
        }
	}

	@Override
	protected void append(LoggingEvent event) {
		if (this.errorFuseHelper.checkIfFused(event)) {
			int samplingRatio = this.errorFuseHelper.getSamplingRatio();
			int _samplingRandom = this.samplingRandom.nextInt(samplingRatio);
			if (_samplingRandom == 0) {
				String messageToChange = String.format("[SAMPLING] %s", event.getMessage());
				LoggingEvent changedEvent = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(),
					event.getLevel(), messageToChange, event.getThreadName(), event.getThrowableInformation(), event.getNDC(),
					event.getLocationInformation(), event.getProperties());
				synchronized (appenders) {
					for (Appender appender : appenders) {
						appender.doAppend(changedEvent);
					}
				}
			} else {
				String messageToChange = String.format("[SAMPLING][SHOULD BE FILTERED OUT] %s", event.getMessage());
				LoggingEvent changedEvent = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(),
					event.getLevel(), messageToChange, event.getThreadName(), event.getThrowableInformation(), event.getNDC(),
					event.getLocationInformation(), event.getProperties());
				synchronized (appenders) {
					for (Appender appender : appenders) {
						appender.doAppend(changedEvent);
					}
				}
			}
		} else {
			synchronized (appenders) {
				for (Appender appender : appenders) {
					appender.doAppend(event);
				}
			}
		}	
	}
}
