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
	//numero mobile host che hanno subito perdite
	private static int numMobileHost = 0;
	private static double med_latenze = 0;
	private static double prob = 0;
	/**
	 * Salva le perdite medie di pacchetti del mh
	 * @param media_pckts_mh
	 */
	public static void salvaMediaPacchetti(int media_pckts_mh){
		tot_pacchetti+=media_pckts_mh;
	}
	
	public static void salvaPercentualePacchettiPersi(double pr){
		numMobileHost++;
		prob+=pr;
	}
	
	/**
	 * Somma la latenza media del mh
	 * @param media_latenza_mh
	 */
	public static void salvaLatenzaMedia(double media_latenza_mh){
		med_latenze += media_latenza_mh;
	}
	
	public static void salva(){
		System.out.println("numero mobile host = "+numMobileHost);
		int media_tot_pacchetti = tot_pacchetti/numMobileHost;
		double lat_media = med_latenze/numMobileHost;
		double pr = prob/numMobileHost;
		try {
			File f = new File("src/radioFM/mediaPacchetti.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,media_tot_pacchetti);
			System.out.println("mediaPacchetti = "+media_tot_pacchetti);
			
			f = new File("src/radioFM/latenzaMedia.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,lat_media);
			System.out.println("Latenza media = "+lat_media);
			
			f = new File("src/radioFM/probabilitaPacchettiPersi.txt");
			if(!f.exists()) {
				f.createNewFile();
			}
			salvaStat(f,pr);
			System.out.println("Probabilità pacchetti persi = "+pr);
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
