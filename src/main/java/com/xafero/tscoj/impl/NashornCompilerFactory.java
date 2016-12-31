package com.xafero.tscoj.impl;

import com.xafero.natra.api.INativeTranslator;
import com.xafero.natra.common.AbstractNativeFactory;

public class NashornCompilerFactory extends AbstractNativeFactory {

	public NashornCompilerFactory() {
		extensions = new String[] { "ts" };
		mimeTypes = new String[] { "text/javascript" };
	}

	@Override
	public INativeTranslator getTranslator() {
		return new NashornCompiler<>(this);
	}
}