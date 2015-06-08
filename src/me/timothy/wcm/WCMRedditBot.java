package me.timothy.wcm;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import me.timothy.bots.Retryable;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.User;
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
		String email = eConfig.username + "@gmail.com";
		EmailFetcher eFetcher = new EmailFetcher(email, eConfig.password);
		logger.printf(Level.DEBUG, "Scanning %s for /r/%s", email, eConfig.subreddit);
		Message[] messages = eFetcher.fetchUnreadMessages();
		
		for(Message message : messages) {
			try {
				handleMessage(eConfig, message);
			} catch (MessagingException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		eFetcher.closeInbox();
	}
	
	/**
	 * Handles a particular message
	 * @param eConfig the configuration for this email
	 * @param message the message
	 * @throws MessagingException 
	 * @throws IOException 
	 */
	private void handleMessage(EmailConfig eConfig, Message message) throws MessagingException, IOException {
		String from = WCMUtils.getEmailFromAddress(message.getFrom()[0]);
		logger.debug(eConfig + " recieved message from " + from);
		
		UserConfig[] userConfigs = config.getUserConfigs();
		
		for(UserConfig uConfig : userConfigs) {
			if(uConfig.email.equals(from)) {
				handleMessage(eConfig, uConfig, message);
				return;
			}
		}
		
		logger.warn("Unknown sender: " + from);
	}

	private void handleMessage(EmailConfig eConfig, UserConfig uConfig,
			Message message) throws MessagingException, IOException {
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
		
		final String messageToSend = WCMUtils.getMainMessage(message);
		logger.debug(messageToSend);
		try {
			String subject = message.getSubject();
			if(subject.startsWith("FW:"))
				subject = subject.substring(3);
			subject = subject.trim();
			String messageTrunc = messageToSend.length() <= 10 ? messageToSend : messageToSend.substring(0, 10) + "...";
			logger.debug("Submitting " + messageTrunc + " as " + redditUser.getUsername() + " to " + eConfig.subreddit + " with subject " + subject);
			final String subjectFinal = subject;
			new Retryable<Boolean>("Submit post") {

				@Override
				protected Boolean runImpl() throws Exception {
					RedditUtils.submitSelf(redditUser, eConfig.subreddit, subjectFinal, messageToSend);
					return true;
				}
			}.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sleeps for the specified period of time, wrapping interrupted
	 * exceptions in runtime exceptions 
	 * @param ms time in milliseconds
	 */
	void sleepFor(long ms) {
		try {
			logger.printf(Level.TRACE, "Sleeping for %d ms", ms);
			Thread.sleep(ms);
		}catch(InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
