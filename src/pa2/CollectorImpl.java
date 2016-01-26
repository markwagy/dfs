package pa2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CollectorImpl extends Remote {
    public int addRequest(Request.RequestType type, String val) 
            throws RemoteException;
    public String readRequest(int requestNumber)
            throws RemoteException;
    public boolean writeRequest(int requestNumber)
            throws RemoteException;
    public int getNewFileServerPort()
            throws RemoteException;
    public boolean joinFileServerList(String ip, int port)
            throws RemoteException;
    public String getFileServersInfo() 
            throws RemoteException;
}
