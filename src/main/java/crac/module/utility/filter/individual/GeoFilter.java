package crac.module.utility.filter.individual;

import java.util.List;

import crac.exception.InvalidParameterException;
import crac.models.db.entities.Task;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * The Geo-Filter requires the fields geoLat, geoLng as double and geoName,
 * geoCountry, geoCountryA, geoMacroRegion, geoRegion, geoLocality as String.
 * All tasks are filtered for matching fields, as long as they are not -1 (if it
 * is a double) or "" (if it is a string)
 * 
 * @author David Hondl
 *
 */
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

			boolean andBind = true;

			try {
				andBind = (boolean) super.getPf().getParam("logicAnd");
			} catch (Exception ex) {
				System.out.println("AndBind not possible");
			}

			List<Task> l = fp.getTasksPool();

			if (!andBind) {
				l.removeIf(x -> (geoLat != -1) && (x.getGeoLat() != geoLat) && (geoLng != -1)
						&& (x.getGeoLng() != geoLng) && (!geoName.equals(""))
						&& (!x.getGeoName().equalsIgnoreCase(geoName)) && (!geoCountry.equals(""))
						&& (!x.getGeoCountry().equalsIgnoreCase(geoCountry)) && (!geoCountryA.equals(""))
						&& (!x.getGeoCountryA().equalsIgnoreCase(geoCountryA)) && (!geoMacroRegion.equals(""))
						&& (!x.getGeoMacroRegion().equalsIgnoreCase(geoMacroRegion)) && (!geoRegion.equals(""))
						&& (!x.getGeoRegion().equalsIgnoreCase(geoRegion)) && (!geoLocality.equals(""))
						&& (!x.getGeoLocality().equalsIgnoreCase(geoLocality)));
			} else {
				l.removeIf(x -> (geoLat != -1) && (x.getGeoLat() != geoLat)
						|| (geoLng != -1) && (x.getGeoLng() != geoLng)
						|| (!geoName.equals("")) && (!x.getGeoName().equalsIgnoreCase(geoName))
						|| (!geoCountry.equals("")) && (!x.getGeoCountry().equalsIgnoreCase(geoCountry))
						|| (!geoCountryA.equals("")) && (!x.getGeoCountryA().equalsIgnoreCase(geoCountryA))
						|| (!geoMacroRegion.equals("")) && (!x.getGeoMacroRegion().equalsIgnoreCase(geoMacroRegion))
						|| (!geoRegion.equals("")) && (!x.getGeoRegion().equalsIgnoreCase(geoRegion))
						|| (!geoLocality.equals("")) && (!x.getGeoLocality().equalsIgnoreCase(geoLocality)));
			}
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
			throw new InvalidParameterException();
		}

	}
}