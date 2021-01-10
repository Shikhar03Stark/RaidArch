package com.shikhar03stark.RaidArch.raid;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.shikhar03stark.RaidArch.core.RaidConfig;
import com.shikhar03stark.RaidArch.raid.Data.Pair;

class Data {
	
	static class Pair{
		public long first;
		public long second;
		public Pair(long buffer, long l) {
			this.first = buffer;
			this.second = l;
		}
		public Pair() {
			this.first = 0;
			this.second = 0;
		}
		
		@Override
		public String toString() {
			String res = "[" + Long.toString(first) + ", " + Long.toString(second) + ")";
			return res;
		}
	}
	
	enum Operation {
		READ,
		WRITE,
		DELETE
	}
	
	private Operation op;
	private boolean latest;
	private String originalPath;
	private String filename;
	private long fileSize;
	private int initialOffset; //next free drive for write ops
	private long chunks;
	private ArrayList<ArrayList<Pair>> chunkTable; //row = Drives, col = seek.start, seek.stop
	private double spaceRemaining;
	private int nextFreeDisk;
	
	public int getNextFreeDisk() {
		return this.nextFreeDisk;
	}
	
	public void setNextFreeDisk(int nextFreeDisk) {
		this.nextFreeDisk = nextFreeDisk;
	}
	
	public double getSpaceRemaining() {
		return this.spaceRemaining;
	}
	
	public void setSpaceRemaining(double spaceLeft) {
		this.spaceRemaining = spaceLeft;
	}
	
	public boolean isLatest() {
		return this.latest;
	}
	
	public void setLatest(boolean val) {
		this.latest = val;
	}
	
	public String getOriginalPath() {
		return this.originalPath;
	}
	
	public void setOriginalPath(String orgPath) {
		this.originalPath = orgPath;
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public int getInitialOffset() {
		return this.initialOffset;
	}
	
	public void setInitialOffset(int offset) {
		this.initialOffset = offset;
	}
	
	public long getChunks() {
		return this.chunks;
	}
	
	public void setChunks(long chunks) {
		this.chunks = chunks;
	}
	
	public ArrayList<Pair> getThreadChunks(int thread){
		ArrayList<Pair> driveChunks = (ArrayList<Pair>) this.chunkTable.get(thread).clone();
		return driveChunks;
	}
	
	public void createChunkTable(ArrayList<ArrayList<Pair>> chunkTable) {
		this.chunkTable =  (ArrayList<ArrayList<Pair>>) chunkTable.clone();
	}
	
	public String getOperation() {
		return op.toString();
	}
	
	public void setOperation(String operationName) {
		if(operationName.equals("READ")) {
			op = Operation.READ;
		}
		else if(operationName.equals("DELETE")) {
			op = Operation.DELETE;
		}
		else {
			op = Operation.WRITE;
		}
	}
	
	public long getFileSize() {
		return this.fileSize;
	}
	
	public void setFileSize(long filesize) {
		this.fileSize = filesize;
	}
	
	public Data() {
		this.latest = true;
	}
	
}

public class Raid0 implements Raid {
	
	private RaidConfig config;
	private int chunkSize;
	
	public Raid0(RaidConfig config){
		this.config = config;
		
		//chunk size set to 1KB
		this.chunkSize = 1000;
	}
	
	//Independent Thread Task
	private void threadWrite(Data filemeta, int drive, String readPath, ArrayList<Data.Pair> chunks) {
		/*Will create file with name `filename.chunk.x` in specified drive, where x is chunk index
		 * */
		if(chunks.size() == 0) {
			return;
		}
		int startOffset = filemeta.getInitialOffset();
		int step = config.getVirtualDrives();
		int chunkIndex = (drive - startOffset)%step;
		if(chunkIndex < 0) {
			chunkIndex = step - chunkIndex;
		}
		String drivePath = config.getSimulationPath() + "Drive" + Integer.toString(drive) + "/";
		String filename = filemeta.getFilename();
		
		try(FileInputStream data = new FileInputStream(readPath)){
			for(int i = 0; i<chunks.size(); i++) {
				//file set start and size;
				int start = (int) chunks.get(i).first;
				int size = (int) chunks.get(i).second - start;
				data.skip(start);
				byte[] buffer = new byte[size];
				data.read(buffer);
				
				String chunkFileName = drivePath + filename + ".chunk." + Integer.toString(chunkIndex);
				
				try {
					Path filePath = Paths.get(chunkFileName);
					Files.write(filePath, buffer);					
				} catch (IOException e) {
					System.err.println(e);
					return;
				}
				
				chunkIndex += step;
			}
			
			data.close();
			
		} catch (Exception e) {
			System.err.println(e);
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
			double totalSize = config.getVirtualDiskMemory() * config.getVirtualDrives() * 1000 * 1000; //BYTES
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
	public boolean updateCache(Object filedata) {
		try {
			Data data = (Data) filedata;
			Document doc = Raid.getParsedMetaDocument(config);
			
			Node remainingSize = doc.getElementsByTagName("remainingSize").item(0);
			Node chunkSize = doc.getElementsByTagName("chunkSize").item(0);
			Node nextFree = doc.getElementsByTagName("nextFreeDisk").item(0);
			
			remainingSize.setTextContent(Double.toString(data.getSpaceRemaining()));
			chunkSize.setTextContent(Integer.toString(this.chunkSize));
			nextFree.setTextContent(Integer.toString(data.getNextFreeDisk()));
			
			String xmlPath = config.getSimulationPath() + ".metafile";
			Raid.transformToXML(doc, xmlPath);
			
			return true;
			
		} catch (Exception e) {
			System.err.println(e);
		}
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
	public boolean updateFileSystem(Object filedata) {
		try {
			Data data = (Data) filedata;
			Document doc = Raid.getParsedMetaDocument(config);
			
			Node root = doc.getElementsByTagName("filesystem").item(0);
			Element file = doc.createElement("file");
			Element lastAccessed = doc.createElement("lastAccessed");
			Element filename = doc.createElement("filename");
			Element orignalPath = doc.createElement("originalPath");
			Element fileSize = doc.createElement("fileSize");
			Element offset = doc.createElement("offsetDrive");
			Element chunks = doc.createElement("chunks");
			
			lastAccessed.setTextContent(new Date().toString());
			filename.setTextContent(data.getFilename());
			orignalPath.setTextContent(data.getOriginalPath());
			fileSize.setTextContent(Long.toString(data.getFileSize()));
			offset.setTextContent(Integer.toString(data.getInitialOffset()));
			chunks.setTextContent(Long.toString(data.getChunks()));
			
			file.appendChild(filename);
			file.appendChild(orignalPath);
			file.appendChild(lastAccessed);
			file.appendChild(fileSize);
			file.appendChild(offset);
			file.appendChild(chunks);
			root.appendChild(file);
			
			String xmlPath = config.getSimulationPath() + ".metafile";
			
			Raid.transformToXML(doc, xmlPath);
			return true;
			
		} catch (Exception e) {
			System.err.println(e);
		}
		return false;
	}

	@Override
	public void writeToRaid(String path) {
		//get file
		Path filePath = Paths.get(path);
		
		//collect info
		long filesize = 0L;
		try {
			filesize = Files.size(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String filename = filePath.getFileName().toString();
		long chunks = (long) Math.ceil(filesize/(0.0+this.chunkSize));
		
		int offset = Integer.parseInt(Raid.getParsedMetaDocument(config).getElementsByTagName("nextFreeDisk").item(0).getTextContent());
		
		//check if space is available
		double spaceLeft = Double.parseDouble(Raid.getParsedMetaDocument(config).getElementsByTagName("remainingSize").item(0).getTextContent());
		spaceLeft *= 1000*1000;
		if(filesize > spaceLeft) {
			System.err.println("File size exceeds remaining Size");
			return;
		}
		else {
			spaceLeft -= filesize;
		}
		
		Data filedata = new Data();
		filedata.setLatest(true);
		filedata.setOperation("WRITE");
		filedata.setOriginalPath(path);
		filedata.setFilename(filename);
		filedata.setFileSize(filesize);
		filedata.setInitialOffset(offset);
		filedata.setChunks(chunks);
		filedata.setSpaceRemaining(spaceLeft);
		
		//Create ChunkTable
		//number of rows denote number of disk
		//0th row is offset drive and then following rows corresponds to drive in round robin fashion
		ArrayList<ArrayList<Data.Pair>> cTable = new ArrayList<ArrayList<Data.Pair>>();
		
		int rows = config.getVirtualDrives();
		for(int i = 0; i<rows; i++) {
			cTable.add(new ArrayList<Data.Pair>());
		}
		
		//add chunks in Column Major fashion
		boolean hasReachedEnd = false;
		long buffer = 0L;
		int cols = (int) Math.ceil(filesize/(0.0 + rows*this.chunkSize));
		int nextFreeDrive = 0;
		for(int i = 0; i<cols && !hasReachedEnd; i++) {
			for(int j = 0; j<rows && !hasReachedEnd; j++) {
				if(buffer+this.chunkSize<filesize) {					
					cTable.get(j).add(new Data.Pair(buffer, buffer + this.chunkSize));
					nextFreeDrive = i + 1;
				}
				else {
					cTable.get(j).add(new Data.Pair(buffer, filesize));
					hasReachedEnd = true;
				}
				buffer += this.chunkSize;
			}
		}
		nextFreeDrive = nextFreeDrive%config.getVirtualDrives();
		filedata.setNextFreeDisk(nextFreeDrive);
		filedata.createChunkTable(cTable);
		
		//Write concurrently
		ArrayList<Thread> writers = new ArrayList<>();
		//Prepare Threads
		for(int i = 0, drive = offset; i<config.getVirtualDrives(); i++, drive++) {
			drive = drive % config.getVirtualDrives();
			Thread t = new Thread(
					new Runnable() {
						Data filedata;
						int drive;
						ArrayList<Data.Pair> chunks;
						String readPath;
						public Runnable init(Data filedata, int drive, String readPath, ArrayList<Data.Pair> chunks) {
							this.filedata = filedata;
							this.drive = drive;
							this.chunks = chunks;
							this.readPath = readPath;
							return this;
						}
						
						@Override
						public void run() {
							threadWrite(this.filedata, this.drive, this.readPath, this.chunks);					
						}
			}.init(filedata, drive, path, filedata.getThreadChunks(i)));
			
			//add thread to arrayList
			writers.add(t);
		}
		
		//run threads
		for(int i = 0; i<writers.size(); i++) {
			writers.get(i).run();
		}
		
		//join threads finally
		for(int i = 0; i<writers.size(); i++) {
			try {
				writers.get(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Update MetaFile
		
		//Update Filesystem
		updateFileSystem(filedata);
		updateCache(filedata);
		
		filedata.setLatest(false);
		
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
