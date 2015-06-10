package me.timothy.wcm;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import me.timothy.bots.Retryable;

/**
 * The email fetcher logs into a GMail account and fetches unread messages.
 * 
 * Uses IMAP for reading emails. Does not support sending emails
 * 
 * @author Timothy
 */
public class EmailFetcher {
//	private static Logger logger = LogManager.getLogger();
	private String email;
	private String password;
	
	private Folder inbox;
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
	 * Fetches any unread messages from the email, marks them ALL as read, and logs off
	 * @return all unread messages. (SEEN = false)
	 */
	public Message[] fetchUnreadMessages() {
		Session session = Session.getDefaultInstance(new Properties());
		return new Retryable<Message[]>("Login via IMAP to googlemail") {

			@Override
			protected Message[] runImpl() throws Exception {

				Store store = session.getStore("imaps");
				store.connect("imap.googlemail.com", 993, email, password);
				
				inbox = store.getFolder("INBOX");
				inbox.open(Folder.READ_WRITE);
				
				Message[] results = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
				inbox.setFlags(results, new Flags(Flags.Flag.SEEN), true);
				
				return results;
			}
		}.run();
	}
	
	public void closeInbox() {
		try {
			inbox.close(false);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
