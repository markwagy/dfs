package pa2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FileServer implements FileServerImpl {
    private static Logger log;
    private String ip;
    private int port;
    private Document doc;
    CollectorImpl collector;
    
    public FileServer(String collectorIP, int collectorPort) {
        doc = new Document();
        collector = Utils.getCollectorInstance(collectorIP, collectorPort);
        ip = getIP();
        port = getNewPort();
    
    }
    
    public static void main(String args[]) {
        
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger(FileServer.class);
        
        if (args.length < 2) {
            System.out.println("Usage: FileServer <collector ip> <collector port>");
            System.exit(1);
        } 
        String collectorIP = args[0];
        int collectorPort = Integer.valueOf(args[1]);
        
        log.info("Starting FileServer");
        
        log.info("Starting FileServer... creating registry and binding");
        try {

            // create instance
            FileServerImpl fs = new FileServer(collectorIP, collectorPort);
            log.debug("created fs");
            // create stub
            FileServerImpl stub = (FileServerImpl) 
                    UnicastRemoteObject.exportObject(fs, fs.getPort());
            log.debug("created stub");
            // create registry
            Registry registry =
                    LocateRegistry.createRegistry(fs.getPort());
            log.equals("created registry");
            // bind object to registry
            registry.rebind(Consts.FILESERVER_RMI_DESC, stub);
            log.info(fs + " listening");
            fs.join();
            log.info("File server at " + fs.getPort() + " finished with startup");
        } catch (Exception e) {
            log.error("FileServer exception: " + e);
        }
    }
    
    /**
     * Join the list of file servers that the collector is aware of
     */
    @Override
    public void join() throws RemoteException {
        try {
            collector.joinFileServerList(ip, port);
        } catch (RemoteException ex) {
            log.error("Problem joining Collector's file server list: " + ex);
        }
    }

    /**
     * Get a new port for this file server from the collector
     * @return 
     */
    public int getNewPort() {
        try {
            // get a valid port number from the collector
            // who keeps track of this port and ip combo for his own purposes)
            return collector.getNewFileServerPort();
        } catch (RemoteException ex) {
            log.error("Problem getting new port for this file server: " + ex);
        }
        return -1;
    }
    
    /**
     * Getter for this file server's port
     * @return 
     */
    @Override
    public int getPort() throws RemoteException {
        return port;
    }

    /**
     * get this machine's (and hence this file server's) IP address
     * @return 
     */
    public String getIP() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("I can't find my own ip address: " + e);
            return null;
        }
    }

    @Override
    public String readFromDocument() throws RemoteException {
        return doc.readFrom();
    }

    @Override
    public void writeToDocument(String newContents) throws RemoteException {
        doc.writeTo(newContents);
    }

    @Override
    public int getVersionNumber() throws RemoteException {
        return doc.getVersionNumber();
    }
    
    @Override
    public String toString() {
        return "<FS>[" + ip + ":" + port + "]";
    }

    @Override
    public String getInfo() throws RemoteException {
        return this.toString() + "<doc version number: " 
                + doc.getVersionNumber() + ">\n";
    }

    @Override
    public Document getDoc() throws RemoteException {
        return this.doc;
    }

    @Override
    public void setDoc(Document doc) throws RemoteException {
        this.doc = doc;
    }
}
