package com.mrprez.gencross.impl.polarisPerso;

import com.mrprez.gencross.Property;
import com.mrprez.gencross.renderer.Renderer;
import com.mrprez.gencross.value.Value;

public class PolarisMaitriseRenderer extends Renderer {

	@Override
	public String displayValue(Value value) {
		if(value.getInt()==-4){
			return "X";
		}
		return super.displayValue(value);
	}

	@Override
	public String displayValue(Property property) {
		return displayValue(property.getValue());
	}
	
	
	

}
