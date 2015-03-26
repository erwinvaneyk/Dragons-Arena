package distributed.systems.network.messagehandlers;

import distributed.systems.core.IMessageReceivedHandler;

public interface MessageHandler extends IMessageReceivedHandler {

	public static final String MESSAGE_TYPE_DEFAULT = "DEFAULT";

	public String getMessageType();
}
