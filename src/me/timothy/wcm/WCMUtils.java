package me.timothy.wcm;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of utility functions
 * @author Timothy
 *
 */
public class WCMUtils {
	private static final Logger logger = LogManager.getLogger();
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
		String text = getText(message);
		text = cleanupFormatting(text);
		text = stripFooter(text);
//		
		return text;
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
			if(line.contains("**WCM Investment Management** | 281 Brooks Street"))
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
	 * Fixes some oddities with reddits version of markup
	 * 
	 * @param text the text
	 * @return cleaned up formatting
	 */
	private static String cleanupFormatting(String text) {
		text = fixExtraneousSpacesInBody(text, "_");
		text = fixExtraneousSpacesInBody(text, "**");
		return text;
	}
	
	/**
	 * Fixes situations where you have marker, followed by a space, followed
	 * by text, followed by space, followed by marker, by stripping those
	 * extra spaces. May only do one space.
	 * 
	 * Example, if marker is _: blah _  blah blah blah_ blah blah _   blah blah   _ _blah _
	 * becomes blah _blah blah blah_ blah blah _blah blah_ _blah_.
	 * 
	 * It is very difficult to do this using regular expressions, because it often confuses which
	 * are groups, and which are in between groups.
	 * 
	 * @param body the body
	 * @param marker the marker
	 * @return fixed
	 */
	private static String fixExtraneousSpacesInBody(String body, String marker) {
		if(marker.length() == 0)
			throw new IllegalArgumentException("Empty marker");
		StringBuilder result = new StringBuilder();
		
		// Index in marker is the index to check in the next iteration, if it 
		// doesn't match, reset to 0, otherwise increment until index in marker
		// is greater than marker length.
		int indexInMarker = 0;

		// Are we inside a block?
		boolean open = false;
		
		// The current block message
		StringBuilder block = null;
		/*
		 * Goal:
		 * 
		 * Go through the body iteratively until we reach a marker. Then, parse until
		 * the end of the marker, trim this segment (that was within this marker), add it,
		 * add the marker, continue.
		 */
		for(int indexInBody = 0; indexInBody < body.length(); indexInBody++) {
			char bodyChar = body.charAt(indexInBody);
			char markChar = marker.charAt(indexInMarker);

			if(!open)
				result.append(bodyChar);
			else
				block.append(bodyChar);
			
			if(bodyChar == markChar) {
				indexInMarker++;
				
				if(indexInMarker >= marker.length()) {
					// We found a marker!
					if(!open) {
						open = true;
						block = new StringBuilder();
					}else {
						String blockMsg = block.substring(0, block.length() - marker.length()).toString().trim();
						result.append(blockMsg).append(marker);
						open = false;
						block = null;
					}
					indexInMarker = 0;
				}
			}
		}
		if(open) {
			logger.error("Still open at end of body " + body + " for marker " + marker);
		}
		return result.toString();
		
	}
	
	public static void main(String[] args) {
		String[] tests = {
			"test 1, _normal_ situation",
			"test 2, _ two spaces _ and stuff",
			"test 3, _   large after_ space",
			"test 4, _large before   _ space",
			"test 5, ** all the** _ things_ that **  could be ** _ the case _",
			"test 6, _ all the_ ** things** that _  could be _ ** the case **"
		};
		
		for(String test : tests) {
			System.out.println("--------BEGIN TEST--------");
			System.out.println(test);
			test = cleanupFormatting(test);
			System.out.println(test);
		}
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
                    text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                	String s = getText(bp);
                    if (s != null)
                    	return HTMLToMarkup.convertToMarkup(fixUglyHTML(s));
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

    //http://blog.codinghorror.com/cleaning-words-nasty-html/
    
    private static final String[] REGEX_REMOVE_HTML = {
    	"<!--(\\w|\\W)+?-->",
    	"<title>(\\w|\\W)+?</title>",
    	"s?class=\\w+",
    	"s+style='[^']+'",
    	"<(meta|link|/?o:|/?style|/?div|/?std|/?head|/?html|body|/?body|/?span|!\\[)[^>]*?>",
    	"(<[^>]+>)+&nbsp;(</\\w+>)+",
    	"\\s+\\v:\\w+=\"\"[^\"\"]+\"\"",
    	"(\\n\\r){2,}"
    };
    private static final String[][] REGEX_REPLACE_HTML = {
    	{ "&nbsp;", " " },
    	{ "p class=\"\\w+\"", "p" }
    };
    /**
     * Clean up the nasty html that word spits out.
     * @param str the nasty html
     * @return the useful html
     */
	private static String fixUglyHTML(String str) {
		for(String regex : REGEX_REMOVE_HTML) {
			str = str.replaceAll(regex, "");
		}
		for(String[] regexAndRepl : REGEX_REPLACE_HTML) {
			str = str.replaceAll(regexAndRepl[0], regexAndRepl[1]);
		}
		return str;
	}
}
