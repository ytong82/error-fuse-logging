package top.towing.logging.helper.impl;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.util.concurrent.ConcurrentHashMap;
import top.towing.logging.helper.ErrorFuseHelper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.spi.LoggingEvent;

public class ErrorFuseHelperImpl implements ErrorFuseHelper {
	// fusing time, use seconds
	private int fusingTime;
	// fuse threshold for error numbers
	private int fuseThreshold;
	// sampling ratio when fuse on
	private int samplingRatio;
	
	private static CacheManager cm;
	private static Cache lefbc; 
	
	static {
		cm = CacheManager.getInstance();
	}
	
	public ErrorFuseHelperImpl(int fusingTime, int fuseThreshold, int samplingRatio) {
		// setup fuse settings
		this.fusingTime = fusingTime;
		this.fuseThreshold = fuseThreshold;
		this.samplingRatio = samplingRatio;
		
		// setup cache
		int fuseCacheTTL = this.fusingTime * 10;
		if (cm.getCache("logging-events-fuse-buckets") == null) {
			lefbc = new Cache("logging-events-fuse-buckets", 0, false, false, fuseCacheTTL, fuseCacheTTL);
			cm.addCache(lefbc);
		}	
	}
	
	
	// use the combination of file name, class name and line number to define to key for every fuse buckets
	// setup every fuse bucket in fuse buckets for every time period defined by fuse.fusing.time
	// calculate if we request to print logger more than fuse.fuse.threshold, if so, fuse it
	public boolean checkIfFused(LoggingEvent loggingEvent) {
		String toStringMd5Hex = getLoggingEventMd5Hex(loggingEvent);
		if (lefbc.get(toStringMd5Hex) == null) {
			ConcurrentHashMap<String, AtomicCounter> chm = new ConcurrentHashMap<String, AtomicCounter>();
			lefbc.put(new Element(toStringMd5Hex, chm));
		}
		
		Element lee = lefbc.get(toStringMd5Hex);
		@SuppressWarnings("unchecked")
		ConcurrentHashMap<String, AtomicCounter> chm = (ConcurrentHashMap<String, AtomicCounter>)lee.getObjectValue();
		String currentFuseBucketTimeStamp = getCurrentFuseBucketTimeStamp();
		if (chm.get(currentFuseBucketTimeStamp) == null) {
			chm.put(currentFuseBucketTimeStamp, setupCounter(chm));
		}
		
		AtomicCounter counter = chm.get(currentFuseBucketTimeStamp);
		if (counter.getFused() == true) {
			return true;
		} else {
			if (counter.incr() > this.fuseThreshold) {
				counter.setFused(true);
				return true;
			} else {
				return false;
			}
		}
	}
	
	public int getSamplingRatio() {
		return samplingRatio;
	}

	private AtomicCounter setupCounter(ConcurrentHashMap<String, AtomicCounter> chm) {
		String lastFuseBucketTimeStamp = getLastFuseBucketTimeStamp();
		AtomicCounter counter = chm.get(lastFuseBucketTimeStamp);
		// check if the last fuse bucket is fused
		// if fused, check if it is fused due to copy its previous one status
		// if it just copies previous status, then judge if it is truly fused. 
		// if not truly fuse, then go to set it as not fused.
		if (counter != null) {
			if (counter.getFused() == true) {
				if (counter.getCount() < this.fuseThreshold) {
					counter.setFused(false);
				}
			}
			return new AtomicCounter(counter.getFused());
		} else {
			return new AtomicCounter();
		}
	}
	
	private String getLoggingEventMd5Hex(LoggingEvent loggingEvent) {
		/*String toString = loggingEvent.getLocationInformation().getFileName() + ' '
				+ loggingEvent.getLocationInformation().getClassName() + ' '
				+ loggingEvent.getLocationInformation().getMethodName() + ' '
				+ loggingEvent.getLocationInformation().getLineNumber() + ' '
				+ loggingEvent.getMessage().toString();*/
		
		// we may just use the combination of file name, class name and line number
		// to calculate as the identifier of log event to calculate the md5sum 
		String toString = loggingEvent.getLocationInformation().getFileName() + ' '
				+ loggingEvent.getLocationInformation().getClassName() + ' '
				+ loggingEvent.getLocationInformation().getLineNumber();
		String toStringMd5Hex = DigestUtils.md5Hex(toString).toUpperCase();
		return toStringMd5Hex;
	}
	
	private String getCurrentFuseBucketTimeStamp() {
		long timeStampNow = System.currentTimeMillis();
		long remains = timeStampNow % (this.fusingTime * 1000);
		long bucketTimeStamp = timeStampNow - remains;
		return Long.toString(bucketTimeStamp);
	}
	
	private String getLastFuseBucketTimeStamp() {
		long timeStampNow = System.currentTimeMillis();
		long remains = timeStampNow % (this.fusingTime * 1000);
		long bucketTimeStamp = timeStampNow - this.fusingTime * 1000 - remains;
		return Long.toString(bucketTimeStamp);
	}
	
	public class AtomicCounter {
		private int count;
		private boolean fused;
		
		public AtomicCounter() {
			this.count = 0;
		}
		
		public AtomicCounter(boolean fused) {
			this.count = 0;
			this.fused = fused;
			
		}
		
		public long incr() {
			synchronized (this) {
				this.count++;
				return this.count;
			}
		}
		
		public int getCount() {
			return this.count;
		}
		
		public boolean getFused() {
			return this.fused;
		}
		
		public void setFused(boolean fused) {
			this.fused = fused;
		}
	}
}
