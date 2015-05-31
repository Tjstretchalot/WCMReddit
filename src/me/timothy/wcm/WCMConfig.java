package me.timothy.wcm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
		public String password;
		public String subreddit;
		
		/**
		 * Initialize an email config
		 * @param username username to use for the email (username@gmail.com)
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
	
	private static Logger logger = LogManager.getLogger();
	private static Gson gson = new Gson();
	
	private File directory;
	private EmailConfig[] cachedEmailConfigs;
	
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
	}
	
	public EmailConfig[] getEmailConfigs() {
		return cachedEmailConfigs;
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
	
	
}
