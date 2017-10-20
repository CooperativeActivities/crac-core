package crac.models.komet.entities;

import javax.persistence.*;


/**
 * The persistent class for the tx_exabiscompetences_crosssubjects_competence_mm database table.
 * @author David Hondl
 */
@Entity
@Table(name="tx_exabiscompetences_crosssubjects_competence_mm")
public class TxExabiscompetencesCrosssubjectsCompetenceMm{

	private int sorting;

	private String tablenames;

	@Column(name="uid_foreign")
	private int uidForeign;

	@Id
	@Column(name="uid_local")
	private int uidLocal;

	public TxExabiscompetencesCrosssubjectsCompetenceMm() {
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