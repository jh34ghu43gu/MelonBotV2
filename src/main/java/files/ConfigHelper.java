package files;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class ConfigHelper {
	
	private static final Logger log = (Logger) LoggerFactory.getLogger(ConfigHelper.class);
	
	private String fileName;
	private HashMap<String, String> options;
	
	public ConfigHelper() {
		this.fileName = "config.json";
		this.options = new HashMap<String, String>();
	}
	
	/**
	 * Copies file to an OLDconfig.json file
	 */
	public void copyFile() {
		log.debug("Copying config file to OLD{}", fileName);
		File config = new File(fileName);
		Path newFilePath = Paths.get("OLD" + fileName);
		Path originalPath = config.toPath();
	    try {
			Files.copy(originalPath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("Error while copying config.");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Clears the options map
	 */
	public void clear() {
		log.debug("Clearing options hashmap");
		options.clear();
	}
	
	/**
	 * @return size of options hashmap
	 */
	public int size() {
		log.debug("Hashmap options length = {}", options.size());
		return options.size();
	}
	
	/**
	 * @return size of json file's options{} -1 for error
	 */
	@SuppressWarnings("rawtypes")
	public int fileSize() {
		JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader(fileName))
        {
            Object obj = jsonParser.parse(reader);
 
            JSONObject configObj = (JSONObject) obj;
            JSONObject options = (JSONObject) configObj.get("Options");
            Set set = options.keySet();
            log.debug("Json file options length = {}", set.size());
    	    return set.size();
            
        } catch (Exception e) {
        	log.error("Error while parsing json file.");
            e.printStackTrace();
            return -1;
        }
	}
	
	/**
	 * @return if config.json (or whatever fileName is) already exists
	 */
	public boolean exists() {
		File config = new File(fileName);
		return(config.exists());
	}
	
	/**
	 * Adds an option/value pair to options hashmap
	 * @param option
	 * @param value - null values will be made into ""
	 */
	public void addOption(String option, String value) {
		if(value != null) {
			log.debug("Adding option '{}' with value '{}' to options", option, value);
			options.put(option, value);
		} else {
			log.debug("Adding option '{}' with empty string value to options", option);
			options.put(option, "");
		}
		
	}
	
	/**
	 * Build config based on options, will clear options if successful
	 * @return true if no errors, will also return false if options is empty
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean build() {
		//Don't build if we don't have any options, we will always need at least a login token
		if(options.isEmpty()) { 
			log.warn("Could not build config, options map empty.");
			return false; 
		}
		
		JSONObject configOptions = new JSONObject();
		//Iterate through options and put inside configOptions
		Set set = options.entrySet();
		Iterator iterator = set.iterator();
	    while(iterator.hasNext()) {
	    	Map.Entry mentry = (Map.Entry)iterator.next();
	    	log.debug("Adding key '{}' with value '{}' to jsonObject", mentry.getKey(), mentry.getValue());
	    	configOptions.put(mentry.getKey(), mentry.getValue());
	    }
		
	    //Actually make config
		JSONObject configObj = new JSONObject();
		configObj.put("Options", configOptions);
		try {
			log.debug("Attempting json file write");
			FileWriter writer = new FileWriter(fileName);
			writer.write(configObj.toJSONString());
			writer.flush();
			writer.close();
			options.clear(); //Clear hashmap to save memory
			log.debug("Json file write successful, hashmap cleared");
			return true;
		} catch (Exception e) {
			log.error("Error occured writing json file.");
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Retrieve all options from config file and store in hashmap
	 * @return true if no errors
	 */
	@SuppressWarnings("rawtypes")
	public boolean getOptions() {
		JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader(fileName))
        {
            Object obj = jsonParser.parse(reader);
 
            JSONObject configObj = (JSONObject) obj;
            JSONObject options = (JSONObject) configObj.get("Options");
            
            //Might be able to just assign this.options = options.keySet()
            //However this ensures correct data type cast so I'm not going to try changing it
            Set set = options.keySet();
            Iterator iterator = set.iterator();
    	    while(iterator.hasNext()) {
    	    	Map.Entry mentry = (Map.Entry)iterator.next();
    	    	this.options.put((String)mentry.getKey(), (String)mentry.getValue());
    	    }
    	    return true;
            
        } catch (Exception e) {
        	log.error("Error while parsing json file.");
            e.printStackTrace();
            return false;
        }
	}
	
	/**
	 * Gets a single option from the hashmap
	 * @param option
	 * @return String value of the option
	 */
	public String getOption(String option) {
		log.debug("Attempting to get option '{}' from hashmap", option);
		return options.get(option);
	}
	
	/**
	 * Gets a single option directly from file
	 * Use if we don't want to keep a hashmap of all options in memory for an object, such as main
	 * @param option
	 * @return String value of the option or "Error" if there was an exception
	 */
	public String getOptionFromFile(String option) {
		log.debug("Attempting to get option '{}' from json file", option);
		JSONParser jsonParser = new JSONParser();
        
        try (FileReader reader = new FileReader(fileName))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            JSONObject configObj = (JSONObject) obj;
            JSONObject options = (JSONObject) configObj.get("Options");
            
            return (String) options.get(option);
            
        } catch (Exception e) {
        	log.error("Error while parsing json file.");
        	e.printStackTrace();
            return "Error";
        }
	}
}
