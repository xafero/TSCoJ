package com.xafero.tscoj.api;

import java.io.Closeable;

public interface ITranspiler extends Closeable {

	String compile(String code);

}