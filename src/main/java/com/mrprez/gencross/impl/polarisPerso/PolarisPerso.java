package com.mrprez.gencross.impl.polarisPerso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.mrprez.gencross.Personnage;
import com.mrprez.gencross.PropertiesList;
import com.mrprez.gencross.Property;
import com.mrprez.gencross.formula.MalformedFormulaException;
import com.mrprez.gencross.history.ConstantHistoryFactory;
import com.mrprez.gencross.history.HistoryItem;
import com.mrprez.gencross.history.HistoryUtil;
import com.mrprez.gencross.value.DoubleValue;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.StringValue;
import com.mrprez.gencross.value.Value;

public class PolarisPerso extends Personnage {
	private Map<String, int[]> modifTypeGen;
	
	

	public PolarisPerso(){
		super();
		initModifTypeGen();
	}
	
	
	private void initModifTypeGen(){
		modifTypeGen = new HashMap<String, int[]>();
		modifTypeGen.put("humain", new int[] {0,0,0,0,0,0,0,0});
		modifTypeGen.put("hybride", new int[] {1,2,2,1,0,-2,0,0});
		modifTypeGen.put("techno-hybride", new int[] {2,3,0,-2,0,0,3,-6});
		modifTypeGen.put("techno-hybride fugitif", new int[] {2,3,0,-2,0,0,3,-6});
		modifTypeGen.put("geno-hybride", new int[] {1,1,2,0,0,0,0,-2});
	}
	
	@Override
	public void passToNextPhase() throws Exception {
		super.passToNextPhase();
		if(phase.equals("jeu")){
			getProperty("PC en Attribut").setEditable(false);
			getProperty("PC en compétences").setEditable(false);
			getProperty("Sexe").setEditable(false);
			getProperty("Type génétique").setEditable(false);
			// TODO Attributs
			getProperty("Mutations").setEditableRecursivly(false);
			getProperty("Mutations").getSubProperties().setFixe(true);
			// TODO Pouvoirs Polaris
			for(Property groupe : getProperty("Competence").getSubProperties()){
				for(Property competence : groupe.getSubProperties()){
					competence.getSubProperty("maitrise").setMin();
					competence.getSubProperty("maitrise").getHistoryFactory().setPointPool("XP");
				}
				for(Property competence : groupe.getSubProperties().getOptions().values()){
					competence.getSubProperty("maitrise").getHistoryFactory().setPointPool("XP");
				}
				groupe.getSubProperties().getDefaultProperty().getHistoryFactory().setPointPool("XP");
			}
			getProperty("Avantages").setEditableRecursivly(false);
			getProperty("Avantages").getSubProperties().setFixe(true);
			getProperty("Desavantages").setEditableRecursivly(false);
			getProperty("Desavantages").getSubProperties().setFixe(true);
		}
	}

	public Boolean changeAttributsPC(Property property, Value newValue){
		int newTotal = 38+((IntValue)newValue).getValue()*2;
		super.pointPools.get("Pts d'Attribut").add(newTotal-pointPools.get("Pts d'Attribut").getTotal());
		return true;
	}
	
	public Boolean changeCompetencePC(Property property, Value newValue){
		int newTotal = 20+((IntValue)newValue).getValue()*10;
		super.pointPools.get("Pts de Competence").add(newTotal-pointPools.get("Pts de Competence").getTotal());
		super.pointPools.get("Pts d'Avantages pro").add(((IntValue)newValue).getValue()*8-pointPools.get("Pts d'Avantages pro").getTotal());
		return true;
	}
	
	public Boolean changeAttribute(Property property, Value newValue) throws MalformedFormulaException{
		Property attribut = (Property) property.getOwner();
		int baseInt = ((IntValue)newValue).getValue();
		int modifGenInt = ((IntValue)attribut.getSubProperty("modif gen").getValue()).getValue();
		attribut.setValue(new IntValue(baseInt+modifGenInt));
		double aptNatDouble = ((double)(baseInt+modifGenInt-9))/2.0;
		attribut.getSubProperty("Aptitude Naturelle").setValue(new DoubleValue(aptNatDouble));
		recalculateAllComptences();
		formulaManager.impactModificationFor(attribut.getAbsoluteName(),this);
		return true;
	}
	
	public Boolean changeTypeGen(Property property, Value newValue){
		int[] oldModif = modifTypeGen.get(property.getValue().toString());
		int[] newModif = modifTypeGen.get(newValue.toString());
		Iterator<Property> it = getProperty("Attributs").iterator();
		int i=0;
		while(it.hasNext()){
			Property attribut = it.next();
			int modifGenInt = ((IntValue)attribut.getSubProperty("modif gen").getValue()).getValue();
			modifGenInt = modifGenInt - oldModif[i] + newModif[i];
			i++;
			attribut.getSubProperty("modif gen").setValue(new IntValue(modifGenInt));
			
			int baseInt = ((IntValue)attribut.getSubProperty("base").getValue()).getValue();
			attribut.setValue(new IntValue(baseInt+modifGenInt));
			double aptNatDouble = ((double)(baseInt+modifGenInt-9))/2.0;
			attribut.getSubProperty("Aptitude Naturelle").setValue(new StringValue(""+aptNatDouble));
		}
		recalculateAllComptences();
		return true;
	}
	
	public void changeCompetenceAtt(Property property, Value newValue){
		Property competence = (Property) property.getOwner();
		recalculateCompetence(competence);
	}
	
	public void changeCompetenceMaitrise(Property property, Value newValue){
		Property competence = (Property) property.getOwner();
		if(competence.getSubProperty("depart")!=null){
			competence.getSubProperty("depart").setEditable(false);
		}
		recalculateCompetence(competence);
	}
	
	public void changeCompetenceDepart(Property property, Value oldValue){
		Property competence = (Property) property.getOwner();
		competence.getSubProperty("maitrise").setValue(property.getValue().clone());
		competence.getSubProperty("maitrise").setMin();
		((PolarisCompetenceHistoryFactory)competence.getSubProperty("maitrise").getHistoryFactory()).setStartValue((IntValue) competence.getSubProperty("maitrise").getValue().clone());
		recalculateCompetence(competence);
	}
	
	public void addCompetence(Property competence){
		recalculateCompetence(competence);
		((PolarisCompetenceHistoryFactory)competence.getSubProperty("maitrise").getHistoryFactory()).setStartValue((IntValue) competence.getSubProperty("maitrise").getValue().clone());
	}
	
	public Boolean removeCompetence(Property competence) throws Exception{
		Property maitrise = competence.getSubProperty("maitrise");
		int cost = maitrise.getHistoryFactory().getCost(null, maitrise.getValue(), HistoryItem.DELETION);
		competence.setHistoryFactory(new ConstantHistoryFactory(maitrise.getHistoryFactory().getPointPool(), cost));
		
		return true;
	}
	
	public Boolean addPouvoirPolaris(Property property) throws Exception{
		Property owner = (Property) property.getOwner();
		PropertiesList propertiesList = owner.getSubProperties();
		
		if(propertiesList.size()==4){
			actionMessage = "Pas plus de 4 pouvoirs polaris";
			return false;
		}else if(propertiesList.size()>0){
			property.setHistoryFactory(new ConstantHistoryFactory("PC",1));
		}else if(propertiesList.size()==0){
			property.setHistoryFactory(new ConstantHistoryFactory("PC",5));
			addCompetence(getProperty("Competence#Compétences Spéciales"),"Maîtrise du flux Polaris","Volonté","Volonté",-4);
		}
		addCompetence(getProperty("Competence#Compétences Spéciales (Pouvoirs Polaris)"), property.getName(), "Intelligence", "Volonté", -4);
		return true;
	}
	
	public Boolean removePouvoirPolaris(Property property) throws Exception{
		Property owner = (Property) property.getOwner();
		PropertiesList propertiesList = owner.getSubProperties();
		
		if(propertiesList.size()>1){
			property.setHistoryFactory(new ConstantHistoryFactory("PC",1));
		}else if(propertiesList.size()==1){
			property.setHistoryFactory(new ConstantHistoryFactory("PC",5));
			this.removeAddedCompetence(getProperty("Competence#Compétences Spéciales#Maîtrise du flux Polaris"));
		}
		removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Pouvoirs Polaris)").getSubProperty(property.getName()));
		return true;
	}
	
	private void recalculateCompetence(Property competence){
		String att1 = competence.getSubProperty("att1").getValue().toString();
		String att2 = competence.getSubProperty("att2").getValue().toString();
		double att1AptNat = getProperty("Attributs").getSubProperty(att1).getSubProperty("Aptitude Naturelle").getValue().getDouble();
		double att2AptNat = getProperty("Attributs").getSubProperty(att2).getSubProperty("Aptitude Naturelle").getValue().getDouble();
		int base = (int) (att1AptNat + att2AptNat);
		competence.getSubProperty("base").setValue(new IntValue(base));
		if(competence.getSubProperty("maitrise").getValue().getInt()==-4){
			competence.setValue(new StringValue("X"));
		}else{
			int global = competence.getSubProperty("maitrise").getValue().getInt() + base;
			competence.setValue(new IntValue(global));
		}
	}
	
	private void recalculateAllComptences(){
		Iterator<Property> it = this.getProperty("Competence").iterator();
		while(it.hasNext()){
			Iterator<Property> it2 = it.next().iterator();
			while(it2.hasNext()){
				recalculateCompetence(it2.next());
			}
		}
	}
	

	@Override
	public void calculate() {
		errors.clear();
		if(pointPools.get("PC").getRemaining()>0){
			errors.add("Il reste des PC à dépenser");
		}
		if(pointPools.get("PC").getRemaining()<0){
			errors.add("Vous avez dépensé trop de PC");
		}
		if(pointPools.get("Pts d'Attribut").getRemaining()>0){
			errors.add("Il reste des Pts d'Attribut à dépenser");
		}
		if(pointPools.get("Pts d'Attribut").getRemaining()<0){
			errors.add("Vous avez dépensé trop de Pts d'Attribut");
		}
		if(pointPools.get("Pts de Competence").getRemaining()>0){
			errors.add("Il reste des Pts de Competence à dépenser");
		}
		if(pointPools.get("Pts de Competence").getRemaining()<0){
			errors.add("Vous avez dépensé trop de Pts de Competence");
		}
		calculatePCDistribution();
	}
	
	public void calculatePCDistribution(){
		if(getProperty("PC en Attribut").getValue().getInt()<3 || getProperty("PC en Attribut").getValue().getInt()>7){
			errors.add("Vous devez dépenser entre 3 et 7 PC en attributs");
		}
		if(HistoryUtil.sumHistoryOfSubTree(history, getProperty("Mutations"), "PC")+HistoryUtil.sumHistoryOfSubTree(history, getProperty("Pouvoirs Polaris"), "PC")>7){
			errors.add("Vous ne pouvez dépenser plus de 7 PC en mutations et pouvoirs polaris");
		}
		if(getProperty("PC en compétences").getValue().getInt()<5 || getProperty("PC en compétences").getValue().getInt()>17){
			errors.add("Vous devez dépenser entre 5 et 17 PC en compétences");
		}
		if(HistoryUtil.sumHistoryOfSubTree(history, getProperty("Avantages"), "PC")>10){
			errors.add("Vous ne pouvez dépenser plus de 10 PC en avantages");
		}
		if(HistoryUtil.sumHistoryOfSubTree(history, getProperty("Desavantages"), "PC")<-5){
			errors.add("Vous ne pouvez avoir plus de 5 PC en desavantages");
		}
	}
	
	public Boolean checkMutation(Property mutation) {
		if(mutation.getName().equals("Parasite(s) 1PC") || mutation.getName().equals("Symbiote(s) 3PC") || mutation.getName().equals("Régénération 2PC")){
			int compte = 1;
			if(getProperty("Mutations#Parasite(s) 1PC")!=null){
				compte++;
			}
			if(getProperty("Mutations#Symbiote(s) 3PC")!=null){
				compte++;
			}
			if(getProperty("Mutations#Régénération 2PC")!=null){
				compte++;
			}
			if(compte>2){
				actionMessage = "Un personnage ne peut pas cumuler plus de deux mutations appartenant à cette liste : Parasite,Symbiote et Régénération.";
				return false;
			}
		}
		return true;
	}

	public void addMutation(Property mutation) throws Exception{
		if(mutation.getName().equals("Adaptation extérieur 3PC")){
			addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Adaptation extérieur","Constitution","Constitution",-3);
		}else if(mutation.getName().contains("Amphibie")){
			addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Hybride","Coordination","Constitution",-3).getSubProperty("maitrise").setMax(new IntValue(0));
		}else if(mutation.getName().contains("Androgyne")){
			getProperty("Sexe").setValue(new StringValue("Androgyne"));
			getProperty("Sexe").setEditable(false);
		}else if(mutation.getName().contains("Asexué")){
			getProperty("Sexe").setValue(new StringValue("Asexué"));
			getProperty("Sexe").setEditable(false);
		}else if(mutation.getName().contains("Caractère félin")){
			addModifGen("Coordination",2);
			getProperty("Mutations").getSubProperties().getOptions().get("Griffes 2PC").setHistoryFactory(new ConstantHistoryFactory("PC", 1));
			getProperty("Mutations").getSubProperties().getOptions().get("Vision nocturne 3PC").setHistoryFactory(new ConstantHistoryFactory("PC", 2));
		}else if(mutation.getName().contains("Caractère canin")){
			addModifGen("Constitution",1);
			getProperty("Mutations").getSubProperties().getOptions().get("Crocs 1PC").setHistoryFactory(new ConstantHistoryFactory("PC", 0));
		}else if(mutation.getName().equals("Caractère reptilien 2PC")){
			addModifGen("Coordination",1);
		}else if(mutation.getName().equals("Caractère simiesque 2PC")){
			addModifGen("Force",1);
			addModifGen("Coordination",1);
			getProperty("Mutations").getSubProperties().getOptions().get("Queue 1PC").setHistoryFactory(new ConstantHistoryFactory("PC", 0));
		}else if(mutation.getName().equals("Contagion 3PC")){
			Property contagionComp = addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Contagion","Constitution","Volonté",-4);
			((PolarisCompetenceHistoryFactory)contagionComp.getSubProperty("maitrise").getHistoryFactory()).setFactor(2);
		}else if(mutation.getName().equals("Difformités légères -1PC")){
			addModifGen("Présence",-1);
		}else if(mutation.getName().equals("Difformités importantes -3PC")){
			addModifGen("Présence",-2);
		}else if(mutation.getName().equals("Empathie 4PC")){
			Property empathieComp = addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Empathie","Volonté","Présence",-3);
			((PolarisCompetenceHistoryFactory)empathieComp.getSubProperty("maitrise").getHistoryFactory()).setFactor(2);
		}else if(mutation.getName().equals("Instabilité moléculaire 4PC")){
			Property controleMolComp = addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Contrôle moléculaire","Constitution","Volonté",-4);
			((PolarisCompetenceHistoryFactory)controleMolComp.getSubProperty("maitrise").getHistoryFactory()).setFactor(2);
		}else if(mutation.getName().equals("Métamorphose 4PC")){
			Property metamorphComp = addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Métamorphose","Constitution","Volonté",-3);
			((PolarisCompetenceHistoryFactory)metamorphComp.getSubProperty("maitrise").getHistoryFactory()).setFactor(2);
		}else if(mutation.getName().equals("Purulence -2PC")){
			addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Contagion","Constitution","Volonté",-4);
			addModifGen("Présence",-2);
		}else if(mutation.getName().equals("Queue 1PC")){
			addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Agilité caudale", "Coordination", "Coordination",0);
		}else if(mutation.getName().equals("Radiation 3PC")){
			Property radComp = addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Radiation","Constitution","Volonté",-3);
			((PolarisCompetenceHistoryFactory)radComp.getSubProperty("maitrise").getHistoryFactory()).setFactor(2);
		}else if(mutation.getName().equals("Sonar 3PC")){
			addCompetence(getProperty("Competence#Compétences Spéciales (Mutations)"),"Sonar","Perception","Perception",-4);
		}
			
		
	}
	
	public void addAvantage(Property avantage){
		if(avantage.getName().equals("Formation 1PC")){
			pointPools.get("Pts de Competence").add(7);
		}
	}
	
	public void removeMutation(Property mutation) throws Exception{
		if(mutation.getName().equals("Adaptation extérieur 3PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Adaptation extérieur"));
		}else if(mutation.getName().contains("Amphibie")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Hybride"));
		}else if(mutation.getName().contains("Androgyne")){
			getProperty("Sexe").setValue(new StringValue("Homme"));
			getProperty("Sexe").setEditable(true);
		}else if(mutation.getName().contains("Asexué")){
			getProperty("Sexe").setValue(new StringValue("Homme"));
			getProperty("Sexe").setEditable(true);
		}else if(mutation.getName().contains("Caractère félin")){
			addModifGen("Coordination",-3);
			getProperty("Mutations").getSubProperties().getOptions().get("Griffes 2PC").setHistoryFactory(new ConstantHistoryFactory("PC", 2));
			getProperty("Mutations").getSubProperties().getOptions().get("Vision nocturne 3PC").setHistoryFactory(new ConstantHistoryFactory("PC", 3));
		}else if(mutation.getName().contains("Caractère canin")){
			addModifGen("Constitution",-1);
			getProperty("Mutations").getSubProperties().getOptions().get("Crocs 1PC").setHistoryFactory(new ConstantHistoryFactory("PC", 1));
		}else if(mutation.getName().equals("Caractère reptilien 2PC")){
			addModifGen("Coordination",-1);
		}else if(mutation.getName().equals("Caractère simiesque 2PC")){
			addModifGen("Force",-1);
			addModifGen("Coordination",-1);
			getProperty("Mutations").getSubProperties().getOptions().get("Queue 1PC").setHistoryFactory(new ConstantHistoryFactory("PC", 1));
		}else if(mutation.getName().equals("Contagion 3PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Contagion"));
		}else if(mutation.getName().equals("Difformités légères -1PC")){
			addModifGen("Présence",1);
		}else if(mutation.getName().equals("Difformités importantes -3PC")){
			addModifGen("Présence",2);
		}else if(mutation.getName().equals("Empathie 4PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Empathie"));
		}else if(mutation.getName().equals("Instabilité moléculaire 4PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Contrôle moléculaire"));
		}else if(mutation.getName().equals("Métamorphose 4PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Métamorphose"));
		}else if(mutation.getName().equals("Purulence -2PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Contagion"));
			addModifGen("Présence",2);
		}else if(mutation.getName().equals("Queue 1PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Agilité caudale"));
		}else if(mutation.getName().equals("Radiation 3PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Radiation"));
		}else if(mutation.getName().equals("Sonar 3PC")){
			removeAddedCompetence(getProperty("Competence#Compétences Spéciales (Mutations)#Sonar"));
		}
	}
	
	public Boolean checkRemoveMutation(Property mutation){
		if(mutation.getName().contains("Caractère félin")){
			if(getProperty("Mutations#Griffes 2PC")!=null){
				actionMessage = "Vous devez d'abord supprimer la mutation Griffes";
				return false;
			}
			if(getProperty("Mutations#Vision nocturne 3PC")!=null){
				actionMessage = "Vous devez d'abord supprimer la mutation Vision nocturne";
				return false;
			}
		}else if(mutation.getName().contains("Caractère canin")){
			if(getProperty("Mutations#Crocs 1PC")!=null){
				actionMessage = "Vous devez d'abord supprimer la mutation Crocs";
				return false;
			}
		}else if(mutation.getName().equals("Caractère simiesque 2PC")){
			if(getProperty("Mutations#Queue 1PC")!=null){
				actionMessage = "Vous devez d'abord supprimer la mutation Queue";
				return false;
			}
		}
		return true;
	}
	
	private Property addCompetence(Property owner, String name, String att1, String att2, int maitrise) throws Exception{
		Property newComp = owner.getSubProperties().getDefaultProperty().clone();
		newComp.setName(name);
		newComp.getSubProperty("att1").setEditable(false);
		newComp.getSubProperty("att2").setEditable(false);
		newComp.getSubProperties().remove(newComp.getSubProperty("depart"));
		newComp.getSubProperty("att1").setValue(new StringValue(att1));
		newComp.getSubProperty("att2").setValue(new StringValue(att2));
		newComp.getSubProperty("maitrise").setValue(new IntValue(maitrise));
		newComp.getSubProperty("maitrise").setMin();
		newComp.setRemovable(false);
		addPropertyToMotherProperty(newComp);
		return newComp;
	}
	
	private void removeAddedCompetence(Property competence) throws Exception{
		Boolean canRemoveElement = ((Property)competence.getOwner()).getSubProperties().getCanRemoveElement();
		competence.setRemovable(true);
		((Property)competence.getOwner()).getSubProperties().setCanRemoveElement(Boolean.TRUE);
		removePropertyFromMotherProperty(competence);
		((Property)competence.getOwner()).getSubProperties().setCanRemoveElement(canRemoveElement);
	}
	
	private void addModifGen(String attName, int modif) throws MalformedFormulaException{
		Property modifGen = getProperty("Attributs#"+attName+"#modif gen");
		modifGen.setValue(new IntValue(modifGen.getValue().getInt()+modif));
		changeAttribute(getProperty("Attributs#"+attName+"#base"), getProperty("Attributs#"+attName+"#base").getValue());
	}
	
	
	
	
	
	

}
