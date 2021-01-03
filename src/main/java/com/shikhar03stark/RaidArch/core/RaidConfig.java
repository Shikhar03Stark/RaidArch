package com.shikhar03stark.RaidArch.core;

import java.nio.file.*;

public class RaidConfig {
	
	public enum ArchType {
		RAID0,
		RAID1,
		RAID4,
		RAID5
	}
	
	private String confFilePath;
	private String simulationPath;
	private ArchType arch;

}
