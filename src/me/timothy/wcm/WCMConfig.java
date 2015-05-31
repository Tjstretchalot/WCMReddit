package me.timothy.wcm;

import java.io.File;
import java.util.List;

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
		public String username;
		public String subreddit;
		public String password;
	}
	
	private File directory;
	private List<EmailConfig> cachedEmailConfig;
	
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
		
	}
}
