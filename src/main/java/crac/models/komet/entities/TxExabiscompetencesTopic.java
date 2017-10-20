package crac.models.komet.entities;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.repository.CrudRepository;

import crac.exception.KometMappingException;
import crac.models.db.entities.CompetenceArea;
import crac.module.matching.interfaces.SyncableCrac;
import crac.module.matching.interfaces.SyncableKomet;

/**
 * The persistent class for the tx_exabiscompetences_topics database table.
 * @author David Hondl
 */
@Entity
@Table(name = "tx_exabiscompetences_topics")
public class TxExabiscompetencesTopic implements SyncableKomet {

	@Id
	private int uid;

	@Type(type = "text")
	private String ataxonomie;

	@Type(type = "text")
	private String btaxonomie;

	private int cat;

	private int crdate;

	@Column(name = "cruser_id")
	private int cruserId;

	@Type(type = "text")
	private String ctaxonomie;

	@Type(type = "text")
	private String description;

	@Type(type = "text")
	private String dtaxonomie;

	private int epop;

	@Type(type = "text")
	private String etaxonomie;

	@Column(name = "fe_creator")
	private int feCreator;

	@Column(name = "fe_group")
	private int feGroup;

	@Column(name = "fe_owner")
	private int feOwner;

	@Type(type = "text")
	private String ftaxonomie;

	@Type(type = "text")
	private String knowledgecheck;

	@Type(type = "text")
	@Column(name = "l10n_diffsource")
	private String l10nDiffsource;

	@Column(name = "l10n_parent")
	private int l10nParent;

	private String learnlist;

	private String numb;

	private int pid;

	private int sorting;

	@Type(type = "text")
	private String source;

	private int sourceid;

	private int subjid;

	@Column(name = "sys_language_uid")
	private int sysLanguageUid;

	@Column(columnDefinition = "TINYTEXT")
	private String title;

	private String titleshort;

	private int tstamp;

	public TxExabiscompetencesTopic() {
	}

	public CompetenceArea MapToCompetenceArea() {
		CompetenceArea area = new CompetenceArea();
		area.setId(this.getUid());
		//area.setDescription(this.getDescription().replaceAll("<"+".*"+">", ""));
		area.setDescription("");
		area.setName(this.getTitle().replaceAll("<"+".*"+">", ""));
		return area;
	}
	

	@Override
	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map) throws KometMappingException {
		CompetenceArea area = new CompetenceArea();
		area.setId(this.getUid());
		//area.setDescription(this.getDescription().replaceAll("<"+".*"+">", ""));
		area.setDescription("");
		area.setName(this.getTitle().replaceAll("<"+".*"+">", ""));
		return area;
	}
	
	public boolean isValid(){
		return !title.equals("");
	}

	public int getUid() {
		return this.uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getAtaxonomie() {
		return this.ataxonomie;
	}

	public void setAtaxonomie(String ataxonomie) {
		this.ataxonomie = ataxonomie;
	}

	public String getBtaxonomie() {
		return this.btaxonomie;
	}

	public void setBtaxonomie(String btaxonomie) {
		this.btaxonomie = btaxonomie;
	}

	public int getCat() {
		return this.cat;
	}

	public void setCat(int cat) {
		this.cat = cat;
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

	public String getCtaxonomie() {
		return this.ctaxonomie;
	}

	public void setCtaxonomie(String ctaxonomie) {
		this.ctaxonomie = ctaxonomie;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDtaxonomie() {
		return this.dtaxonomie;
	}

	public void setDtaxonomie(String dtaxonomie) {
		this.dtaxonomie = dtaxonomie;
	}

	public int getEpop() {
		return this.epop;
	}

	public void setEpop(int epop) {
		this.epop = epop;
	}

	public String getEtaxonomie() {
		return this.etaxonomie;
	}

	public void setEtaxonomie(String etaxonomie) {
		this.etaxonomie = etaxonomie;
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

	public String getFtaxonomie() {
		return this.ftaxonomie;
	}

	public void setFtaxonomie(String ftaxonomie) {
		this.ftaxonomie = ftaxonomie;
	}

	public String getKnowledgecheck() {
		return this.knowledgecheck;
	}

	public void setKnowledgecheck(String knowledgecheck) {
		this.knowledgecheck = knowledgecheck;
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

	public String getLearnlist() {
		return this.learnlist;
	}

	public void setLearnlist(String learnlist) {
		this.learnlist = learnlist;
	}

	public String getNumb() {
		return this.numb;
	}

	public void setNumb(String numb) {
		this.numb = numb;
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

	public String getTitleshort() {
		return this.titleshort;
	}

	public void setTitleshort(String titleshort) {
		this.titleshort = titleshort;
	}

	public int getTstamp() {
		return this.tstamp;
	}

	public void setTstamp(int tstamp) {
		this.tstamp = tstamp;
	}


}