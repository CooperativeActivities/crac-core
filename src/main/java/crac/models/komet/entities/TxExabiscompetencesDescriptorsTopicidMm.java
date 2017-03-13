package crac.models.komet.entities;

import javax.persistence.*;


/**
 * The persistent class for the tx_exabiscompetences_descriptors_topicid_mm database table.
 * 
 */
@Entity
@Table(name="tx_exabiscompetences_descriptors_topicid_mm")
public class TxExabiscompetencesDescriptorsTopicidMm {

	private int sorting;

	private String tablenames;

	@Column(name="uid_foreign")
	private int uidForeign;

	@Id
	@Column(name="uid_local")
	private int uidLocal;

	public TxExabiscompetencesDescriptorsTopicidMm() {
	}

	public int getSorting() {
		return this.sorting;
	}

	public void setSorting(int sorting) {
		this.sorting = sorting;
	}

	public String getTablenames() {
		return this.tablenames;
	}

	public void setTablenames(String tablenames) {
		this.tablenames = tablenames;
	}

	public int getUidForeign() {
		return this.uidForeign;
	}

	public void setUidForeign(int uidForeign) {
		this.uidForeign = uidForeign;
	}

	public int getUidLocal() {
		return this.uidLocal;
	}

	public void setUidLocal(int uidLocal) {
		this.uidLocal = uidLocal;
	}

}