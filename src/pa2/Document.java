package pa2;

import java.io.Serializable;

public class Document implements Serializable {
    
    public static final long serialVersionUID = 212387L;
    
    private int versionNumber;
    private String content;
    
    public Document() {
        content = "";
        versionNumber = 0;
    }
        
    public int getVersionNumber() {
        return versionNumber;
    }
    
    public void writeTo(String moreContent) {
        // add new content
        content += moreContent + "\n";
        // update version number
        versionNumber += 1;
    }
    
    public String readFrom() {
        // return content
        return content;
    }

    public void incrementVersionNumber() {
        versionNumber++;
    }
    
    public String toString() {
        return content;
    }

}
