package client.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CommandLoader {
    protected final Map<String, Commands> commands = new HashMap<String, Commands>();

    public boolean loadCommands(File file) throws FileNotFoundException, InvalidCommandException {
    	return this.loadCommands(file, true);
    }
    
    public boolean loadCommands(File file, boolean ignoreIncorrectFiles) throws FileNotFoundException, InvalidCommandException {
    	if (!file.exists()) {
    		throw new FileNotFoundException("CommandLoader: Could not find file: " + file.getName());
    	} else if (file.isDirectory()) {
    		boolean ret = true;
    		for (File f : file.listFiles()) {
    			ret = ret & loadCommands(f, ignoreIncorrectFiles);
    		}
    	} else if (file.isFile()) {
    		if (file.getName().endsWith("jar")) {
				try {
					JarFile jf = new JarFile(file);
					return loadJarFile(jf);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new InvalidCommandException(-1, "CommandLoader: Unknown error occurred while loading: " + file.getName());
				}
    		} else if (!ignoreIncorrectFiles) {
    			throw new InvalidCommandException(1, "CommandLoader: File is not a jar: " + file.getName());
    		}
    	}
		return false;
    }
    
    protected boolean loadJarFile(JarFile jarFile) throws Exception {
		Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			Commands command = null;
			JarEntry je = e.nextElement();
			Class<?> jarClass;
			jarClass = Class.forName(je.getName());
            Class<? extends Commands> cmdClass = jarClass.asSubclass(Commands.class);
            Constructor<? extends Commands> constructor = cmdClass.getConstructor();
            command = constructor.newInstance();
            if (command != null) commands.put(command.getClass().getName(), command);
		}
		return true;
    }
}
