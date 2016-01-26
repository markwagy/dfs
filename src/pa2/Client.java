package pa2;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Scanner;

import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.ConfigurationException;

public class Client {
    
    private static Logger log;
    private CollectorImpl collector;
    PropertiesConfiguration config;
    
    private static final String WRITE_TYPE_CHAR = "w";
    private static final String READ_TYPE_CHAR  = "r";
    private static final String QUERY_CHAR = "f";
    private static final String QUIT_CHAR = "q";
    
    public Client(String collectorIP, int collectorPort) {
      collector = Utils.getCollectorInstance(collectorIP, collectorPort);
      if (collector == null) {
          log.fatal("Unable to find collector!");
      }
    }
    
    public static void main(String[] args) {
        // start log4j
        PropertyConfigurator.configure("config/log4j.properties");
        log = Logger.getLogger(Client.class);

        String requestTypeStr = "";
        Client client;
        String writeItem = ""; // what to write in a write request
        
        String collectorIP = Consts.DEFAULT_COLLECTOR_IP;
        int collectorPort = Consts.DEFAULT_COLLECTOR_PORT;
        
        if (args.length < 2) {
            log.warn("Using default collector IP and port: "
                    + collectorIP + ":" + collectorPort);
        } else {
            collectorIP = args[0];
            collectorPort = Integer.valueOf(args[1]);
        }
        
        /**
         * If no arguments are given besides the collector IP and port, 
         * we go into 'interactive mode'.
         * otherwise we are using command line args to drive requests
         * (this is useful for automated testing)
         */
        if (args.length == (2+0)) {
            doInteractive(collectorIP, collectorPort);
        } else if (args.length == (2+1) ||
                (args.length == (2+1) && "".equals(args[2]))) {
            // only one arg means read request
            requestTypeStr = args[2];
            /*
             * if the request type is not "r" for read, 
             * the client is not being used properly
             */
            log.info("Request type: " + requestTypeStr);
            if (!requestTypeStr.equalsIgnoreCase(READ_TYPE_CHAR)) {
                println("ERROR: One argument to the program " + 
                        "should only be used with read requests!");
                System.exit(1);
            }
            
            client = new Client(collectorIP, collectorPort);
            String readResult = client.requestRead();
            System.out.println("Read ---\n" + readResult 
                    + "---\n from the data store document");
            
        } else if (args.length == (2 + 2)) {
            /*
             * two args for write request: one to tell that it is a write request
             * and another, the actual value to write.
             * If anything but a write request is being used with two args,
             * the program is not being used correctly
             */
            requestTypeStr = args[2];
            log.info("Request type: " + requestTypeStr);
            if (!requestTypeStr.equalsIgnoreCase(WRITE_TYPE_CHAR)) {
                println("ERROR: Two arguments to the program " + 
                        "should only be used with write requests!");
                System.exit(1);
            }
            
            writeItem = args[3];
            log.info("Write item: " + writeItem);
            log.info("Starting Client");
            client = new Client(collectorIP, collectorPort);
            boolean success = client.requestWrite(writeItem);
            if(success) {
                System.out.println("Successfully wrote '"
                        + writeItem + "' to the data store document");
            }
        } else {
            System.err.println("Usage: client <r or w> " + 
                    "[if w: string to write (surrounded in quotes)]");
            System.exit(1);
        }
        
    }
    
    private static void printMenu() {
        println("Would you like to:");
        println("(" + READ_TYPE_CHAR + ") Request a read");
        println("(" + WRITE_TYPE_CHAR + ") Request a write");
        println("(" + QUERY_CHAR + ") Query file servers through Collector");
        println("(" + QUIT_CHAR + ") Quit");
    }
    
    /**
     * Abstraction for the sake of less typing
     */
    private static void println(String line) {
        System.out.println(line);
    }
    
    private static String readln() {
        // print prompt
        System.out.println(">> ");
        // get line
        Scanner input = new Scanner(System.in);
        String val = input.nextLine();
        return val;
    }
    
    private String requestRead() {
        int requestNumber = -1;
        try {
            /*
             * need to do this in two parts to allow for a request to 
             * be insered into a queue, and then retrieved when the request
             * return value is read (per specs)
             */
            requestNumber = collector.addRequest(Request.RequestType.Read, "");
            String readResult = collector.readRequest(requestNumber);
            return readResult;
        } catch (RemoteException ex) {
            log.error("Remote exception to collector: " + ex);
            return null;
        }
    }
    
    private boolean requestWrite(String writeVal) {
        int requestNumber = -1;
        try {
            requestNumber = collector.addRequest(Request.RequestType.Write, writeVal);
            boolean writeSuccess = collector.writeRequest(requestNumber);
            return writeSuccess;
        } catch (RemoteException ex) {
            log.error("Remote exception trying to fulfull a write request: " + ex);
            return false;
        }
    }
    
    private static void doInteractive(String collectorIP, int collectorPort) {
        Client client = new Client(collectorIP, collectorPort);
        while (true) {
            printMenu();
            String choice = readln();
            // exit if they want to quit
            if (choice.equalsIgnoreCase(QUIT_CHAR)) {
                System.exit(0);
            }
            
            if (choice.equalsIgnoreCase(WRITE_TYPE_CHAR)) {
                println("Please type what you would like to write" +
                        " to the shared document: ");
                String writeItem = readln();
                if(client.requestWrite(writeItem)) {
                    println("Wrote " + writeItem + " successfully");
                } else {
                    println("Error writing " + writeItem);
                }
            } else if (choice.equalsIgnoreCase(READ_TYPE_CHAR)) {
                println("Read the following from the file system: \n"
                        + client.requestRead());
            } else if (choice.equalsIgnoreCase(QUERY_CHAR)) {
                CollectorImpl collector = Utils.getCollectorInstance(collectorIP, collectorPort);
                String infoString;
                try {
                    infoString = collector.getFileServersInfo();
                    println("Got this info from Collector:");
                    println(infoString);
                } catch (RemoteException ex) {
                    log.error("Remote exception: " + ex);
                }
            } else {
                println("Unknown option");
            }
        }
    }
}
            
            