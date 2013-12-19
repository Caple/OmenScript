package pw.caple.mc.omenscript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Reads existing scripts written in the OmenScript scripting language.
 */
public final class ScriptReader {

	private final int tabWorth;
	private final Logger log;

	public ScriptReader() {
		tabWorth = OmenScript.getInstance().getConfig().getInt("tab");
		log = OmenScript.getInstance().getLogger();
	}

	/**
	 * Returns a list of nodes if the file is valid, else returns null.
	 */
	public List<ScriptNode> read(File file) {
		if (!file.canRead()) {
			log.warning("IO Error. Can not open for reading. Ignored script " + file.getName());
			return null;
		}
		;

		List<ScriptNode> rootNodes = new ArrayList<>();
		Map<Integer, ScriptNode> lastNodeAtGivenDepth = new HashMap<>();
		String line;
		String scriptName = file.getName();
		StringBuilder key = new StringBuilder(50);
		StringBuilder value = new StringBuilder(50);
		boolean readingKey = false;
		boolean readingValue = false;
		int depth = 0;
		int lineNumber = 1;
		try (
			BufferedReader br = new BufferedReader(new FileReader(file))) {
			while ((line = br.readLine()) != null) {
				for (char character : line.toCharArray()) {
					switch (character) {
					case '\t':
						if (readingKey) {
							key.append(character);
						} else if (readingValue) {
							value.append(character);
						} else {
							depth += tabWorth;
						}
						break;
					case ' ':
						if (readingKey) {
							key.append(character);
						} else if (readingValue) {
							value.append(character);
						} else {
							depth++;
						}
						break;
					case ':':
						if (readingKey) {
							readingKey = false;
							readingValue = true;
						} else if (readingValue) {
							value.append(character);
						}
						break;
					default:
						if (readingValue) {
							value.append(character);
						} else {
							readingKey = true;
							key.append(character);
						}
						break;
					}
				}
				if (readingValue || readingKey) {
					ScriptNode node = new ScriptNode(line, key.toString().toLowerCase().trim(), value.toString().trim(), scriptName, lineNumber);
					if (depth == 0) {
						rootNodes.add(node);
					} else {
						ScriptNode parentNode = lastNodeAtGivenDepth.get(depth - 2);
						if (parentNode != null) {
							parentNode.addChild(node);
						} else {
							log.warning("Parser error! [" + scriptName + "]: Wrong number of spaces at line " + lineNumber + ". -> " + line.trim());
						}
					}
					lastNodeAtGivenDepth.put(depth, node);
				}
				depth = 0;
				readingValue = false;
				readingKey = false;
				key.delete(0, key.length());
				value.delete(0, value.length());
				lineNumber++;
			}
			return rootNodes;
		} catch (FileNotFoundException e) {
			log.warning("Script not found: " + file.getPath());
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("IO Error. Ignored script " + file.getName());
		}
		return null;
	}
}
