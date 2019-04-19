package receipt.service;

import receipt.entity.Drug;

import java.util.HashMap;
import java.util.Map;

public class DrugService {

	private static volatile DrugService instance = null;
	
	private final Map<String, Drug> drugs;
	
	private DrugService() {
		drugs = new HashMap<>();
		drugs.put("hydrocodone", new Drug("Hydrocodone", 100));
		drugs.put("generic zocor", new Drug("Generic Zocor", 10));
		drugs.put("lisinopril", new Drug("Lisinopril", 12));
		drugs.put("generic synthroid", new Drug("Generic Synthroid", 150));
		drugs.put("generic norvasc", new Drug("Generic Norvasc", 90));
		drugs.put("azithromycin", new Drug("Azithromycin", 300));
	}

	public static DrugService getInstance() {
        if (instance == null) {
            synchronized(DrugService.class) {
                if (instance == null) {
                    instance = new DrugService();
                }
            }
        }

        return instance;
    }
	
	public boolean nameExists(String drugName) {
		return drugs.containsKey(nameToKey(drugName));
	}

	public Drug findByName(String drugName) {
		return drugs.get(nameToKey(drugName));
	}
	
	private String nameToKey(String drugName) {
		return drugName.trim().toLowerCase();
	}
		
}
