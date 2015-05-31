package me.timothy.wcm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Configuration for the bot - important things like account information, emails, users, etc.
 * 
 * @author Timothy
 */
public class WCMConfig {
	/**
	 * Information regarding an account
	 * 
	 * @author Timothy
	 */
	public class EmailConfig {
		public String username; // for the email
		public String password;
		public String subreddit;
		
		/**
		 * Initialize an email config
		 * @param username username to use for the email (postthistocompany@gmail.com)
		 * @param password the password to use
		 * @param subreddit the subreddit
		 */
		public EmailConfig(String username, String password, String subreddit) {
			this.username = username;
			this.password = password;
			this.subreddit = subreddit;
		}

		@Override
		public String toString() {
			return "EmailConfig [username=" + username + ", password="
					+ password + ", subreddit=" + subreddit + "]";
		}
	}
	
	/**
	 * Describes the configuration for a user
	 * 
	 * @author Timothy
	 */
	public class UserConfig {
		/**
		 * The email of the user. This is the email that when sends an email to one of 
		 * the subreddit emails, the bot knows to post from the particular reddit account.
		 * 
		 * E.g. johndoe@gmail.com
		 */
		public String email;
		public String redditUsername;
		public String redditPassword;
		
		/**
		 * Create a new user with the specified information
		 * @param emailUsername the email of the user
		 * @param redditUsername the reddit username of the user
		 * @param redditPassword the password of the user
		 */
		public UserConfig(String email, String redditUsername,
				String redditPassword) {
			this.email = email;
			this.redditUsername = redditUsername;
			this.redditPassword = redditPassword;
		}

		@Override
		public String toString() {
			return "UserConfig [email=" + email + ", redditUsername="
					+ redditUsername + ", redditPassword=" + redditPassword
					+ "]";
		}
	}
	
	private static Logger logger = LogManager.getLogger();
	private static Gson gson = new GsonBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	
	private File directory;
	private EmailConfig[] cachedEmailConfigs;
	private UserConfig[] cachedUserConfigs;
	
	/**
	 * Prepares the config to load in the specified directory
	 * @param directory the directory to load from
	 */
	public WCMConfig(File directory) {
		this.directory = directory;
	}
	
	/**
	 * Reloads the configuration from disk
	 */
	public void reload() {
		loadEmailConfig(new File(directory, "emailconfig.json"));
		loadUserConfig(new File(directory, "userconfig.json"));
	}
	
	public EmailConfig[] getEmailConfigs() {
		return cachedEmailConfigs;
	}
	
	public UserConfig[] getUserConfigs() {
		return cachedUserConfigs;
	}
	
	/**
	 * Loads the email configuration from the file, assuming a json format.
	 * 
	 * ---
	 * 
	 * Example file is
	 * [  
     *    {  
     *       "username":"john",
     *       "password":"doe",
     *       "subreddit":"john"
     *    },
     *    {
     *       ...same as above...
     *    }
     * ]    
	 * @param file the file to load from
	 */
	private void loadEmailConfig(File file) {
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			cachedEmailConfigs = gson.fromJson(br, EmailConfig[].class);
		}catch(FileNotFoundException ex) {
			logger.error("Missing file " + file.getAbsolutePath() + ", which should contain email configuration!");
			throw new RuntimeException(ex);
		}catch(IOException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Loads the user configuration from the file, assuming a json format
	 * 
	 * ---
	 * 
	 * Example file is
	 * [
	 *   {
	 *     "email": "johndoe@gmail.com",
	 *     "reddit_username": "john",
	 *     "reddit_password": "hunter2"
	 *   }
	 * ]
	 * @param file
	 */
	private void loadUserConfig(File file) {
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			cachedUserConfigs = gson.fromJson(br, UserConfig[].class);
		}catch(FileNotFoundException ex) {
			logger.error("Missing file " + file.getAbsolutePath() + ", which should contain user configuration!");
			throw new RuntimeException(ex);
		}catch(IOException ex) {
			logger.throwing(ex);
			throw new RuntimeException(ex);
		}
	}
}
