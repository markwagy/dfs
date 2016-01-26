package pa2;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class Utils {
    private static Logger log;
    
    public static PropertiesConfiguration getConfigInstance() {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration(Consts.PROPERTIES_FILE_NAME);
            return config;
        } catch (ConfigurationException ex) {
            log.error("Config Exception when getting config instance" + ex);
        } catch (Exception e) {
            log.error("Exception when getting config instance: " + e);
        }
        return null;
    }
    
    public static String getConfigVal(String key) throws ConfigurationException {
        PropertiesConfiguration config = Utils.getConfigInstance();
        return config.getString(key);
    }
    
    public static int getNw() {
        int Nw = -1;
        PropertiesConfiguration conf = getConfigInstance();
        Nw = conf.getInt("Nw");
        return Nw;
    }
    
    public static int getNr() {
        int Nr = -1;
        PropertiesConfiguration conf = getConfigInstance();
        Nr = conf.getInt("Nr");
        return Nr;
    }
    
    
    public static boolean giffordConstraintsAreSatisfied(int N, int Nw, int Nr) {
        log = Logger.getLogger(Utils.class);
        if (! (Nr + Nw > N) ) {
            log.fatal("Nr + Nw must be greater than N! "
                    + "(Nr = " + Nr + ", Nw = " + Nw + ", N = " + N + ")");
            return false;
        }
        if (! (Nw > (N/2)) ) {
            log.fatal("Nw must be greater than N/2! "
                    + "(Nw = " + Nw + ", N = " + N + ")");
            return false;
        }
        return true;
    }
    
    public static int getMaxVal(int[] vals) {
        int max = Integer.MIN_VALUE;
        for (int i=0; i<vals.length; i++)
            if (vals[i] > max)
                max = vals[i];
        return max;
    }
    
    
    /**
     * Get RMI reference to the Collector
     */
    public static CollectorImpl getCollectorInstance(String collectorIP, int collectorPort) {
        log = Logger.getLogger(Utils.class);
        
        CollectorImpl collector = null;
        
        try {
            Registry registry =
                    LocateRegistry.getRegistry(collectorIP, collectorPort);
            try {
                collector =
                        (CollectorImpl) registry.lookup(Consts.COLLECTOR_RMI_DESC);
                return collector;
            } catch (NotBoundException ex1) {
                log.error("Couldn't lookup registry because it isn't bound: "
                        + ex1);
            } catch (AccessException ex1) {
                log.error("Couldn't lookup registry due to lack of access: "
                        + ex1);
            }
        } catch (RemoteException e) {
            log.error("Remote exception: " + e);
        }
        if (collector == null) {
            log.fatal("Unable to contact the Collector!");
        }
        return null;
    }
    
    /**
     * Get RMI reference to the FileServer instance
     */
    public static FileServerImpl getFileServerInstance(String ip, int port) {
        log = Logger.getLogger(Utils.class);
        FileServerImpl fileServer;
        
        try {
            Registry registry =
                    LocateRegistry.getRegistry(ip, port);
            try {
                fileServer =
                        (FileServerImpl) registry.lookup(Consts.FILESERVER_RMI_DESC);
                return fileServer;
            } catch (NotBoundException ex1) {
                log.error("Couldn't lookup registry because it isn't bound: "
                        + ex1);
            } catch (AccessException ex1) {
                log.error("Couldn't lookup registry due to lack of access: "
                        + ex1);
            }
        } catch (RemoteException e) {
            log.error("Remote exception: " + e);
        }
        return null;
    }
}
