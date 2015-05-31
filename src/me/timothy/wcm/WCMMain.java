package me.timothy.wcm;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry class to the program. Loads configuration and runs the WCMRedditBot.
 * 
 * @author Timothy
 */
public class WCMMain {
	private static Logger logger = LogManager.getLogger();
	public static void main(String[] args) {
		WCMConfig config = new WCMConfig(new File("config"));
		config.reload();
		
		WCMRedditBot rBot = new WCMRedditBot(config);
		
		rBot.run();
	}
}
