package core.text_extractors;

import static org.junit.Assert.*;

import java.io.Console;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.junit.Test;

public class JavaRegexTest {
	
	public Pair<String, String> getNameAndFilename(String value) {
		Pattern pattern = Pattern.compile("\".*?\"");
		Matcher matcher = pattern.matcher(value);
		
		boolean found = false;
		ArrayList<String> r = new ArrayList<String>();
    while (matcher.find()) {
			int beg = matcher.start();
			int end = matcher.end();
			found = true;
			  
			if ((end - beg) > 2)
				r.add(value.substring(matcher.start()+1, matcher.end()-1));
    }
    
    if(found) {
    	return Pair.fromCollection(r);
    } else {
    	return Pair.with("",  "");
    }
	}
	
	@Test
	public void testParsePOSTFile() {
		// http://docs.oracle.com/javase/tutorial/essential/regex/test_harness.html
		
		String value = "Content-Disposition: form-data; name=\"myfile\"; filename=\"2_data.srt\"";
		Pattern pattern = Pattern.compile("\".*?\"");
		Matcher matcher = pattern.matcher(value);
		
		boolean found = false;
    while (matcher.find()) {
    	int beg = matcher.start();
    	int end = matcher.end();
        System.out.format("I found the text" +
            " \"%s\" starting at " +
            "index %d and ending at index %d.%n",
            matcher.group(),
            beg,
            end);
        found = true;
        
        if ((end - beg) > 2)
        	System.out.println(value.substring(matcher.start()+1, matcher.end()-1));
    }
    if(!found){
        //console.format("No match found.%n");
    }
		
	}
	
	@Test
	public void testFolded() {
		String value = "Content-Disposition: form-data; name=\"myfile\"; filename=\"2_data.srt\"";
		Pair<String, String> r = this.getNameAndFilename(value);
		assertFalse(r.getValue0().isEmpty());
		assertFalse(r.getValue1().isEmpty());
	}

}
