package pa2;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTest {
    
    @Test
    public void testGetVersionNumber() {
        System.out.println("getVersionNumber");
        Document doc = new Document();
        int expResult = 0;
        int result = doc.getVersionNumber();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetVersionNumberIncrement() {
        System.out.println("getVersionNumber increment");
        Document doc = new Document();
        int expected = 2;
        doc.writeTo("D Gibbons");
        doc.writeTo(" is a bad man");
        int result = doc.getVersionNumber();
        assertEquals(expected, result);
    }

    @Test
    public void testWriteToReadFrom() {
        System.out.println("writeTo");
        String moreContent = "I once saw a cat\n";
        String yetMoreContent = "It was a black cat\n";
        String evenMoreContent = "Yes it was";
        Document doc = new Document();
        doc.writeTo(moreContent);
        doc.writeTo(yetMoreContent);
        doc.writeTo(evenMoreContent);
        String getContent = doc.readFrom();
        String expected = "I once saw a cat\nIt was a black cat\nYes it was";
        assertEquals(expected, getContent);
    }


}
