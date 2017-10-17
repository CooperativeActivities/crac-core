package crac.models.db.relation;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.Task;
import lombok.Data;

@Data
@Entity
@Table(name = "repetition_dates")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class RepetitionDate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "date_id")
	private long id;
	
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "repetitionDate", fetch = FetchType.LAZY)
	private Set<Task> mappedTo;
	
	public RepetitionDate() {
	}

	public RepetitionDate(int year, int month, int day, int hour, int minute) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
	}

}
