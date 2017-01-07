package com.xafero.tscoj.api;

import java.io.Closeable;
import java.util.Map;

public interface ISystem extends Closeable {

	String getCurrentDirectory();

	boolean fileExists(String path);

	boolean directoryExists(String path);

	String getExecutingFilePath();

	void write(Object obj);

	String readFile(String path, String encoding);

	void writeFile(String path, String data, boolean byteOrderMark);

	String[] getDirectories(String path);

	void push(String path, String code);

	void exit(int code);

	Map<String, String> dump();
}