package crac.models.input;

import java.util.Calendar;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Helperclass that maps json-input to data that can be used by different endpoints
 * @author David Hondl
 *
 */
@Data
public class PostOptions {

	private int id;
	private String name;
	private int importanceLevel;
	private int neededProficiencyLevel;
	private int proficiencyValue;
	private int importanceValue;
	private int likeValue;
	private boolean mandatory;
	private String text;
	private String firstName;
	private String lastName;
	private String elasticQuery;
	
	@Getter
	@Setter
	private Calendar date;

	public PostOptions() {
		text = "";
		name = "";
		firstName = "";
		lastName = "";
		elasticQuery = "";
	}

}
