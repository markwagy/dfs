package pa2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import pa2.Request.RequestType;

public class Collector implements CollectorImpl {

    private static Logger log;
    private RequestQueue requestQueue;
    private ArrayList<FileServerImpl> fileServers;
    
    private String ip;
    private int port;
    
    // maintains a current file server port to assign to joining file servers
    private int currentFileServerPort;
    private final int Nw;
    private final int Nr;
    
    private PropertiesConfiguration config;
    
    private final String NW_FIELD_NAME = "Nw";
    private final String NR_FIELD_NAME = "Nr";
    
    public Collector() {
        this(Consts.DEFAULT_COLLECTOR_IP, Consts.DEFAULT_COLLECTOR_PORT);
    }
    
    public Collector(String ip, int port) {
        fileServers = new ArrayList<FileServerImpl>();
        requestQueue = new RequestQueue();
        currentFileServerPort = Consts.FILESERVER_PORT_OFFSET;
        this.ip = ip;
        this.port = port;
        try {
            config = new PropertiesConfiguration("config/config.properties");
        } catch (ConfigurationException ex) {
            log.error("Problem loading properties config file: " + ex);
        }
                
        this.Nw = config.getInt(NW_FIELD_NAME);
        this.Nr = config.getInt(NR_FIELD_NAME);
    }
    
    public static void main(String args[]) {
        // start log4j
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger(Collector.class);

        String ip;
        int port;
        
        if (args.length == 0) {
            log.warn("Using default collector ip and port: " 
                    + Consts.DEFAULT_COLLECTOR_IP + ":" 
                    + Consts.DEFAULT_COLLECTOR_PORT);
            ip = Consts.DEFAULT_COLLECTOR_IP;
            port = Consts.DEFAULT_COLLECTOR_PORT;
        } else {
            ip = args[0];
            port = Integer.valueOf(args[1]);
        }
        // create collector instance
        CollectorImpl collector = new Collector(ip, port);
        
        log.info("Starting Collector... creating registry and binding");
        try {
            // create stub
            CollectorImpl stub =
                    (CollectorImpl) UnicastRemoteObject.exportObject(
                        collector, port);
            // create registries
            Registry registry1 =
                    LocateRegistry.createRegistry(port);
            Registry registry2 = 
                    LocateRegistry.createRegistry(port+1);
            Registry registry3 = 
                    LocateRegistry.createRegistry(port+2);
            // bind object to registries
            registry1.rebind(Consts.COLLECTOR_RMI_DESC, stub);
            registry2.rebind(Consts.COLLECTOR_RMI_DESC, stub);
            registry3.rebind(Consts.COLLECTOR_RMI_DESC, stub);
            log.info(collector + " listening on ports: " + port + "," + (port+1) + "," + (port+2));
            log.info("Collector finished with startup");
        } catch (Exception e) {
            log.error("Collector exception: " + e);
        }
    }

    /**
     * Add a request to the request queue
     * @param type: type of request
     * @param val: value to write if this is a write request
     * @return int requestNumber: the request number to reference to get result
     * @throws RemoteException 
     */
    @Override
    public int addRequest(RequestType type, String val) 
            throws RemoteException {
        Request req = new Request(type, val);
        return requestQueue.addRequest(new Request(type, val));
    }

    @Override
    synchronized public String readRequest(int requestNumber) throws RemoteException {
        // assemble read quorum
        boolean giffordConstraintsOk = 
                Utils.giffordConstraintsAreSatisfied(getN(), getNw(), getNr());
        if (!giffordConstraintsOk) {
            log.warn("Read request not valid until Gifford constraints are satisfied, "
                    + "skipping request");
            return "";
        }
        int[] readQuorum = getReadQuorumList();
        log.info("Read quorum file servers selected: ");
        for (int i=0; i<readQuorum.length; i++)
            log.info("File Server " + readQuorum[i]);
        int[] versionNumbers = getVersionNumbersList(readQuorum);
        int maxVersion = getMaxVersionNumber(versionNumbers);
        log.info("Latest version number = " + maxVersion);
        Document readDoc = getDocWithVersionNumber(readQuorum, maxVersion);
        return readDoc.toString();
    }

    @Override
    synchronized public boolean writeRequest(int requestNumber) throws RemoteException {
        boolean giffordConstraintOk = 
                Utils.giffordConstraintsAreSatisfied(getN(), getNw(), getNr());
        if (!giffordConstraintOk) {
            log.warn("Write request not valid until Gifford constraints are satisfied, "
                    + "skipping request");
            return false;
        }
        // TODO: check to see if our request is the current one
        // NOTE: Collector probably needs to be multi-threaded to
        // handle multiple requests coming in...
        /*
        if (!(requestQueue.peek().getNumber() == requestNumber)) {
            try {
                wait(Consts.WAIT_TIME);
            } catch (InterruptedException ex) {
                log.error("Impatient process. Can't Wait: " + ex);
            }
        }
         */
        int[] writeQuorum = getWriteQuorumList();
        log.info("Write quorum file servers selected: ");
        for (int i=0; i<writeQuorum.length; i++)
            log.info("File Server " + writeQuorum[i]);
        int[] versionNumbers = getVersionNumbersList(writeQuorum);
        int maxVersion = getMaxVersionNumber(versionNumbers);
        log.info("Latest version number = " + maxVersion);
        Document latestDocInQuorum = getDocWithVersionNumber(writeQuorum, maxVersion);
        Request request = requestQueue.get(requestNumber);
        String valToWrite = request.getVal();
        latestDocInQuorum.writeTo(valToWrite);
        updateFileServersWithDoc(writeQuorum, latestDocInQuorum);
        return true;
    }

    /**
     * File Servers contact the collector here to get the port that they 
     * should run on.
     * @param ip
     * @return
     * @throws RemoteException 
     */
    @Override
    public int getNewFileServerPort() throws RemoteException {
        currentFileServerPort++;
        return currentFileServerPort;
    }
    
    @Override
    public boolean joinFileServerList(String ip, int port) throws RemoteException {
        FileServerImpl fs =  Utils.getFileServerInstance(ip, port);
        fileServers.add(fs);
        return true;
    }

    @Override
    public String getFileServersInfo() throws RemoteException {
        String infoStr = "FS INFO: \n";
        if (fileServers.isEmpty()) {
            infoStr = "No file servers to query";
        }
        else {
            for (FileServerImpl fs : fileServers) {
                infoStr += fs.getInfo();
            }
        }
        return infoStr;
    }
    
    @Override
    public String toString() {
        return "Collector [" + ip + ":" + port + "]";
    }
    
    /**
     * Get the number of active file servers
     * @return 
     */
    private int getN() {
        return fileServers.size();
    }
    
    private int getNw() {
        return Nw;
    }
    
    private int getNr() {
        return Nr;
    }
    
    /**
     * Get a list of the doc version numbers on file servers
     */
    private int[] getVersionNumbersList() {
        int[] versionNumbers = new int[fileServers.size()];
        int i=0;
        for (FileServerImpl fs : fileServers) {
            try {
                versionNumbers[i] = fs.getVersionNumber();
            } catch (RemoteException ex) {
                log.error(
                        "Problem getting version number from " + fs + ": " + ex);
            }
        }
        return versionNumbers;
    }
    
    private int[] getVersionNumbersList(int[] fileServerIndices) {
        int[] versionNumberList = new int[fileServerIndices.length];
        for (int i=0; i< fileServerIndices.length; i++) {
            FileServerImpl fs = fileServers.get(i);
            try {
                versionNumberList[i] = fs.getVersionNumber();
            } catch (RemoteException ex) {
                log.error(
                        "Problem getting version number from " 
                        + fs + ": " + ex);
            }
        }
        return versionNumberList;
    }
    
    /**
     * Get the maximum/most recent version number in file servers
     * @param versionNumbers: list of existing version numbers
     * @return the maximum version number in file servers
     */
    private int getMaxVersionNumber(int[] versionNumbers) {
        int max = -1;
        for (int num : versionNumbers) {
            if (num > max) {
                max = num;
            }
        }
        // if the maximum is less than zero, we have a problem
        if (max < 0) {
            log.error("Unable to find max version number");
        }
        return max;
    }
    
    private int getVersionNumberAtFileServer (int nodeIndex) {
        int versionNumber = -1;
        int[] fsVersionList = getVersionNumbersList();
        if (nodeIndex > fsVersionList.length) {
            log.fatal("Given node index (" + nodeIndex + ") is greater than "
                    + "number of nodes (" + fsVersionList.length + ")!");
        }
        return fsVersionList[nodeIndex];
    }
    
    /**
     * Get a random list of numbers from 0 to number of nodes
     * @param n
     * @return 
     */
    private int[] getRandomListOfNodeIndices(int n) {
        // create list of successive numbers and shuffle them
        ArrayList<Integer> nums = new ArrayList<Integer>(getN());
        for (int i=0; i<getN(); i++) {
            nums.add(new Integer(i));
        }
        Collections.shuffle(nums);
        // now add to return list
        int[] returnList = new int[n];
        for (int i=0; i<n; i++) {
            returnList[i] = nums.get(i).intValue();
        }
        return returnList;
    }
    
    /**
     * Get a list of file server indices to make up read quorum
     */
    private int[] getReadQuorumList() {
        if (Nr < 0) {
            log.fatal("Incorrect value of Nr!");
        } else if (Nr > getN()) {
            log.error("Not enough total file servers (" + getN() 
                    + ") to make read quorum (" + Nr + ")!");
        }
        return getRandomListOfNodeIndices(Nr);
    }
    
    /**
     * Get a list of file server indices to make up write quorum
     */
    private int[] getWriteQuorumList() {
        if (Nw < 0) {
            log.fatal("Incorrect value of Nw!");
        } else if (Nw > getN()) {
            log.error("Not enough total file servers (" + getN() 
                    + ") to make write quorum (" + Nw + ")!");
        }
        return getRandomListOfNodeIndices(Nw);
    }

    /**
     * Given a set of file server indices, get a doc with given version
     */
    private Document getDocWithVersionNumber(int[] fileServerIndices, int version) {
        for (int idx : fileServerIndices) {
            FileServerImpl fs = fileServers.get(idx);
            try {
                if (fs.getVersionNumber() == version) {
                    return fs.getDoc();
                }
            } catch (RemoteException ex) {
                log.error("Problem getting version number for " + fs + ": " + ex);
            }
        }
        log.error("Couldn't find file server with given doc version (" 
                + version + ")");
        return null;
    }

    private void updateFileServersWithDoc(int[] fileServerIndices, Document doc) {
        for (int fsIndex : fileServerIndices) {
            FileServerImpl fs = fileServers.get(fsIndex);
            try {
                fs.setDoc(doc);
            } catch (RemoteException ex) {
                log.error("Unable to write doc to " + fs + ": " + ex);
            }
        }
    }

}
