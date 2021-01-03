package com.shikhar03stark.RaidArch.core;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RaidConfig {
	
	public enum RaidType {
		RAID0,
		RAID1,
		RAID4,
		RAID5
	}
	
	private String confFilePath;
	private String simulationPath;
	private RaidType archType;
	private int virtualDrives;
	private double virtualDiskMemory;
	
	//Getter and Setter for confFile
	public String getConfFilePath() {
		return this.confFilePath;
	}	
	
	public void setConfFilePath(String path) {
		this.confFilePath = path;
	}
	
	//Getter and Setter for simulationPath
	public String getSimulationPath() {
		return this.simulationPath;
	}
	
	public void setSimulationPath(String path) {
		this.simulationPath = path;
	}
	
	//Getter and Setter for archType
	public String getArchType() {
		return this.archType.toString();
	}
	
	public void setArchType(String arch) {
		for(RaidType r: RaidType.values()) {
			if(r.toString().equals(arch)) {
				this.archType = r;
				break;
			}
		}
		
	}
	
	//Getter and Setter for virtualDrives
	public int getVirtualDrives() {
		return this.virtualDrives;
	}
	
	public void setVirtualDrives(String strDrives) {
		int drives = Integer.parseInt(strDrives);
		if(drives <= 0) {
			this.virtualDrives = 1;
		}
		else {
			this.virtualDrives = drives;
		}
	}
	
	//Getter and Setter for virtualDiskMemory
	public double getVirtualDiskMemory() {
		return this.virtualDiskMemory;
	}
	
	public void setVirtualDiskMemory(String sizeMB) {
		double size = Double.parseDouble(sizeMB);
		if(size <= 0) {
			size = 100;
		}
		else {
			this.virtualDiskMemory = size;
		}
	}
	
	//constructor
	public RaidConfig(String confPath) {
		String xmlFilePath = confPath.strip();
		File xmlFile = new File(xmlFilePath);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();
			Element settings = doc.getDocumentElement();

			//set Parameters
			setConfFilePath(xmlFilePath);
			setSimulationPath(settings.getElementsByTagName("simulationDirectory").item(0).getChildNodes().item(0).getNodeValue());
			setArchType(settings.getElementsByTagName("raidArch").item(0).getChildNodes().item(0).getNodeValue());
			setVirtualDiskMemory(settings.getElementsByTagName("virtualDiskMemory").item(0).getChildNodes().item(0).getNodeValue());
			setVirtualDrives(settings.getElementsByTagName("virtualDrives").item(0).getChildNodes().item(0).getNodeValue());
			
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	

}
