package me.timothy.wcm;

import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

/**
 * Converts html to markup by using Jython
 * to interact with the python html2text library
 * @author Timothy
 *
 */
public class HTMLToMarkup {
	private static final PythonInterpreter interpreter = new PythonInterpreter();
	
	static {
		interpreter.exec("import html2text");
	}
	
	/**
	 * Converts the specified html to markup
	 * @param html the html
	 * @return the markup
	 */
	public static String convertToMarkup(String html) {
		PyString htmlPy = new PyString(html);
		PyObject module = interpreter.get("html2text");
		PyObject html2text = module.__getattr__("html2text");
		PyObject result = html2text.__call__(htmlPy);
		
		return result.asString();
	}
}
