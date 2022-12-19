package com.almostreliable.almostarch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlmostArchMod {
	public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);
	public static void init() {
		LOG.info("Hello mod! :-)");
	}
}
