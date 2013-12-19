package pw.caple.mc.omenscript;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a single, valid line of a script. Script nodes are organized in a
 * hierarchical fashion to reflect the scripting language they represent. A
 * script node is pulled from a script, regardless of it's content and children.
 * It is not validated until it is run through the {@link ScriptCompiler}
 */
public final class ScriptNode {

	final private List<ScriptNode> children = new ArrayList<>();
	final private String key;
	final private String value;
	final private String scriptName;
	final private String line;
	final private int lineNumber;

	public ScriptNode(String line, String key, String value, String scriptName, int lineNumber) {
		this.line = line;
		this.key = key;
		this.value = value;
		this.scriptName = scriptName;
		this.lineNumber = lineNumber;
	}

	final public void addChild(ScriptNode child) {
		children.add(child);
	}

	final public boolean hasChildren() {
		return children.size() > 0;
	}

	final public List<ScriptNode> getChildren() {
		return children;
	}

	final public String getKey() {
		return key;
	}

	final public String getValue() {
		return value;
	}

	final public String getScriptName() {
		return scriptName;
	}

	final public String getLine() {
		return line;
	}

	final public int getLineNumber() {
		return lineNumber;
	}

}
