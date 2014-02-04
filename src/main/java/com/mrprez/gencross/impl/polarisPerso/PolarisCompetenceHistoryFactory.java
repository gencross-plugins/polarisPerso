package com.mrprez.gencross.impl.polarisPerso;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

import com.mrprez.gencross.history.HistoryFactory;
import com.mrprez.gencross.value.IntValue;
import com.mrprez.gencross.value.Value;

public class PolarisCompetenceHistoryFactory extends HistoryFactory {
	private IntValue startValue = new IntValue(0);
	private int factor = 1;
	
	public PolarisCompetenceHistoryFactory(Element element) {
		super(element);
	}
	
	public PolarisCompetenceHistoryFactory(String pointPool, IntValue startValue){
		super(pointPool);
		this.startValue = startValue;
	}
	
	public PolarisCompetenceHistoryFactory(String pointPool, IntValue startValue, int factor){
		super(pointPool);
		this.startValue = startValue;
		this.factor = factor;
	}

	@Override
	public Map<String, String> getArgs() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("startValue", startValue.toString());
		result.put("factor", ""+factor);
		return result;
	}

	@Override
	public int getCost(Value oldValue, Value newValue, int action) {
		int newValueInt;
		if(newValue==null){
			newValueInt = startValue.getInt();
		}else{
			newValueInt = newValue.getInt();
		}
		int oldValueInt;
		if(oldValue==null){
			oldValueInt = startValue.getInt();
		}else{
			oldValueInt = oldValue.getInt();
		}
		int cost = 0;
		int min = Math.min(newValueInt, oldValueInt);
		int max = Math.max(newValueInt, oldValueInt);
		for(int value=max;value>min; value--){
			if(value<=5){
				cost = cost+1;
			}else if(value<=10){
				cost = cost+2;
			}else{
				cost = cost+1+(value-10)*2;
			}
		}
		if(oldValueInt<newValueInt){
			return cost*factor;
		}else{
			return -cost*factor;
		}
	}

	@Override
	public void setArgs(Map<String, String> map) {
		if(map.containsKey("startValue")){
			startValue = new IntValue(Integer.parseInt(map.get("startValue")));
		}
		if(map.containsKey("factor")){
			factor = Integer.parseInt(map.get("factor"));
		}
	}

	public IntValue getStartValue() {
		return startValue;
	}
	public void setStartValue(IntValue startValue) {
		this.startValue = startValue;
	}
	public int getFactor() {
		return factor;
	}
	public void setFactor(int factor) {
		this.factor = factor;
	}

	@Override
	public HistoryFactory clone() {
		return new PolarisCompetenceHistoryFactory(pointPool, startValue, factor);
	}
	

}
