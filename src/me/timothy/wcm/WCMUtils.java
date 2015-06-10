package me.timothy.wcm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
		logger.debug(text);
		System.exit(0);
		text = text.replaceAll("[^\\x00-\\x7F]", ""); // no ascii
		text = stripFooter(text);
		text = cleanupFormatting(text);
		text = fixNewlines(text);
		
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
		Scanner scanner = new Scanner(text);
		StringBuilder result = new StringBuilder();

		boolean first = true;
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
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
		scanner.close();
		return result.toString();
	}
	
	/**
	 * Finds single newlines followed by text and switches them to 2 newlines
	 * in order to ensure reddit displays them as 2 newlines.
	 * 
	 * @param text the text to fix newlines on
	 * @return the fixed newlines
	 */
	private static String fixNewlines(String text) {
		StringBuilder result = new StringBuilder();
		
		boolean first = true;
		boolean lastWasEmpty = false;
		Scanner scanner = new Scanner(text);
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			if(first)
				first = false;
			else
				result.append("\n");
			
			boolean empty = line.trim().isEmpty();
			if(lastWasEmpty || empty)
				result.append(line);
			else
				result.append("\n").append(line);
			
			lastWasEmpty = empty;
		}
		scanner.close();
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
                    text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                	String s = getText(bp);
                    if (s != null)
                    	return htmlToMarkdown(fixUglyHTML(s));
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
		return "<html>" + str + "</html>";
	}
	
	/**
	 * Converts html to markdown
	 * @param theHTML the html
	 * @return the markdown
	 */
	private static String htmlToMarkdown(String theHTML) {
		System.err.print(theHTML);
		File xsltFile = new File("markdown.xsl");

		Source xmlSource = new StreamSource(new StringReader(theHTML));
		Source xsltSource = new StreamSource(xsltFile);

		TransformerFactory transFact =
				TransformerFactory.newInstance();
		Transformer trans;
		try {
			trans = transFact.newTransformer(xsltSource);

			StringWriter result = new StringWriter();
			trans.transform(xmlSource, new StreamResult(result));
			return result.toString();
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}
}
