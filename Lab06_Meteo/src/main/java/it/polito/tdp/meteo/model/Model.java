package it.polito.tdp.meteo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.meteo.DAO.MeteoDAO;

public class Model {
	
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	private List<Rilevamento> bestSoluzione = new LinkedList<>();
	private int bestCosto = 0;
	
	public Model() {
		
	}

	// of course you can change the String output with what you think works best
	public String getUmiditaMedia(int mese) {
		String result = "";
		
		MeteoDAO dao = new MeteoDAO();
		List<Rilevamento> rilevamenti = (dao.getAllRilevamentiLocalitaMese(mese, "Torino"));
		rilevamenti.addAll( dao.getAllRilevamentiLocalitaMese(mese, "Milano") );
		rilevamenti.addAll( dao.getAllRilevamentiLocalitaMese(mese, "Genova") );
		
		Integer mediaTO = 0 , mediaMI = 0 , mediaGE = 0; // somma delle umidità, in realtà.
		ArrayList<Integer> giorni = new ArrayList<>( Arrays.asList(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31) ); // giorni di ogni mese
		
		for (Rilevamento r : rilevamenti) {
			if (r.getLocalita().equals("Torino")) {
				mediaTO += r.getUmidita();
			} else if (r.getLocalita().equals("Milano")) {
				mediaMI += r.getUmidita();
			} else if (r.getLocalita().equals("Genova")) {
				mediaGE += r.getUmidita();
			}
		}
		
		result += "Torino: " + (mediaTO / giorni.get(mese-1)) + "\nMilano: " +  (mediaMI/giorni.get(mese-1)) + "\nGenova: " + (mediaGE/giorni.get(mese-1)) +"\n";
		return result;
	}
	
	// of course you can change the String output with what you think works best
	public String trovaSequenza(int mese) {
		MeteoDAO dao = new MeteoDAO();
		List<Rilevamento> TO = dao.getAllRilevamentiLocalitaMese(mese, "Torino");
		List<Rilevamento> MI = dao.getAllRilevamentiLocalitaMese(mese, "Milano"); // prendo le rilevazioni del mese specificato per ogni città
		List<Rilevamento> GE = dao.getAllRilevamentiLocalitaMese(mese, "Genova");
		
		List< List<Rilevamento>> possibili = new LinkedList<> ( Arrays.asList( TO.subList(0, 14), MI.subList(0, 14), GE.subList(0, 14) ));
		 // aggiungo le prime 15 di ogni mese alla lista delle possibli date
	
		List<Rilevamento> parziale = new LinkedList<>();
		cerca(parziale, 0, possibili); // avvio la ricorsione
		
		String result = "";
		result += bestSoluzione.toString() + "\nCosto totale: " + bestCosto;
		
		return result;
	}
	
	private void cerca (List<Rilevamento> parziale, int L, List< List<Rilevamento> > rimanenti) {
		
		if (L == NUMERO_GIORNI_TOTALI) {
			int giorniTO = 0, giorniMI = 0, giorniGE = 0;
			for ( Rilevamento r : parziale ) {
				if (r.getLocalita().equals("Torino")) giorniTO++;
				if (r.getLocalita().equals("Milano")) giorniMI++;
				if (r.getLocalita().equals("Genova")) giorniGE++;
			}
			if ((giorniTO <= NUMERO_GIORNI_CITTA_MAX  && giorniMI <= NUMERO_GIORNI_CITTA_MAX  && giorniGE  <= NUMERO_GIORNI_CITTA_MAX )) {
				
	
				int costo = calcolaCosto(parziale);
				if (costo < bestCosto) {
					bestCosto = costo;
					bestSoluzione = new LinkedList<>(parziale);
				}
			}
		}
		
		if (L > 15) {
			return; // parziali da non esplorare
		}
		
		for (int i = 0; i < 3; i++) {
			for (int k = 0; k < rimanenti.get(i).size(); k ++) {
				LinkedList<Rilevamento> temp = new LinkedList<>( Arrays.asList( rimanenti.get(i).get(k), rimanenti.get(i).get(k+1), rimanenti.get(i).get(k+2)) ); // get 3 elemtni consecutivi della singola città, negli elementi rimanenti
			
				parziale.addAll( temp ); // provo ad aggiungere i 3gg
				rimanenti.get(i).removeAll(temp);
				cerca(parziale, L+1, rimanenti);
			
				parziale.removeAll( temp );
				rimanenti.get(i).addAll(temp);
				
			}
		}
				
	}
	
	// procedura per il calcolo del costo totale
	private int calcolaCosto (List<Rilevamento> parziale) {
		int costo = 0;
		for (int i = 0; i < parziale.size(); i++) {
			int umiditaMinima = 100;
			for ( int k = i; k < parziale.size() && parziale.get(k).getLocalita().equals(parziale.get(i).getLocalita()); k++) { // analizzo i giorni consecutivi nella stessa city
				int inEsame = parziale.get(k).getUmidita();
				if ( inEsame < umiditaMinima) { // prendo l'umidità minima dei giorni in cui sto nella city
					umiditaMinima = inEsame; 
				}
			}
			costo += umiditaMinima;
		}
		
		return costo;
	}
	

}
