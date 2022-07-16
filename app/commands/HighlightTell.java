package commands;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class HighlightTell implements DummyTell {
	
	ArrayList<ObjectNode> messages = new ArrayList<ObjectNode>();

	

	@Override
	public void tell(ObjectNode message) {
		
		// only trap messages which are instructions for highlighting a tile on the UI
		if (message.has("messagetype")) {
			if (message.findValuesAsText("messagetype").get(0).equals("drawTile")) {
				this.messages.add(message);
			}
		}
	}

	
	public Iterable<ObjectNode> readMessages() {
		return messages;
	}
	
	public void resetMessages() {
		messages.clear();
	}
	
}


