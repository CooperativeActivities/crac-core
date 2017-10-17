package crac.models.komet.entities;

import java.util.HashSet;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.repository.CrudRepository;

import crac.exception.KometMappingException;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.module.matching.interfaces.SyncableCrac;
import crac.module.matching.interfaces.SyncableKomet;


/**
 * The persistent class for the tx_exabiscompetences_descriptors database table.
 * 
 */
@Entity
@Table(name="tx_exabiscompetences_descriptors")
public class TxExabiscompetencesDescriptor implements SyncableKomet{

	@Id
	private int uid;

	@Type(type="text")
	private String benefit;

	private int cat;

	private String comment;

	private int crdate;

	@Column(name="cruser_id")
	private int cruserId;

	private int epop;

	private int exampleid;

	private String exampletext;

	@Column(name="fe_creator")
	private int feCreator;

	@Column(name="fe_group")
	private int feGroup;

	@Column(name="fe_owner")
	private int feOwner;

	@Type(type="text")
	@Column(name="l10n_diffsource")
	private String l10nDiffsource;

	@Column(name="l10n_parent")
	private int l10nParent;

	private int niveauid;

	private String numb;

	private int parentid;

	@Column(name="permission_type")
	private int permissionType;

	private int pid;

	private int profoundness;

	@Type(type="text")
	private String requirement;

	private int skillid;

	private int sorting;

	@Type(type="text")
	private String source;

	private int sourceid;

	@Column(name="sys_language_uid")
	private int sysLanguageUid;

	private int taxid;

	@Type(type="text")
	private String title;

	@Type(type="text")
	@Column(name="title_employee")
	private String titleEmployee;

	@Type(type="text")
	@Column(name="title_students")
	private String titleStudents;

	private String titleshort;

	private int topicid;

	private int tstamp;

	public TxExabiscompetencesDescriptor() {
	}
	
	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map) throws KometMappingException{
		
		CompetenceAreaDAO competenceAreaDAO = (CompetenceAreaDAO) map.get(CompetenceAreaDAO.class);
		
		Competence c = new Competence();
		c.setId(this.uid);
		c.setName(this.titleshort);
		c.setDescription(this.title);
		
		CompetenceArea ca = competenceAreaDAO.findOne((long)this.topicid);
		if(ca != null){
			HashSet<CompetenceArea> areas = new HashSet<>();
			areas.add(ca);
			c.setCompetenceAreas(areas);
		}
		return c;
	}
	
	public boolean isValid(){
		return !title.equals("");
	}
	
	public Competence mapToCompetence(CompetenceAreaDAO caDAO){
		Competence c = new Competence();
		c.setId(this.uid);
		c.setName(this.titleshort);
		c.setDescription(this.title);
		CompetenceArea ca = caDAO.findOne((long)this.topicid);
		if(ca != null){
			HashSet<CompetenceArea> areas = new HashSet<>();
			areas.add(ca);
			c.setCompetenceAreas(areas);
		}
		return c;
	}

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getBenefit() {
		return this.benefit;
	}

	public void setBenefit(String benefit) {
		this.benefit = benefit;
	}

	public int getCat() {
		return this.cat;
	}

	public void setCat(int cat) {
		this.cat = cat;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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

	public int getEpop() {
		return this.epop;
	}

	public void setEpop(int epop) {
		this.epop = epop;
	}

	public int getExampleid() {
		return this.exampleid;
	}

	public void setExampleid(int exampleid) {
		this.exampleid = exampleid;
	}

	public String getExampletext() {
		return this.exampletext;
	}

	public void setExampletext(String exampletext) {
		this.exampletext = exampletext;
	}

	public int getFeCreator() {
		return this.feCreator;
	}

	public void setFeCreator(int feCreator) {
		this.feCreator = feCreator;
	}

	public int getFeGroup() {
		return this.feGroup;
	}

	public void setFeGroup(int feGroup) {
		this.feGroup = feGroup;
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

	public int getNiveauid() {
		return this.niveauid;
	}

	public void setNiveauid(int niveauid) {
		this.niveauid = niveauid;
	}

	public String getNumb() {
		return this.numb;
	}

	public void setNumb(String numb) {
		this.numb = numb;
	}

	public int getParentid() {
		return this.parentid;
	}

	public void setParentid(int parentid) {
		this.parentid = parentid;
	}

	public int getPermissionType() {
		return this.permissionType;
	}

	public void setPermissionType(int permissionType) {
		this.permissionType = permissionType;
	}

	public int getPid() {
		return this.pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getProfoundness() {
		return this.profoundness;
	}

	public void setProfoundness(int profoundness) {
		this.profoundness = profoundness;
	}

	public String getRequirement() {
		return this.requirement;
	}

	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}

	public int getSkillid() {
		return this.skillid;
	}

	public void setSkillid(int skillid) {
		this.skillid = skillid;
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

	public int getSysLanguageUid() {
		return this.sysLanguageUid;
	}

	public void setSysLanguageUid(int sysLanguageUid) {
		this.sysLanguageUid = sysLanguageUid;
	}

	public int getTaxid() {
		return this.taxid;
	}

	public void setTaxid(int taxid) {
		this.taxid = taxid;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleEmployee() {
		return this.titleEmployee;
	}

	public void setTitleEmployee(String titleEmployee) {
		this.titleEmployee = titleEmployee;
	}

	public String getTitleStudents() {
		return this.titleStudents;
	}

	public void setTitleStudents(String titleStudents) {
		this.titleStudents = titleStudents;
	}

	public String getTitleshort() {
		return this.titleshort;
	}

	public void setTitleshort(String titleshort) {
		this.titleshort = titleshort;
	}

	public int getTopicid() {
		return this.topicid;
	}

	public void setTopicid(int topicid) {
		this.topicid = topicid;
	}

	public int getTstamp() {
		return this.tstamp;
	}

	public void setTstamp(int tstamp) {
		this.tstamp = tstamp;
	}

}