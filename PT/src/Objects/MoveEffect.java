package Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Enums.EffectType;
import Enums.MoveImpact;

public class MoveEffect{
	
	private final EffectType type;
	private final MoveImpact impact;
	private final Map<String,String> params;
	
	public MoveEffect(EffectType type, MoveImpact impact, Map<String, String> params) {
		super();
		this.type = type;
		this.impact=impact;
		this.params = params;
	}

	public String getParam(String key) {
		return params.get(key);
	}

	public EffectType getType() {
		return type;
	}



	public MoveImpact getImpact() {
		return impact;
	}

	public Map<String, String> getParams() {
		return params;
	}
	
	public static List<MoveEffect> parse(String input) {
		List<MoveEffect> effects=new ArrayList<MoveEffect>();
		String[] spliteffects=input.split(";");
		for(String spliteffect:spliteffects){
			String[] components=spliteffect.split(",");
			try{
				EffectType type=EffectType.valueOf(components[0].substring(7));
				MoveImpact impact=MoveImpact.valueOf(components[1].substring(7));
				Map<String,String> params=null;
				if(components.length>2){
					params=new HashMap<String,String>();
					for(int i=2;i<components.length;i++){
						String[] component=components[i].split("=");
						params.put(component[0],component[1]);
					}
				}
				
				effects.add(new MoveEffect(type,impact,params));
			}catch(Exception e){System.out.println("Error parsing move effect from "+spliteffect);};
		}
		return effects;
	}
}
