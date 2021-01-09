package com.shikhar03stark.RaidArch.raid;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.shikhar03stark.RaidArch.core.RaidConfig;

public class Raid0 implements Raid {
	
	private RaidConfig config;
	private int chunkSize;
	
	public Raid0(RaidConfig config){
		this.config = config;
		
		//chunk size
		//if drive size is less than 10MB chuck will be 10% of drive size
		//else 5MB chucks
		if(config.getVirtualDiskMemory() < 10) {
			this.chunkSize = (int) config.getVirtualDiskMemory()/10;
		}
		else {
			this.chunkSize = 5;
		}
	}

	@Override
	public void genCache() {
		if(!Raid.isCachePresent(config)) {
			Document doc = Raid.getParsedMetaDocument(config);
			Node root = doc.getElementsByTagName("metadata").item(0);
			Element cache = doc.createElement("cache");
			Element remainingSize = doc.createElement("remainingSize");
			Element chunkSize = doc.createElement("chunkSize");
			Element nextFreeDisk = doc.createElement("nextFreeDisk");
			Element filesystem = doc.createElement("filesystem");
			
			//RAID0 has total size of number of disk x size of each disk
			double totalSize = config.getVirtualDiskMemory() * config.getVirtualDrives();
			remainingSize.setTextContent(Double.toString(totalSize));
			chunkSize.setTextContent(Integer.toString(this.chunkSize));
			nextFreeDisk.setTextContent("0");
			
			cache.appendChild(remainingSize);
			cache.appendChild(chunkSize);
			cache.appendChild(nextFreeDisk);
			root.appendChild(cache);
			root.appendChild(filesystem);
			
			String xmlPath = config.getSimulationPath() + ".metafile";
			Raid.transformToXML(doc, xmlPath);
		}
		
	}

	@Override
	public boolean updateCache() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void genFileSystem() {
		// TODO Auto-generated method stub
		if(!Raid.isFileSystemPresent(config)) {
			Document doc = Raid.getParsedMetaDocument(config);
			Node root = doc.getElementsByTagName("metadata").item(0);
			
			Element filesystem = doc.createElement("filesystem");
			root.appendChild(filesystem);
			String xmlPath = config.getSimulationPath() + ".metafile";
			Raid.transformToXML(doc, xmlPath);
			
		}
	}

	@Override
	public boolean updateFileSystem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void writeToRaid(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFromRaid(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFromRaid(String filename, String outputPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getRaidInfo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStateRecoverable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void recoverFileSystem() {
		// TODO Auto-generated method stub
		
	}

}
