package crac.models.input;

import java.util.HashMap;

import crac.models.db.daos.MaterialDAO;
import crac.models.db.entities.Material;
import lombok.Data;

/**
 * Helperclass that maps json-input to data that can be used to create a material
 * Also handles default values for the relationship
 * @author David Hondl
 *
 */
@Data
public class MaterialMapping {
	
	private long materialId;
	
	private long quantity;
	
	private String name;
	
	private String description;

	public MaterialMapping() {
		this.materialId = 0l;
		this.quantity = -200;
		this.name = "UNSET";
		this.description = "UNSET";
	}
	
	public Material mapToMaterial(HashMap<String, String> response, MaterialDAO materialDAO){
		Material m = new Material();
		
		if(this.materialId != 0){
			Material mf = materialDAO.findOne(this.materialId);
			if(mf != null){
				m = mf;
				response.put("material", "ALREADY_EXISTS_VALUES_ADJUSTED");
			}else{
				response.put("material_id", "ID_NOT_VALID");
				response.put("material", "CREATED");
			}
		}else{
			response.put("material", "CREATED");
		}
				
		if(this.quantity == -200){
			response.put("quantity", "NOT_ASSIGNED");
			response.put("quantity", "DEFAULT_VALUE_ASSIGNED");
			m.setQuantity(1);
		}else if(this.quantity < 1){
			response.put("quantity", "VALUE_NOT_VALID");
			response.put("quantity", "DEFAULT_VALUE_ASSIGNED");
			m.setQuantity(1);
		}else{
			m.setQuantity(this.quantity);
		}
		
		if(this.name.equals("UNSET")){
			response.put("name", "NOT_ASSIGNED");
			response.put("material", "NOT_CREATED");
			return null;
		}else{
			m.setName(this.name);
		}
		
		if(this.description.equals("UNSET")){
			response.put("description", "NOT_ASSIGNED");
			response.put("description", "DEFAULT_VALUE_ASSIGNED");
			m.setDescription("Keine Beschreibung vorhanden!");
		}else{
			m.setDescription(this.description);
		}
		
		return m;
	}

}
