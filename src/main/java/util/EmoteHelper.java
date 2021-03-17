package util;

import java.util.ArrayList;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

/**
 * @author aaron
 * Class to hold emote related static methods
 */
public class EmoteHelper {
	
	/**
	 * Checks string for a unicode emoji which we want to store as htmlHex in the database.
	 * @param s
	 * @return - htmlHex of an emoji or the original string if it's not an emoji
	 */
	public static String getEmoteString(String s) {
		String out = s;
		//Standard unicode emoji
		if(EmojiManager.isEmoji(s)) {
			out = EmojiParser.parseToHtmlHexadecimal(s);
		}
		
		return out;
	}
	
	/**
	 * Attempts to parse a unicode emoji if the string param doesn't start with <
	 * @param s
	 * @return unicode emoji or the emote id if it started with <
	 */
	public static String getEmoteUnicode(String s) {
		String out = s;
		if(!s.startsWith("<")) {
			out = EmojiParser.parseToUnicode(s);
		}
		return out;
	}
	
	/**
	 * Try to parse a default emote from string
	 * This is used for the default emotes from bot
	 * @param s
	 * @return
	 */
	public static String getDefaultEmote(String s) {
		return EmojiManager.getForAlias(s).getUnicode();
	}
	
	/**
	 * Cleans emote id's of everything except the ID
	 * Used for checking if an emote exists on a server
	 * @param s
	 * @return
	 */
	public static String getCustomEmoteID(String s) {
		String out = s;
		if(s.endsWith(">")) {
			out = s.substring(s.lastIndexOf(":")+1, s.length()-1);
		}
		return out;
	}
	
	/**
	 * Cleans emote of <>
	 * Used for adding the reactions.
	 * @param s
	 * @return
	 */
	public static String cleanCustomEmote(String s) {
		String out = s;
		if(s.endsWith(">")) {
			out = s.substring(1, s.length()-1);
		}
		return out;
	}
	
	/**
	 * Custom emote check for valid emotes/keys for DB adding.
	 * @param s
	 * @return True if emoji, discord emote, or 'default' 'none' 'off'
	 */
	public static Boolean isEmote(String s) {
		//Unicode emote
		if(EmojiManager.isEmoji(s)) { 
			return true;
		}
		//Discord emote
		if(s.startsWith("<") && s.endsWith(">")) { 
			return true;
		}
		//Keywords
		//TODO put this in a config or sumthin since it will be referenced in MessageEvent
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("default");
		keys.add("none");
		keys.add("off");
		for(String key : keys) {
			if(s.equalsIgnoreCase(key)) { return true; }
		}
		
		return false;
	}
	
	
}
