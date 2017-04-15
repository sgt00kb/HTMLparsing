package PitchBookSED;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.nodes.TextNode;

public class InterviewProjectMain {

	public static void main(String[] args) throws IOException {
		
		//Testing url
		String url = "https://www.w3schools.com/html/html_layout.asp";
		String outputfileName = "output.txt";
		
		extractsHTML(url, outputfileName);
	}
	
	private static void extractsHTML(String url, String outputfileName) {
		String html = url(url);
		
		ArrayList<String> links = new ArrayList<>();
		linkFilter(html,links);
		writeFile(outputfileName, links, "links");
		
		ArrayList<String> tagList = new ArrayList<>();
		tagFilter(html, tagList);
		writeFile(outputfileName, tagList, "HTML");
		
		// Adding everything that meets the third requirement to thirdResult
		ArrayList<String> thirdResult = new ArrayList<String>();
		ThirdRequirementHTMLTagTreeTraversal(html, thirdResult);
		writeFile(outputfileName, thirdResult, "sequences");
	}
	
	//NodeText: All the text from node will be saved in here
	private static void ThirdRequirementHTMLTagTreeTraversal(String html, ArrayList<String> thirdRequirementResult){

		    Document doc = Jsoup.parse(html);
		    ArrayList<Node> listNode = new ArrayList<Node>();
		    ArrayList<String> NodeText = new ArrayList<>();

		    Node root = doc.root();
		    recursiveDFS(root, listNode);
		    for(int i = 0; i < listNode.size(); i++) {
		    	String text = listNode.get(i).toString();
		    		if(text.length() > 1){
			    	NodeText.add(text);
		    		}

		    }
		    sequencesFilter(NodeText,thirdRequirementResult);
	}
	
	// DFT html tree, and get text from every child node.
	private static void recursiveDFS(Node node, ArrayList<Node> list) {
	    if (node instanceof TextNode) {
	        list.add(node);
	    }
	    for (Node child: node.childNodes()) {
	        recursiveDFS(child, list);
	    }
	}
	 
	// Filter everything in the NodeText, only add what we need in result.
	// result contains all the required text
	private static void sequencesFilter( ArrayList<String> NodeText, ArrayList<String> result){
		for(int i = 0; i < NodeText.size(); i++) {
			String local = NodeText.get(i);
			String[] localSA = local.split(" ");
			String cur = "";
			String curA = "";
			

			int counter = 0;
			int size = localSA.length;
			for(int j = 0; j < localSA.length; j++) {
				String text = localSA[j];
				
				if(text.length() >= 1 && text.charAt(0) != ' ') {
					char c = text.charAt(0);
					if(Character.isUpperCase(c)) {
						counter++;
						cur += text + " ";
					}  
					if(counter >= 2 ) {
						curA = cur;
					} 
					if (!Character.isUpperCase(c)){
						if (counter >= 2) {
							result.add(curA);
						}
						counter = 0;
						curA = "";
						cur = "";
					}
					
					if(size == counter || (j == size - 1 && counter >= 2 )) {
						if (curA.length() > 1) {
							result.add(curA);
						}

					}
				}
			}
		}		
	}
		
	// Reading the links
	// Using Jsoup to get all the elements that has attribute of href.
	// linksList is the output that contains all the links. 
	private static void linkFilter(String html, List<String> linksList) {		
        // get all links
		Document doc = Jsoup.parse(html);
        Elements links = doc.select("[href]");
        
        for (Element link : links) {      
        	 linksList.add(link.attr("href"));
        }	
        doc.getAllElements();
	}
	
	
	// Input: All html into a String. 
	// Jsuop won't be able to keep any illegal input
	// If the script is as complicated as Amazon web, this works perfect.	
	private static void tagFilter(String text, ArrayList<String> tagsList) {
	
      Pattern pattern = Pattern.compile("(<)(.*?)(>)", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(text);
      
      boolean scriptFlag = false;
      boolean styleFlag = false;
      int counter = 0;
      int styleCounter = 0;
      String preTag = "";
      
      // Using regular expression to get all the <>
      while(matcher.find()) {
    	 
      	String tag = matcher.group(2);
      	String[] tagS = tag.split(" ");
      	boolean scriptlocal = tag.contains("script");
      	boolean stylelocal = tag.contains("style");
     

      
      	 if ( (tag.length() < 1 || tag.toCharArray()[0] == ' ' ||
      			 tagS[0].contains("!--") || tag.toCharArray()[0] == '#' ) 
      			 &&(! (scriptlocal || stylelocal))) {
      		 	continue;
       	} 
      	int size = tagS[tagS.length - 1].length() - 1;
      	
    	// <link/>|| <link> || <meta/> || <meta> will be output as it is
    	 if(tag.toCharArray()[tag.length() - 1] == '/' && ((! (scriptFlag || styleFlag)))) {	
      		String tagString = "<" +  tagS[0] + "/>";
      		tagsList.add(tagString);
     	}

    	 
    	 // style case
       	else if (stylelocal  &&
       				(
       					((tagS[0] != null ) || tagS[0].substring(0, 5).equals("style"))
       					||
       					((tagS[tagS.length - 1] != null ) || tagS[tagS.length - 1].substring(size - 5, size).equals("style"))
       				)
       			) {
       		String tagString = "<" +  tagS[0] + ">";
       		styleCounter++;
       		
       		if(styleCounter % 2 == 1) {
       			styleFlag = true;
       			preTag = tagString;
       			tagsList.add(tagString);
       		} 

       		else if(styleCounter % 2 == 0) {
       			styleFlag = false;
       			tagsList.add(tagString);
       		}
       		
    	 }
      	
      	
    	// script: skip everything between a pair of script tag 
      	else if (scriptlocal && (tagS[0].contains("script") || tagS[tagS.length - 1].contains("script") )) {
      		String tagString = "";
      		if ((tagS[0].contains("script"))) {
      			tagString = "<" +  tagS[0] + ">";
      		} else if (tagS[tagS.length - 1].contains("script") ){
      			tagString = "</" +  "script" + ">";
      		} else {
      			tagString = "";
      		}
       		counter++;
       		
       		if(counter % 2 == 1) {
       			scriptFlag = true;
       			preTag = tagString;
       			tagsList.add(tagString);
       		} 

       		else if(counter % 2 == 0) {
       			scriptFlag = false;
       			tagsList.add(tagString);
       		}
    	 }

      	else if (scriptFlag || styleFlag) {
      		String curTag = tagS[0];
      		if ( curTag != preTag) {
      			continue;
      		}
      		
      	}
      	else { 
      		String tagString = "<" +  tagS[0] + ">";
      		tagsList.add(tagString);
      		}
      }
	}
	

	// get all the source code
	// It's necessary for the second requirement,
	// Since the second requirement is using regular expression not Jsoup. 
	// Raw source code is required.
	private static String url(String urlInput){

	    URL url;
	    InputStream is = null;
	    BufferedReader br;
	    String line = "";
	    String res = "";
	    
	    // Handle missing transport protocol
    	if (!urlInput.toLowerCase().matches("^\\w+://.*")) {
    		urlInput = "http://" + urlInput;
    	}

	    try {
	        url = new URL(urlInput);
	        is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));

	        while ((line = br.readLine()) != null) {
	            res += new StringBuilder().append(line + "\n");
	        }
	        
	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	         ioe.printStackTrace();
	    } finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	    }	
		return res;
	}
	


	// write in output.txt
	private static void writeFile( String fileName, List<String> linkslist, String title){
		BufferedWriter writer = null;
        try {
            File logFile = new File(fileName);

            // This will output the full path where the file will be written to...
            // System.out.println(logFile.getCanonicalPath());
            
            writer = new BufferedWriter(new FileWriter(logFile, true));
            
            // linkslist
            writer.write("[" + title + "]" +  "\n");
            for(int i = 0; i < linkslist.size(); i++) {
            	String strL1 = linkslist.get(i);
                if(strL1 != null) {
                	
               	 writer.write(strL1 + "\n");
               } else {
               	System.out.println("error");
               }
            }
            
            writer.write("\n");
           
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
            }
        }
	}
}
