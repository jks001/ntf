package com.blockchain.test.utils;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ConfigUtil {
	
	private static Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);
	
	static Configuration config;
	
	private ConfigUtil(){}
	
	 public static synchronized Configuration getInstance() {
	        try {
				if (config == null) {
					config = new Configurations().properties(new File("bitcoinj.properties"));
				}
				return config;
			} catch (ConfigurationException e) {
				LOG.info("=== com.bscoin.coldwallet.cointype.common.ConfigUtil.getInstance():{} ===",e.getMessage(),e);
			}
			return null;
	    }
}
