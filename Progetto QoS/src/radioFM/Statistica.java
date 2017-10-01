package radioFM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Statistica {
	
	private static int tot_pacchetti = 0;
	private static int numMobileHost = 0;
	private static double latenze = 0;
	private static double med_latenze = 0;
	
	/**
	 * Salva le perdite medie di pacchetti del mh
	 * @param media_pckts_mh
	 */
	public static void salvaMediaPacchetti(int media_pckts_mh){
		tot_pacchetti+=media_pckts_mh;
		numMobileHost++;
	}
	
	/**
	 * Somma le latenze totali dei mh che verranno divise per il numero di mh
	 * @param latenza_mh
	 */
	public static void salvaLatenzaTotale(double latenza_mh){
		latenze+= latenza_mh;
	}
	
	/**
	 * Somma la latenza media del mh
	 * @param media_latenza_mh
	 */
	public static void salvaLatenzaMedia(double media_latenza_mh){
		med_latenze += media_latenza_mh;
	}
	
	public static void salva(){
		int media_tot_pacchetti = tot_pacchetti/numMobileHost;
		double lat_media_tot = latenze/numMobileHost;
		double lat_media = med_latenze/numMobileHost;
		try {
			File f = new File("src/radioFM/mediaPacchetti.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,media_tot_pacchetti);
			System.out.println("mediaPacchetti = "+media_tot_pacchetti);
			f = new File("src/radioFM/latenzaMediaTot.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,lat_media_tot);
			System.out.println("Latenza media totale = "+lat_media_tot);
			f = new File("src/radioFM/latenzaMedia.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,lat_media);
			System.out.println("Latenza media = "+lat_media);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static void salvaStat(File f, int stat) throws IOException{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String linea = "", l = "";
			while(l != null) {
				l = br.readLine();
				if(l != null)
					linea += l+"\n";
			}
			br.close();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.print(linea);
			pw.print(stat);
			pw.close();
	}
	
	private static void salvaStat(File f, double stat) throws IOException{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String linea = "", l = "";
			while(l != null) {
				l = br.readLine();
				if(l != null)
					linea += l+"\n";
			}
			br.close();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.print(linea);
			pw.print(stat);
			pw.close();
	}
}
