import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by mashenjun on 11-3-15.
 */
public interface test extends Remote {
    public void Hello () throws RemoteException;
}
