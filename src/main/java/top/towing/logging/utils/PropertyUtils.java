package top.towing.logging.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyUtils {
	private static Properties props = new Properties();
	
	static {
		InputStream input = null;
		try {
			input = new FileInputStream("src/main/resources/application.properties");
			props.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static String getProperty(String propertyName, String defaultValue) {
		return props.getProperty(propertyName, defaultValue);
	}

}
