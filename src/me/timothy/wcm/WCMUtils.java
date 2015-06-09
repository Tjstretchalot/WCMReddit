package me.timothy.wcm;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

/**
 * Collection of utility functions
 * @author Timothy
 *
 */
public class WCMUtils {
	
	/**
	 * Parses an email from an address of the form William Horner <william@wcminvest.com>
	 * @param addr the address
	 * @return the email
	 */
	public static String getEmailFromAddress(Address addr) {
		Matcher matcher = Pattern.compile("<(.+)>").matcher(addr.toString());
		if(!matcher.find())
			return addr.toString();
		return matcher.group(1);
	}

	/**
	 * Parses through an email and attempts to locate the core of the message, strip formatting,
	 * and post it.
	 * 
	 * @param message
	 * @return
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public static String getMainMessage(Message message) throws MessagingException, IOException {
		return cleanupFormatting(stripFooter(getText(message).replaceAll("[^\\x00-\\x7F]", ""))); // strip non-ascii and footer
	}
	
	
	/**
	 * Attempts to strip footers
	 * @param text
	 * @return
	 */
	private static String stripFooter(String text) {
		String[] lines = text.split("\n");
		
		StringBuilder result = new StringBuilder();
		
		boolean first = true;
		for(String line : lines) {
			if(line.contains("WCM Investment Management | 281 Brooks Street, Laguna Beach, CA 92651"))
				return result.toString();
			
			if(first)
				first = false;
			else
				result.append("\n");
			result.append(line);
		}
		
		return result.toString();
	}
	
	/**
	 * Removes extra spacing the beginning of lines.
	 * 
	 * @param text the text
	 * @return cleaned up formatting
	 */
	private static String cleanupFormatting(String text) {
		String[] lines = text.split("\n");
		
		StringBuilder result = new StringBuilder();

		boolean first = true;
		for(String line : lines) {
			int initialSpacing = 0;
			for(; initialSpacing < line.length(); initialSpacing++) {
				if(!Character.isWhitespace(line.charAt(initialSpacing))) {
					break;
				}
			}
			
			if(initialSpacing >= 4) {
				line = "  " + line.substring(initialSpacing);
			}

			if(first)
				first = false;
			else
				result.append("\n");
			result.append(line);
		}
		
		return result.toString();
	}
	
	// http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody
	// changed to prefer plain text over html text
    /**
     * Return the primary text content of the message.
     */
    public static String getText(Part p) throws
                MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    String s = getText(bp);
                    if(s != null)
                    	return s;
                } else if (bp.isMimeType("text/html")) {
                    if (text != null)
                        text = getText(bp);
                    continue;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

	
	
}
