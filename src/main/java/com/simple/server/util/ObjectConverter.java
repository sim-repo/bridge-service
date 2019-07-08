package com.simple.server.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.io.StringReader;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.simple.server.config.ContentType;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;



public class ObjectConverter {
	private ObjectConverter(){}
		
	public static String objectToJson(Object object){
		StringWriter writer = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.writeValue(writer, object);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return writer.toString();
	}
	
	public static Object jsonToObject(String json, Object object){
		ObjectMapper mapper = new ObjectMapper();
		final ObjectReader reader = mapper.reader();
		try {
			object = reader.forType(object.getClass()).readValue(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public static <T> List<T> jsonToObjects(String json, Class<T> clazz){
		ObjectMapper mapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		TypeFactory t = TypeFactory.defaultInstance();
				
		//final ObjectReader reader = mapper.reader();
		List<T> res = null;
		try {
			
			res = mapper.readValue(json,t.constructCollectionType(ArrayList.class,clazz));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
		
	public static String objectToXml(Object object, Boolean useDeclaration) throws Exception{
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, useDeclaration);
		String xml = xmlMapper.writer().withRootName("Message").writeValueAsString(object);		
		return xml;
}
	
	
	public static String jsonToXml(String json, Boolean useDeclaration) throws Exception{
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String,Object>>(){});
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, useDeclaration);		
		String xml = xmlMapper.writeValueAsString(map);		
		return xml;
	}
	
	public static String xmlToJson(String xml) throws Exception{
		JSONObject jObject = XML.toJSONObject(xml);
	    ObjectMapper mapper = new ObjectMapper();	   
	    Object json = mapper.readValue(jObject.toString(), Object.class);
	    String res = mapper.writeValueAsString(json);
		return res;
	}
	
	public static String listMapToJson(List<Map<String, Object>> list) throws Exception{       
	    JSONArray json_arr=new JSONArray();
	    for (Map<String, Object> map : list) {
	        JSONObject json_obj=new JSONObject();
	        for (Map.Entry<String, Object> entry : map.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            try {
	                json_obj.put(key,value);
	            } catch (JSONException e) {
	                e.printStackTrace();
	            }                           
	        }
	        json_arr.put(json_obj);
	    }
	    return json_arr.toString();
	}
	
	
	public static String listMapToJsonFirstObj(List<Map<String, Object>> list) throws Exception{       	   
		JSONObject json_obj=new JSONObject();
	    for (Map<String, Object> map : list) {
	        
	        for (Map.Entry<String, Object> entry : map.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            try {
	                json_obj.put(key,value);
	            } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }                           
	        }
	        return json_obj.toString();
	    }
	    return null;
	}
	
	public static boolean isValidJSON(final String json) throws IOException {
	    boolean valid = true;
	    ObjectMapper mapper = new ObjectMapper();
	    try{ 
	    	mapper.readTree(json);
	    } catch(JsonProcessingException e){
	        valid = false;
	    }
	    return valid;
	}
	
	public static boolean isValidXML(final String xml) throws Exception{
		 boolean valid = true;
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		 try {
			 Document doc = dBuilder.parse(xml);
		 }catch(Exception e) {
			 valid = false;
		 }
		 return valid;
	}
	
	
	private static String prepareJSON(String original, String fldSeparator) {
		if(fldSeparator != null)
			return original.replaceAll(fldSeparator, "\"");
		return original;
	}
	
	
	public static String bodyTransform(String original, ContentType contentType, String fldSeparator, Boolean removeXmlAttributes, Boolean useCharsetBase64, Boolean useDeclaration) throws Exception{
		
		String converted = "";
		String res = original;	
		boolean isJson = false;
		
		
		
		switch(contentType){		
		 	case XmlPlainText:
		 	case ApplicationXml: 
		 				 		
		 		if(fldSeparator != null && fldSeparator.isEmpty()  == false)
		 			original = prepareJSON(original, fldSeparator);
		 		isJson = ObjectConverter.isValidJSON(original);
		 		if(isJson){
		 			converted = ObjectConverter.jsonToXml(original,useDeclaration);
		 		}
		 		break;
		 	case JsonPlainText:
		 	case ApplicationJson:
		 				 			
		 		if(fldSeparator != null && fldSeparator.isEmpty() == false)
		 			original = prepareJSON(original, fldSeparator);
		 		
		 		isJson = ObjectConverter.isValidJSON(original);
		 		
		 		if(!isJson) {		 		
		 			
		 			if (original.contains("&lt;")) { 
		 				original = original.replaceAll("&lt;", "<");		 			
		 				original = original.replaceAll("&gt;", ">");		 		
		 			}			 			
			 					
			 		String initial = original;		 		
			 					 		
		 			if(removeXmlAttributes){		 				
		 					org.w3c.dom.Document document = null;		 																 					
							String xml = ObjectConverter.removeNameSpacesFromXmlString(original);
							document = ObjectConverter.convertXmlStringToDocument(xml);
							document = ObjectConverter.removeAllXmlAttributes(document);
							initial = ObjectConverter.convertDocumentToXmlString(document);							 					 						 				
		 			}	
					 	
			 	    converted = ObjectConverter.xmlToJson(initial);	
		 		}
	 			if (useCharsetBase64) {
	 				converted = Base64.getEncoder().encodeToString(converted.getBytes());
	 			}
		 			 
		 		break;
		}
		if (converted != null && converted != "" && !converted.equals("{}")) 
			res = converted;		
		return res;
	}
	

	
	public static Document convertXmlStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try  
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }
	
	public static String convertDocumentToXmlString(Document doc) {
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer;
	        try {
	            transformer = tf.newTransformer();
	            // below code to remove XML declaration
	            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	            StringWriter writer = new StringWriter();
	            transformer.transform(new DOMSource(doc), new StreamResult(writer));
	            String output = writer.getBuffer().toString();
	            return output;
	        } catch (TransformerException e) {
	            e.printStackTrace();
	        }
	        
	        return null;
	  }
	
	public static String removeNameSpacesFromXmlString(String xml) {
        try{
            String xslStr = String.join("\n",
                "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">",
                "<xsl:output version=\"1.0\" encoding=\"UTF-8\" indent=\"no\"/>",
                "<xsl:strip-space elements=\"*\"/>",                          
                "  <xsl:template match=\"@*|node()\">",
                "   <xsl:element name=\"{local-name()}\">",
                "     <xsl:apply-templates select=\"@*|node()\"/>",
                "  </xsl:element>",
                "  </xsl:template>",  
                "  <xsl:template match=\"text()\">",
                "    <xsl:copy/>",
                "  </xsl:template>",                                  
                "</xsl:transform>");

            // Parse XML and Build Document
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            Document doc = db.parse (is);                      

            // Parse XSLT and Configure Transformer
            Source xslt = new StreamSource(new StringReader(xslStr));
            Transformer tf = TransformerFactory.newInstance().newTransformer(xslt);

            // Output Result to String
            DOMSource source = new DOMSource(doc);
            StringWriter outWriter = new StringWriter();
            StreamResult strresult = new StreamResult( outWriter );        
            tf.transform(source, strresult);
            StringBuffer sb = outWriter.getBuffer(); 
            String finalstring = sb.toString();

            return(finalstring);

        } catch (Exception e) {
            System.out.println("Could not parse message as xml: " + e.getMessage());
        }
            return "";    
    }
	
	
	public static Document removeAllXmlAttributes(Document thisDoc) throws XPathExpressionException {
	    XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    XPathExpression expr = xpath.compile("//*[@*]");
	    NodeList result =(NodeList) expr.evaluate(thisDoc, XPathConstants.NODESET);
	    for (int i = 0; i < result.getLength(); i++) {
	        NamedNodeMap map = result.item(i).getAttributes();
	        for (int j = 0; j < map.getLength(); j++) {
	            map.removeNamedItem(map.item(j--).getNodeName());
	        }
	    }
	    return thisDoc;
	}
	
	
}
