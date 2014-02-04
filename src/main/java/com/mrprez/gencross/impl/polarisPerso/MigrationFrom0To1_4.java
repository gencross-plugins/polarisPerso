package com.mrprez.gencross.impl.polarisPerso;

import com.mrprez.gencross.Property;
import com.mrprez.gencross.Version;
import com.mrprez.gencross.migration.DummyHistoryFactory;
import com.mrprez.gencross.migration.DummyRenderer;
import com.mrprez.gencross.migration.MigrationPersonnage;
import com.mrprez.gencross.migration.Migrator;

public class MigrationFrom0To1_4 implements Migrator {

	@Override
	public MigrationPersonnage migrate(MigrationPersonnage migrationPersonnage) throws Exception {
		migrationPersonnage.getPluginDescriptor().setVersion(new Version(1,4));
		
		for(Property property : migrationPersonnage.getProperties()){
			changePackageNameInProperty(property);
		}
		
		return migrationPersonnage;
	}
	
	private void changePackageNameInProperty(Property property){
		DummyRenderer renderer = (DummyRenderer)property.getRenderer();
		if("com.mrprez.gencross.impl.PolarisMaitriseRenderer".equals(renderer.getClassName())){
			renderer.setClassName("com.mrprez.gencross.impl.polarisPerso.PolarisMaitriseRenderer");
		}
		
		DummyHistoryFactory historyFactory = (DummyHistoryFactory) property.getActualHistoryFactory();
		if(historyFactory != null){
			if("com.mrprez.gencross.impl.PolarisCompetenceHistoryFactory".equals(historyFactory.getClassName())){
				historyFactory.setClassName("com.mrprez.gencross.impl.polarisPerso.PolarisCompetenceHistoryFactory");
			}
		}
		
		if(property.getSubProperties() != null){
			for(Property subProperty : property.getSubProperties()){
				changePackageNameInProperty(subProperty);
			}
			for(Property option : property.getSubProperties().getOptions().values()){
				changePackageNameInProperty(option);
			}
			if(property.getSubProperties().getDefaultProperty() != null){
				changePackageNameInProperty(property.getSubProperties().getDefaultProperty());
			}
		}
		
	}
	
	
	
}
