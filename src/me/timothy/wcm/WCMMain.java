package me.timothy.wcm;

import java.io.File;
import java.io.IOException;

import me.timothy.jreddit.requests.Utils;
import me.timothy.wcm.WCMConfig.SiteConfig;

import org.json.simple.parser.ParseException;

/**
 * Entry class to the program. Loads configuration and runs the WCMRedditBot.
 * 
 * @author Timothy
 */
public class WCMMain {
//	private static Logger logger = LogManager.getLogger();
	public static void main(String[] args) throws IOException, ParseException {
		WCMConfig config = new WCMConfig(new File("config"));
		config.reload();
		
		SiteConfig siteConfig = config.getSiteConfig();
		Utils.BASE = siteConfig.baseUrl;
		Utils.SITE_WIDE_USERNAME = siteConfig.username;
		Utils.SITE_WIDE_PASSWORD = siteConfig.password;
		
		WCMRedditBot rBot = new WCMRedditBot(config);
		
		while(true) {
			rBot.run();
			rBot.sleepFor(300000);
		}
	}
}
