package me.timothy.wcm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

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
	
	/**
	 * Describes an email that has been cached (so that the folder
	 * can be closed)
	 * 
	 * @author Timothy
	 */
	public static class CachedEmail {
		/**
		 * Who sent the email
		 */
		public Address[] from;
		
		/**
		 * Content (stringified) of the email
		 */
		public String content;

		/**
		 * Initialize the cached email
		 * @param from the addresses of who sent it
		 * @param content the content
		 */
		public CachedEmail(Address[] from, String content) {
			super();
			this.from = from;
			this.content = content;
		}

		@Override
		public String toString() {
			return "CachedEmail [from=" + Arrays.deepToString(from) + ", content=" + content + "]";
		}
	}
	
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
	public List<CachedEmail> fetchUnreadMessages() {
		Session session = Session.getDefaultInstance(new Properties());
		try {
			Store store = session.getStore("imaps");
			store.connect("imap.googlemail.com", 993, email, password);
			
			Folder inbox = store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);
			
			Message[] results = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			inbox.setFlags(results, new Flags(Flags.Flag.SEEN), true);
			
			List<CachedEmail> cached = new ArrayList<CachedEmail>();
			for(Message message : results) {
				cached.add(new CachedEmail(message.getFrom(), message.getContent().toString()));
			}
			inbox.close(false);
			return cached;
		} catch (MessagingException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
