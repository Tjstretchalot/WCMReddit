package me.timothy.wcm;

import java.util.Arrays;
import java.util.List;

import me.timothy.wcm.EmailFetcher.CachedEmail;
import me.timothy.wcm.WCMConfig.EmailConfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The brain of the bot - does the actual high-level processing.
 * Does one iteration per call to run()
 * 
 * @author Timothy
 */
public class WCMRedditBot implements Runnable {
	private static Logger logger = LogManager.getLogger();
	
	private WCMConfig config;
	
	/**
	 * Initializes the reddit bot with the specified configuration 
	 * options
	 * @param config the config
	 */
	public WCMRedditBot(WCMConfig config) {
		this.config = config;
	}
	
	/**
	 * Performs one iteration of testing
	 */
	public void run() {
		EmailConfig[] eConfigs = config.getEmailConfigs();
		
		for(EmailConfig eConfig : eConfigs) {
			scanEmail(eConfig);
			sleepFor(2000);
		}
	}
	
	/**
	 * Scans the email for any unread messages
	 * @param eConfig the emails config options
	 */
	private void scanEmail(EmailConfig eConfig) {
		EmailFetcher eFetcher = new EmailFetcher(eConfig.username + "@gmail.com", eConfig.password);
		List<CachedEmail> messages = eFetcher.fetchUnreadMessages();
		
		System.out.println(eConfig + " has " + messages.size() + " unseen messages");
		for(CachedEmail message : messages) {
			handleMessage(eConfig, message);
		}
		System.out.println("Done scanning email");
	}
	
	/**
	 * Handles a particular message
	 * @param eConfig the configuration for this email
	 * @param message the message
	 */
	private void handleMessage(EmailConfig eConfig, CachedEmail message) {
		System.out.println(eConfig + " recieved message from " + Arrays.deepToString(message.from) + ": ");
		System.out.println(message.content);
	}

	private void sleepFor(long ms) {
		try {
			logger.printf(Level.TRACE, "Sleeping for %d ms", ms);
			Thread.sleep(ms);
		}catch(InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
