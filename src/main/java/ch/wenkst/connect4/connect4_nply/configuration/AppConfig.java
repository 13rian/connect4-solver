package ch.wenkst.connect4.connect4_nply.configuration;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ch.wenkst.sw_utils.Utils;
import ch.wenkst.sw_utils.logging.Log;

public class AppConfig {
	private static Log log = Log.getLogger(AppConfig.class);
	private Config config; 	
	private static AppConfig instance = null; 	// instance for the singleton access
	
	/**
	 * holds the global values of the application
	 */
	protected AppConfig() {
		parseConfigFile();
	}
	
	
	/**
	 * returns an instance of the application configuration which holds the configuration values
	 * @return
	 */
	public static AppConfig getInstance() {
		if(instance == null) {
			instance = new AppConfig();
		}	      
		return instance;
	}
	
	
	// general configuration values
	
	
	
	// file  paths
	public static final String sep = File.separator;
	public static final String dirLoggerConfig = Utils.getWorkDir() + sep + "config" + sep + "log_config.properties";
	public static final String dirPositions = Utils.getWorkDir() + sep + "positions" + sep;
	public static final String dirSolvedPos = Utils.getWorkDir() + sep + "solved_pos" + sep;
	public static final String dirTranspositionTable = Utils.getWorkDir() + sep + "transposition_table_csvs" + sep;
	public static final String dirTestSet = Utils.getWorkDir() + File.separator + "test_set" + File.separator;
	
	
	// board parameters
	public static final byte boardHeight = 6;									// height of the board
	public static final byte boardWidth = 7; 									// width of the board
	public static final byte boardSize = boardHeight*boardWidth; 				// total size of the board
	public static final byte scoreRef = ((boardHeight*boardWidth) + 1) / 2; 	// winner score: scoreRef - #winner disks
	
    public static final byte minScore = -boardSize/2 + 3;						// minimal score that can be reached
    public static final byte maxScore = (boardSize+1)/2 - 3;					// maximal score that can be reached
    
    
    // parameters for the solver
    public static final int tpTableSize = 8388593; 			// would give 64Mb if the table would only use 64 bits
	
    
    
	/**
	 * opens and parses the configuration file
	 */
	private void parseConfigFile() {
		try {
			config = ConfigFactory.load();
		
		} catch (Exception e) {
			log.severe("failed to parse the configuration file, stop the program: ", e); 
			System.exit(1);
		}
	}
	
	
	/**
	 * retrieves the configuration value with the passed key, if it is not present or cannot be 
	 * casted the passed default value will be returned
	 * @param key 			key of the configuration value
	 * @param defaultVal 	the default value that is returned if an error occurs
	 * @return 				the configuration value of the passed key or the default value;
	 */
	@SuppressWarnings("unchecked")
	public <T> T getConfigValue(String key, T defaultVal) {
		try {
			T result = (T) config.getAnyRef(key);
			return result;
			
		} catch (Exception e) {
			log.warning("error reading the configuration value " + key + ": " + e.getMessage());
			return null;
		}
	}
	


	public Config getConfig() {
		return config;
	}
}
