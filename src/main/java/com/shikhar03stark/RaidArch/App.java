package com.shikhar03stark.RaidArch;

import com.shikhar03stark.RaidArch.core.Common;
import com.shikhar03stark.RaidArch.core.RaidConfig;
import com.shikhar03stark.RaidArch.raid.Raid0;

public class App 
{
    public static void main( String[] args )
    {
        RaidConfig config = new RaidConfig("/home/shikhar/dev/Java/Projects/RaidArch/raid-arch.conf");
        Common.setupSimDir(config);
        Raid0 raidInstance = new Raid0(config);
        raidInstance.genCache();
    }
}
