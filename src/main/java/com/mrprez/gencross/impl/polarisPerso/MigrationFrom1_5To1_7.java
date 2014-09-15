package com.mrprez.gencross.impl.polarisPerso;

import com.mrprez.gencross.Version;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;

public class MigrationFrom1_5To1_7 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		migrationPersonnage.getPluginDescriptor().setVersion(new Version(1,7));
		return migrationPersonnage;
	}
	
	
	
}
