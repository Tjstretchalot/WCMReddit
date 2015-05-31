package me.timothy.wcm;

import java.util.List;

import me.timothy.bots.Retryable;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.User;
import me.timothy.wcm.EmailFetcher.CachedEmail;
import me.timothy.wcm.WCMConfig.EmailConfig;
import me.timothy.wcm.WCMConfig.UserConfig;

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
		
		for(CachedEmail message : messages) {
			handleMessage(eConfig, message);
		}
	}
	
	/**
	 * Handles a particular message
	 * @param eConfig the configuration for this email
	 * @param message the message
	 */
	private void handleMessage(EmailConfig eConfig, CachedEmail message) {
		String from = WCMUtils.getEmailFromAddress(message.from[0]);
		logger.debug(eConfig + " recieved message from " + from + ": ");
		logger.debug(message.content);
		
		UserConfig[] userConfigs = config.getUserConfigs();
		
		for(UserConfig uConfig : userConfigs) {
			logger.debug("comparing " + uConfig.email);
			if(uConfig.email.equals(from)) {
				handleMessage(eConfig, uConfig, message);
				return;
			}
		}
		
		logger.warn("Unknown sender: " + message.from[0].toString());
	}

	private void handleMessage(EmailConfig eConfig, UserConfig uConfig,
			CachedEmail message) {
		final User redditUser = new User(uConfig.redditUsername, uConfig.redditPassword);
		logger.debug("Attempting to login as " + uConfig.redditUsername + " with password " + uConfig.redditPassword);
		Boolean login = new Retryable<Boolean>("Login " + uConfig.redditUsername) {

			@Override
			protected Boolean runImpl() throws Exception {
				RedditUtils.loginUser(redditUser);
				return true;
			}
			
		}.run();
		
		if(!login) {
			logger.error("Failed to login as " + uConfig.redditUsername);
			System.exit(1);
		}
		
		logger.debug("Logged in as " + redditUser.getUsername());
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
