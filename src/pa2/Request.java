package pa2;

import java.sql.Time;
import org.apache.log4j.Logger;

public class Request {
    
    public static enum RequestType {Read, Write};
    
    RequestType type;
    Time timeStamp;
    int number;
    private static Logger log;
    String val;
    
    public Request(Request.RequestType type) {
        log = Logger.getLogger(Request.class);
        if (type != RequestType.Read) {
            log.error("Trying to create a non-read request " +
                    "with no values attached. Not not not good");
        }
        this.type = type;
        timeStamp = new Time(System.currentTimeMillis());
        number = 0;
    }
    
    public Request(Request.RequestType type, String val) {
        log = Logger.getLogger(Request.class);
        this.type = type;
        timeStamp = new Time(System.currentTimeMillis());
        number = 0;
        this.val = val;
    }
    
    
    public void setNumber(int requestNumber) {
        number = requestNumber;
    }
    
    public int getNumber() {
        return number;
    }
    
    public String getVal() {
        return val;
    }
    
    @Override
    public String toString() {
        String str = "<" + timeStamp + "> ";
        if (this.type == RequestType.Read) {
            return str + "READ";
        } else {
            return str + "WRITE";
        }
    }
}
