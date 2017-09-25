package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.List;

import crac.exception.WrongParameterException;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.utility.ParamterDummy;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class GeoFilter extends ConcreteFilter {

	public GeoFilter() {
		super("Geo-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		try {
			double geoLat = (double) super.getPf().getParam("geoLat");
			double geoLng = (double) super.getPf().getParam("geoLng");
			String geoName = (String) super.getPf().getParam("geoName");
			String geoCountry = (String) super.getPf().getParam("geoCountry");
			String geoCountryA = (String) super.getPf().getParam("geoCountryA");
			String geoMacroRegion = (String) super.getPf().getParam("geoMacroRegion");
			String geoRegion = (String) super.getPf().getParam("geoRegion");
			String geoLocality = (String) super.getPf().getParam("geoLocality");

			List<Task> l = fp.getTasksPool();

			l.removeIf(x -> (geoLat != -1) && (x.getGeoLat() != geoLat) || (geoLng != -1) && (x.getGeoLng() != geoLng)
					|| (!geoName.equals("")) && (!x.getGeoName().equals(geoName))
					|| (!geoCountry.equals("")) && (!x.getGeoCountry().equals(geoCountry))
					|| (!geoCountryA.equals("")) && (!x.getGeoCountryA().equals(geoCountryA))
					|| (!geoMacroRegion.equals("")) && (!x.getGeoMacroRegion().equals(geoMacroRegion))
					|| (!geoRegion.equals("")) && (!x.getGeoRegion().equals(geoRegion))
					|| (!geoLocality.equals("")) && (!x.getGeoLocality().equals(geoLocality)));
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
			throw new WrongParameterException();
		}

	}
}