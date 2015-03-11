package distributed.systems.core.exception;

import distributed.systems.core.IMessageReceivedHandler;
import distributed.systems.core.Message;
import distributed.systems.das.BattleField;

/**
 * Created by mashenjun on 10-3-15.
 * may be a wrapper for the BattleField.
 */
public class BattleFieldPoxy implements IMessageReceivedHandler{
    private BattleField field;
    public BattleFieldPoxy(BattleField field) {
    }

    @Override
    public void onMessageReceived(Message message) {

    }
}
