package com.atguigu.commons;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigManager {

    private ConfigManager instance = null;

    private ConfigManager(){

        String configfile = "bigdata.properties";

        if(null != System.getenv(Constants.BIGDATA_HOME)){

        }


        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setFileName(configfile));
        try
        {
            Configuration config = builder.getConfiguration();
        }
        catch(ConfigurationException cex)
        {
            // loading of the configuration file failed
        }

    }



}
