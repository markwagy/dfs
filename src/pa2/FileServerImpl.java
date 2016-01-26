package pa2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileServerImpl extends Remote {
    public String readFromDocument() 
            throws RemoteException;
    public void writeToDocument(String newContents) 
            throws RemoteException;
    public int getVersionNumber() 
            throws RemoteException;
    public int getPort() 
            throws RemoteException;
    public String getInfo()
            throws RemoteException;
    public void join() 
            throws RemoteException;
    public Document getDoc()
            throws RemoteException;
    public void setDoc(Document doc)
            throws RemoteException;
}
