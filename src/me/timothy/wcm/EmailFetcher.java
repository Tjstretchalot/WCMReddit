package me.timothy.wcm;

import me.timothy.jreddit.info.Message;

/**
 * The email fetcher logs into a GMail account and fetches unread messages.
 * 
 * Uses IMAP for reading emails. Does not support sending emails
 * 
 * @author Timothy
 */
public class EmailFetcher {
	private String email;
	private String password;
	
	/**
	 * Creates an email fetcher tied to a particular account
	 * @param email the email
	 * @param password the password
	 */
	public EmailFetcher(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	/**
	 * Fetches any unread messages from the email, and logs off
	 * @return all unread messages. (SEEN = false)
	 */
	public Message[] fetchUnreadMessages() {
		return null;
	}
}
