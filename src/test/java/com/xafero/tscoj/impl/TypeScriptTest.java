package com.xafero.tscoj.impl;

import static org.junit.Assert.assertEquals;

import java.util.Map.Entry;

import org.junit.Test;

import com.xafero.natra.api.INativeParams;
import com.xafero.natra.api.INativeTask;

public class TypeScriptTest {

	@Test
	public void testCompiler() throws Exception {
		NashornCompiler<?> tsc = new NashornCompiler<>(null);
		INativeParams parms = new TestNativeParams("class Test { name: string }");
		INativeTask<?, ?> task = tsc.translate(parms);
		task.call();
		Entry<?, ?> pair;
		pair = (Entry<?, ?>) task.getResults().entrySet().iterator().next();
		Entry<?, ?> res = (Entry<?, ?>) pair.getValue();
		String ts = res.getValue().toString().split("//")[0];
		assertEquals("var Test = (function () {    function Test() {    }    return Test;}());", strip(ts));
		tsc.close();
	}

	private static String strip(String text) {
		return text.replace('\n' + "", "").replace('\r' + "", "").replace('\t' + "", "").trim();
	}
}