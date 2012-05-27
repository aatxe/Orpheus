package client.command.external;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import tools.MapleLogger;

public class CommandLoader {
	private static CommandLoader instance;
	private static boolean initialized = false;
	protected AbstractCommandProcessor commandProcessor;
    protected final ArrayList<Class<? extends Commands>> commands = new ArrayList<Class<? extends Commands>>();
    
    public boolean loadCommands(String file) throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommands(new File(file));
    }
    
    public boolean loadCommands(String file, boolean ignoreIncorrectFiles)  throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommands(new File(file), ignoreIncorrectFiles);
    }
    
    public boolean loadCommands(File file) throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommands(file, true);
    }
    
    public boolean loadCommands(File file, boolean ignoreIncorrectFiles) throws FileNotFoundException, CommandLoaderException {
    	return this.load(file, ignoreIncorrectFiles, 1);
    }
    
    public boolean loadCommandProcessor(String file) throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommandProcessor(new File(file));
    }
    
    public boolean loadCommandProcessor(String file, boolean ignoreIncorrectFiles) throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommandProcessor(new File(file), ignoreIncorrectFiles);
    }
    
    public boolean loadCommandProcessor(File file) throws FileNotFoundException, CommandLoaderException {
    	return this.loadCommandProcessor(file, true);
    }
    
    public boolean loadCommandProcessor(File file, boolean ignoreIncorrectFiles) throws FileNotFoundException, CommandLoaderException {
    	return this.load(file, ignoreIncorrectFiles, 2);
    }
    
    public boolean load(String file) throws FileNotFoundException, CommandLoaderException {
    	return this.load(new File(file));
    }
    
    public boolean load(String file, boolean ignoreIncorrectFiles) throws FileNotFoundException, CommandLoaderException {
    	return this.load(new File(file), ignoreIncorrectFiles);
    }
    
    public boolean load(File file) throws FileNotFoundException, CommandLoaderException {
    	return this.load(file, true);
    }
    
    public boolean load(File file, boolean ignoreIncorrectFiles) throws FileNotFoundException, CommandLoaderException {
    	return this.load(file, true, 0);
    }
    
    protected boolean load(File file, boolean ignoreIncorrectFiles, int loadType) throws FileNotFoundException, CommandLoaderException {
    	if (!file.exists()) {
    		throw new FileNotFoundException("CommandLoader: Could not find file: " + file.getName());
    	} else if (file.isDirectory()) {
    		boolean ret = true;
    		for (File f : file.listFiles()) {
    			ret = ret & load(f, ignoreIncorrectFiles, loadType);
    		}
    	} else if (file.isFile()) {
    		if (file.getName().endsWith("jar")) {
				try {
					JarFile jf = new JarFile(file);
					if (loadType == 0) {
						initialized = (loadCommands(jf) && loadCommandProcessor(jf)) || initialized;
						return initialized;
					} else if (loadType == 1) {
						initialized = loadCommands(jf) || initialized;
						return initialized;
					} else if (loadType == 2) {
						initialized = loadCommandProcessor(jf) || initialized;
						return initialized;
					} else {
						throw new CommandLoaderException(4, "CommandLoader: Invalid loadType.");
					}
				} catch (NoSuchMethodException ex) {
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex);
					throw new CommandLoaderException(2, "CommandLoader: Constructor missing from AbstractCommandProcessor.");
				} catch (CommandLoaderException ex) { // because otherwise the part after would be huge...
					throw new CommandLoaderException(ex.getIdentifier(), ex.getMessage()); // throw 'er up
				} catch (Exception ex) {
					MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex);
					throw new CommandLoaderException(-1, "CommandLoader: Unknown error occurred while loading: " + file.getName());
				}
    		} else if (!ignoreIncorrectFiles) {
    			throw new CommandLoaderException(1, "CommandLoader: File is not a jar: " + file.getName());
    		}
    	}
		return false;
    }

    protected boolean loadCommands(JarFile jarFile) {
		Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			try {
				JarEntry je = e.nextElement();
				if (je.getName().endsWith(".class") && !je.isDirectory() && !je.getName().contains("$")) {
					Class<?> jarClass = Class.forName(je.getName().substring(0, je.getName().lastIndexOf(".class")));
					Class<? extends Commands> cmdClass = jarClass.asSubclass(Commands.class);
					commands.add(cmdClass);
				}
			} catch (ClassNotFoundException ex) {
				MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex); // something stupid happened.
			} catch (ClassCastException ex) {
				continue; // don't worry about it.
			}
		}
		return true;
    }
    
    protected boolean loadCommandProcessor(JarFile jarFile) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, CommandLoaderException {
    	Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			try {
				JarEntry je = e.nextElement();
				if (je.getName().endsWith(".class") && !je.isDirectory() && !je.getName().contains("$")) {
					Class<?> jarClass = Class.forName(je.getName().substring(0, je.getName().lastIndexOf(".class")));
					Class<? extends AbstractCommandProcessor> cpClass = jarClass.asSubclass(AbstractCommandProcessor.class);
					Constructor<? extends AbstractCommandProcessor> cpConstructor = cpClass.getConstructor();
					commandProcessor = cpConstructor.newInstance();
				}
			} catch (ClassNotFoundException ex) {
				MapleLogger.print(MapleLogger.EXCEPTION_CAUGHT, ex); // something stupid happened.
			} catch (ClassCastException ex) {
				continue; // don't worry about it.
			}
		}
		if (commandProcessor == null) throw new CommandLoaderException(3, "CommandLoader: CommandProcessor not found.");
		return (commandProcessor != null);
    }
    
    public void clear() {
    	commandProcessor = null;
    	commands.clear();
    	System.gc();
    }
    
    public AbstractCommandProcessor getCommandProcessor() {
    	return commandProcessor;
    }
    
    public ArrayList<Class<? extends Commands>> getCommands() {
    	return commands;
    }
    
    public static CommandLoader getInstance() {
    	if (instance == null) instance = new CommandLoader();
    	return instance;
    }

    public static boolean isInitialized() {
    	return initialized;
    }
    
    public static void clean() {
    	instance.clear();
    	instance = null;
    	initialized = false;
    	System.gc();
    }
}
