package net.server;

import java.io.Console;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Aaron Weiss
 */
public class CreateINI {
	public static void main(String args[]) {
		StringBuilder sb = new StringBuilder();
		String nl = "\n";
		byte worlds;
		Console con = System.console();

		sb.append("# OrpheusMS INI Configuration" + nl);
		sb.append("# Created on " + getDateStamp() + nl + nl);
		sb.append("# World Flags: 0 = None, 1 = Event, 2 = New, 3 = Hot" + nl);
		
		System.out.println("Welcome to OrpheusMS's Configuration Creator" + nl);
		System.out.println("World Flags: 0 = None, 1 = Event, 2 = New, 3 = Hot");
		worlds = Byte.parseByte(con.readLine("How many worlds? "));
		sb.append("worlds=").append(worlds).append(nl + nl);
		
		System.out.println(nl);
		System.out.println("Properties for server");
		String mysqlInConfig = con.readLine("Would you like to configure MySQL in the configuration file? ");
		while (!mysqlInConfig.equalsIgnoreCase("yes") && !mysqlInConfig.equalsIgnoreCase("no") && !mysqlInConfig.equalsIgnoreCase("y") && !mysqlInConfig.equalsIgnoreCase("n")) {
			System.out.println("You must enter either yes or no.");
			mysqlInConfig = con.readLine("Would you like to configure MySQL in the configuration file? ");
		}
		
		if (mysqlInConfig.equalsIgnoreCase("yes") || mysqlInConfig.equalsIgnoreCase("y")) {
			sb.append("# Properties for server").append(nl);
			sb.append("mysql_host").append("=").append(con.readLine("\tMySQL Host: ")).append(nl);
			sb.append("mysql_port").append("=").append(Integer.parseInt(con.readLine("\tMySQL Port: "))).append(nl);
			sb.append("mysql_user").append("=").append(con.readLine("\tMySQL Username: ")).append(nl);
			sb.append("mysql_pass").append("=").append(con.readLine("\tMySQL Password: ")).append(nl);
			System.out.println("Be sure to set DB_USE_COMPILED_VALUES to false in ServerConstants!");
		}
		
		System.out.println(nl);

		for (byte b = 0; b < worlds; b++) {
			sb.append("# Properties for world").append(b).append(nl);
			System.out.println("Properties for world " + b);
			
			if (b > 1) {
				System.out.println("Be sure to create an NPC folder for this world!");
			}
			sb.append("flag").append(b).append("=").append(Byte.parseByte(con.readLine("\tWorld Flag: "))).append(nl);
			sb.append("servermessage").append(b).append("=").append(con.readLine("\tServer Message: ")).append(nl);
			sb.append("eventmessage").append(b).append("=").append(con.readLine("\tEvent Message: ")).append(nl);
			sb.append("recommendmessage").append(b).append("=").append(con.readLine("\tRecommend Message: ")).append(nl);
			sb.append("channels").append(b).append("=").append(Byte.parseByte(con.readLine("\tNumber of Channels: "))).append(nl);
			sb.append("exprate").append(b).append("=").append(Byte.parseByte(con.readLine("\tExperience Rate: "))).append(nl);
			sb.append("mesorate").append(b).append("=").append(Byte.parseByte(con.readLine("\tMeso Rate: "))).append(nl);
			sb.append("droprate").append(b).append("=").append(Byte.parseByte(con.readLine("\tDrop Rate: "))).append(nl);
			sb.append("bossdroprate").append(b).append("=").append(Byte.parseByte(con.readLine("\tBoss Drop Rate: "))).append(nl);
			System.out.println(nl);
			sb.append(nl);
		}

		sb.append(nl).append("gmserver=").append(Boolean.parseBoolean(con.readLine("GM Server (true/false): ")));
		FileOutputStream out = null;
		try {
			out = new FileOutputStream("orpheus.ini", false);
			out.write(sb.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {}
		}

		System.out.println("Configuration Complete." + nl);
		System.out.println("Server Launch Creator");
		
		sb = new StringBuilder();
		try {
			String heapsize = con.readLine("Java Heap Size (in MB): ");
			while (heapsize.equals("?")) {
				System.out.println("");
				System.out.println("WikiAnswers: Java heap is the heap size allocated to JVM applications which takes care of the new objects being created. If the objects being created exceed the heap size, it will throw a MemoryOutofBounds exception." + nl);
				heapsize = con.readLine("Java Heap Size (in MB): ");
			}
			String keystore = con.readLine("Name of Keystore: ");
			String linux = con.readLine("Will your server be running Unix/Linux? ");
			while (!linux.equalsIgnoreCase("yes") && !linux.equalsIgnoreCase("no") && !linux.equalsIgnoreCase("y") && !linux.equalsIgnoreCase("n")) {
				System.out.println("You must enter either yes or no.");
				linux = con.readLine("Will your server be running Unix/Linux? ");
			}
			if (linux.equalsIgnoreCase("no") || linux.equalsIgnoreCase("n")) {
				out = new FileOutputStream("launch_server.bat", false);
				sb.append("@echo off").append("\r\n").append("@title OrpheusMS Server").append("\r\n");
				sb.append("set CLASSPATH=.;dist\\*\r\n");
				sb.append("java -Xmx").append(heapsize).append("m -Dwzpath=wz\\ -Djavax.net.ssl.keyStore=").append(keystore).append(" -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=").append(keystore).append(" -Djavax.net.ssl.trustStorePassword=passwd net.server.Server\r\n");
				sb.append("pause");
			} else if (linux.equalsIgnoreCase("yes") || linux.equalsIgnoreCase("y")) {
				out = new FileOutputStream("runsrv.sh", false);
				sb.append("#!/bin/sh").append(nl);
				sb.append("export CLASSPATH=").append(".:dist/*").append(nl);
				sb.append("java ").append("-Xmx").append(heapsize).append("M").append(" -Dwzpath=wz/ ").append("-Djavax.net.ssl.keyStore=").append(keystore).append(" -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=").append(keystore).append(" -Djavax.net.ssl.trustStorePassword=passwd");
				sb.append(" net.server.Server");
			}
			out.write(sb.toString().getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {}
		}
		System.out.println("");
		System.out.println("Make sure that ServerConstants is modified, cleaned, andcompiled before you start the server.");
		System.out.println("If you would like to modify these settings, rerun this script, or modify the generated configuration file.");
	}
	
	private static String getDateStamp() {
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		return sdf.format(now);
	}
}
