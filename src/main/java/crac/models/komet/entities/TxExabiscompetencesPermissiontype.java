package crac.models.komet.entities;

import javax.persistence.*;

import org.hibernate.annotations.Type;


/**
 * The persistent class for the tx_exabiscompetences_permissiontype database table.
 * 
 */
@Entity
@Table(name="tx_exabiscompetences_permissiontype")
public class TxExabiscompetencesPermissiontype {

	@Id
	private int uid;

	private int crdate;

	@Column(name="cruser_id")
	private int cruserId;

	@Type(type="text")
	private String description;

	@Column(name="fe_creator")
	private int feCreator;

	@Column(name="fe_owner")
	private int feOwner;

	@Type(type="text")
	@Column(name="l10n_diffsource")
	private String l10nDiffsource;

	@Column(name="l10n_parent")
	private int l10nParent;

	private int pid;

	private int selfassessment;

	private int sorting;

	@Column(name="sys_language_uid")
	private int sysLanguageUid;

	private String title;

	private int tstamp;

	public TxExabiscompetencesPermissiontype() {
	}

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
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

	public String getL10nDiffsource() {
		return this.l10nDiffsource;
	}

	public void setL10nDiffsource(String l10nDiffsource) {
		this.l10nDiffsource = l10nDiffsource;
	}

	public int getL10nParent() {
		return this.l10nParent;
	}

	public void setL10nParent(int l10nParent) {
		this.l10nParent = l10nParent;
	}

	public int getPid() {
		return this.pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getSelfassessment() {
		return this.selfassessment;
	}

	public void setSelfassessment(int selfassessment) {
		this.selfassessment = selfassessment;
	}

	public int getSorting() {
		return this.sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public int getSysLanguageUid() {
		return this.sysLanguageUid;
	}

	public void setSysLanguageUid(int sysLanguageUid) {
		this.sysLanguageUid = sysLanguageUid;
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