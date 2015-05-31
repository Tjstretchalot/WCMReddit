package me.timothy.wcm;

import javax.mail.Message;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.wcm.WCMConfig.EmailConfig;

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
		Message[] messages = eFetcher.fetchUnreadMessages();
		
		for(Message message : messages) {
			handleMessage(eConfig, message);
		}
	}
	
	/**
	 * Handles a particular message
	 * @param eConfig the configuration for this email
	 * @param message the message
	 */
	private void handleMessage(EmailConfig eConfig, Message message) {
		
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
