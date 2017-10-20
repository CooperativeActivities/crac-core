package crac.models.komet.entities;

import javax.persistence.*;

import org.hibernate.annotations.Type;


/**
 * The persistent class for the tx_exabiscompetences_crosssubjects database table.
 * @author David Hondl
 */
@Entity
@Table(name="tx_exabiscompetences_crosssubjects")
public class TxExabiscompetencesCrosssubject{

	@Id
	private int uid;

	private int competence;

	private int crdate;

	@Column(name="cruser_id")
	private int cruserId;

	private byte deleted;

	@Type(type="text")
	private String description;

	@Column(name="fe_creator")
	private int feCreator;

	@Column(name="fe_owner")
	private int feOwner;

	private byte hidden;

	private int pid;

	private int sorting;

	@Type(type="text")
	private String source;

	private int sourceid;

	private int subjid;

	@Column(columnDefinition = "TINYTEXT")
	private String title;

	private int tstamp;

	public TxExabiscompetencesCrosssubject() {
	}

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getCompetence() {
		return this.competence;
	}

	public void setCompetence(int competence) {
		this.competence = competence;
	}

	public int getCrdate() {
		return this.crdate;
	}

	public void setCrdate(int crdate) {
		this.crdate = crdate;
	}

	public int getCruserId() {
		return this.cruserId;
	}

	public void setCruserId(int cruserId) {
		this.cruserId = cruserId;
	}

	public byte getDeleted() {
		return this.deleted;
	}

	public void setDeleted(byte deleted) {
		this.deleted = deleted;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getFeCreator() {
		return this.feCreator;
	}

	public void setFeCreator(int feCreator) {
		this.feCreator = feCreator;
	}

	public int getFeOwner() {
		return this.feOwner;
	}

	public void setFeOwner(int feOwner) {
		this.feOwner = feOwner;
	}

	public byte getHidden() {
		return this.hidden;
	}

	public void setHidden(byte hidden) {
		this.hidden = hidden;
	}

	public int getPid() {
		return this.pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getSorting() {
		return this.sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public String getSource() {
		return this.source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getSourceid() {
		return this.sourceid;
	}

	public void setSourceid(int sourceid) {
		this.sourceid = sourceid;
	}

	public int getSubjid() {
		return this.subjid;
	}

	public void setSubjid(int subjid) {
		this.subjid = subjid;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTstamp() {
		return this.tstamp;
	}

	public void setTstamp(int tstamp) {
		this.tstamp = tstamp;
	}

}