package pa2;

import java.util.LinkedList;
import org.apache.log4j.Logger;

public class RequestQueue extends LinkedList<Request> {
    
    Logger log;
    private int requestNumber;
    
    public RequestQueue() {
        log = Logger.getLogger(RequestQueue.class);
        requestNumber = 0;
    }
    
    /**
     * Add a value to the queue and return the position that 
     * the added value has in the queue... note, this adds to the
     * end of the queue
     */
    public int addRequest(Request req) {
        req.setNumber(requestNumber);
        boolean success = super.add(req);
        if(success) {
            log.info("Added " + req + " to queue");
        } else {
            log.error("Problem adding " + req + " to queue");
        }
        requestNumber++;
        // TODO: need to make the following work...
//        return requestNumber;
        return this.size()-1;
    }
    
    public Request getRequest(int requestNumber) {
        for (Request req : this) {
            if (req.getNumber() == requestNumber) {
                return req;
            }
        }
        log.error("Could not find request with request number " 
                + requestNumber);
        return null;
    }
}
