package tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PrintError {
    public static final String ACCOUNT_STUCK = "accountStuck.rtf",
                               EXCEPTION_CAUGHT = "exceptionCaught.rtf";//more to come (maps)
    
    //private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static void print(final String file, final Throwable t) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
	    out.write(getString(t).getBytes());
	    out.write("\n---------------------------------\n".getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}
    }
 
    public static void print(final String file, final String s) {
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(file, true);
	    out.write(s.getBytes());
	    out.write("\n---------------------------------\n".getBytes());
	} catch (IOException ess) {
	} finally {
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (IOException ignore) {
	    }
	}
    }
    
    private static String getString(final Throwable e) {
	String retValue = null;
	StringWriter sw = null;
	PrintWriter pw = null;
	try {
	    sw = new StringWriter();
	    pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    retValue = sw.toString();
	} finally {
	    try {
		if (pw != null) {
		    pw.close();
		}
		if (sw != null) {
		    sw.close();
		}
	    } catch (IOException ignore) {
	    }
	}
	return retValue;
    }    
}
