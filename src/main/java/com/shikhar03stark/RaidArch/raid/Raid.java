package com.shikhar03stark.RaidArch.raid;


import com.shikhar03stark.RaidArch.core.RaidConfig;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;

public interface Raid {
	
	//generate parsed document of metafile
	public static Document getParsedMetaDocument(RaidConfig config){
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String metafileDir = config.getSimulationPath()+".metafile";
		File metafile = new File(metafileDir);
		Document doc = null;
		try {
			doc = dBuilder.parse(metafile);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	//Transform DOM to XML
	public static boolean transformToXML(Document doc, String xmlPath) {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DOMSource domSource = new DOMSource(doc);
		StreamResult sResult = new StreamResult(new File(xmlPath));
		
		try {
			transformer.transform(domSource, sResult);
			return true;
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	//check if cache is present
	public static boolean isCachePresent(RaidConfig config) {
		try {
			Document doc = getParsedMetaDocument(config);
			
			doc.getDocumentElement().normalize();
			Element cache = doc.getDocumentElement();
			
			if(cache.getElementsByTagName("cache").getLength() > 0) {
				return true;
			}
			
		}catch (Exception e) {
			System.err.println(e);
		}
		
		return false;
	}
	
	//check if fileSystem block is present
	public static boolean isFileSystemPresent(RaidConfig config) {
		try {
			Document doc = getParsedMetaDocument(config);
			doc.getDocumentElement().normalize();
			
			Element fileSystem = doc.getDocumentElement();
			
			if(fileSystem.getElementsByTagName("filesystem").getLength() > 0) {
				return true;
			}
		} catch (Exception e) {
			System.err.println(e);
		}
		
		return false;
	}
	
	//generate raid specific initial Cache
	public void genCache();
	
	//update cache when CREATE,DELETE files according to RAID
	public boolean updateCache(Object filedata);
	
	//generate FileSystem Object 
	public void genFileSystem();
	
	//update fileSystem when CREATE, DELETE files according to RAID
	public boolean updateFileSystem(Object filedata);
	
	//write data to files according to RAID
	public void writeToRaid(String path);
	
	//Delete file/data according to RAID
	public void removeFromRaid(String filename);
	
	//Read file/data according to RAID
	public void readFromRaid(String filename, String outputPath);
	
	//get Human friendly Cache information.
	public void getRaidInfo();
	
	//Determine if FileSystem is inconsistent and can be recovered
	public boolean isStateRecoverable();
	
	//RAID specific recovery strategy
	public void recoverFileSystem();
	
	
}
