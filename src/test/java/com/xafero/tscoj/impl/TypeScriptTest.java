package com.xafero.tscoj.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.xafero.tscoj.api.ITranspiler;

public class TypeScriptTest {

	@Test
	public void testCompiler() throws IOException {
		ITranspiler tsc = new NashornCompiler<>();
		String ts = tsc.compile("class Test { name: string }").split("//")[0];
		assertEquals("var Test = (function () {    function Test() {    }    return Test;}());", strip(ts));
		tsc.close();
	}

	private static String strip(String text) {
		return text.replace('\n' + "", "").replace('\r' + "", "").replace('\t' + "", "").trim();
	}
}