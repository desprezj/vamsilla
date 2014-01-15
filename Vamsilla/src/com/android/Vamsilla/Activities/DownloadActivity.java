/***********************************************************
*	Fichier : DownloadActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 18/06/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Activité affichant la liste des fichiers vidéo en relation 
*	avec VAMSI.
*
*	VAMSILLA v6 2012
***************************************************************/
package com.android.Vamsilla.Activities;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.tools.GlobalEnum;
import com.android.Vamsilla.tools.MemorySize;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;




/**
 * Classe DownloadActivity
 */
public class DownloadActivity extends ListActivity  {
	
	
	private ListView listView;
	private boolean isAllChecked=false;
	
	//Préférences utilisateur
	private String vamsillaPath;
	private String ipServeur ;
	private String loginFtp;
	private String passwordFtp;
	
	private static final int DB_REFRESH = 400; //Fréquence des demandes d'accès à la base de données au cas où elle soit bloquée
	
	
	
	private List<ChannelSftp.LsEntry> fileVector;
	
	//Liste d'items séléctionnés dans la liste
	private Map<Integer , String> selectedItems;
	
	private ArrayList<HashMap<String, String>> listeItemFtp  = new ArrayList<HashMap<String, String>>();
	
	
	@Override
	 /** Procedure de creation de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activité est réinitialisée après avoir été
	 * 								arrêtée, les données sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		
		//Execution du sftp et récupération de la liste des fichiers
		new sFtp().execute();

		selectedItems = new HashMap<Integer, String>();
		
        SharedPreferences vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
        
		//Initialisation des variables
		vamsillaPath = vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
		loginFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.LOGIN_FTP, "roger");
		passwordFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.PASSWORD_FTP, "roger");
		ipServeur = vamsillaPrefs.getString(GlobalEnum.PREFS.IP_SERVER, "192.168.92.45");
		
		//Récupération automatique de la liste (l'id de cette liste est nommée obligatoirement @android:id/list afin d'être détectée)
		listView = getListView();
	}
	
	 /** Traitement d'un clic sur le bouton tout cocher/décocher
	 * 
	 * 
	 * @param v : source du clic
	 */
	public void checkAll(View v)
	{
		CheckBox cb;
		
		//Si clic sur le bouton tout cocher
		if(!isAllChecked)
		{
			for(int i = 0 ; i < getListAdapter().getCount(); i++)
			{
				//On coche la checkbox
				cb = (CheckBox) listView.getChildAt(i).findViewById(R.id.check);
				//Si l'élément n'est pas dans la liste de fichiers à télécharger, on l'ajoute
				if(!cb.isChecked())
				{
					String name = ((TextView) listView.getChildAt(i).findViewById(R.id.name)).getText().toString();
					int index = Integer.valueOf(((TextView) listView.getChildAt(i).findViewById(R.id.check)).getText().toString());
					selectedItems.put(index, name);
				}
				cb.setChecked(true);
				listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.cbChecked));
			}
			//On change le bouton en tout décocher
			((Button) v).setText(getString(R.string.uncheckAllButton));
		}
		//Si un clic sur le bouton tout décocher
		else
		{
			for(int i = 0 ; i < getListAdapter().getCount(); i++)
			{
				//On décoche la checkbox
				cb = (CheckBox) listView.getChildAt(i).findViewById(R.id.check);
				cb.setChecked(false);
				listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.bodyBg));
				int index = Integer.valueOf(((TextView) listView.getChildAt(i).findViewById(R.id.check)).getText().toString());
				//On enlève le fichier de la liste à télécharger
				selectedItems.remove(index);
			}
			//On change le bouton en tout cocher
			((Button) v).setText(getString(R.string.checkAllButton));
		}
		isAllChecked = !isAllChecked;
	}
	
	/** Traitement d'un clic sur le bouton "Télécharger"
	 * 
	 * 
	 * @param v : source du clic
	 */
	public void download(View v) {
		

		double totalSize;
		
		//on récupère le dossier "vamsilla" sur la carte SD
		File vFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath);
		
		ArrayList<String> downloadList = new ArrayList<String>();
		totalSize = 0;
		MemorySize memSize = new MemorySize(getApplicationContext());
		//on test si le dossier "vamsilla" existe (sinon, on affiche une erreur)
		if(!vFolder.isDirectory()) {
			Toast.makeText(getApplicationContext(), "Merci de créér le dossier \"vamsilla\" !",Toast.LENGTH_LONG).show();
		}else{
			
			//Pour chaque item coché dans la ListeView, on démarre le service
			Set<Integer> keySet = selectedItems.keySet();
			Iterator<Integer> it = keySet.iterator();
			File testFile;
			while(it.hasNext())
			{
				int index = it.next();
				String tempFileName = fileVector.get(index).getFilename();
				testFile = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + tempFileName);
				//Si un fichier séléctionné existe déjà sur le périphérique, on ne l'ajoute pas aux téléchargements
				if(testFile.exists()) {
					Toast.makeText(this, tempFileName + " : Fichier existant, ne sera pas téléchargé", Toast.LENGTH_SHORT);
				} else {
					totalSize += fileVector.get(index).getAttrs().getSize();
					downloadList.add(tempFileName);
				}
			}
			if(totalSize < memSize.getFreeMemory(GlobalEnum.MEMORY.BYTES))
			{
				//on crée un nouvelle intent vers l'IntentService de téléchargement des fichiers
				Intent iDownloadIntent = new Intent(this, DownloadSFTPService.class);
				iDownloadIntent.setPackage("com.android.Vamsilla.Activities");
				
					//paramètres d'information du serveur transmit au service
					iDownloadIntent.putExtra(GlobalEnum.DOWNLOAD.TOTAL_FILES_SIZE, totalSize);
					iDownloadIntent.putStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST, downloadList);
					//Paramètre des préférences
					iDownloadIntent.putExtra(GlobalEnum.PREFS.PATH, vamsillaPath);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.LOGIN_FTP, loginFtp);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.IP_SERVER, ipServeur);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.PASSWORD_FTP, passwordFtp);
					//Démarrage du service de téléchargement des fichiers
					startService(iDownloadIntent);
				
				//Lancement de l'activité "RunningDownloadsActivity" pour avoir l'affichage de la progression des téléchargements
				Intent iRunningDownload = new Intent(getApplicationContext(),RunningDownloadsActivity.class);
				startActivity(iRunningDownload);
			}
			//Place insuffisante : appel du buffer
			else
			{
				Toast.makeText(getApplicationContext(), "Espace insuffisant", Toast.LENGTH_SHORT).show();
				Intent iBuffer = new Intent(this,BufferActivity.class);
				startActivity(iBuffer);
			}
		}
	}
	
	
	/**
	 * 
	 * Connexion au serveur FTP et listing des fichiers
	 * 
	 */
	public class sFtp extends AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> {
		
		//nombre de fichiers présents sur le serveur
		private int nbrFichier = 0;
		
	    @SuppressWarnings( "unchecked" )
	    /**
	     * Execution du traitement en arrière plan
	     */
		protected ArrayList<HashMap<String, String>> doInBackground(Void... arg0) {

			//Connexion au serveur
			try {
				
				JSch jsch = new JSch();
				
				Session session = jsch.getSession(loginFtp, ipServeur, GlobalEnum.DOWNLOAD.PORT);
				
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				config.put("PreferredAuthentifications", "password");
				session.setConfig(config);
				
				session.setPassword(passwordFtp);
				session.connect();
				
				//Configuration du canal en mode sFTP et ouverture
				Channel channel = session.openChannel("sftp");
				ChannelSftp sftpChannel = (ChannelSftp) channel;
				sftpChannel.connect();
				
				fileVector = sftpChannel.ls(".");
				
				// On déclare la HashMap qui contiendra les informations pour un item (ou un fichier)
				HashMap<String, String> map;
				
				//formatage de l'affichage de la taille du fichier
				DecimalFormat f = new DecimalFormat();
		        f.setMaximumFractionDigits(2); 
			    for (int i=0; i<fileVector.size(); i++) {
			    	
			    	//Récupération du nom du fichier
			    	String fileName = fileVector.get(i).getFilename();
			    	if(showFile(fileName))
			    	{
				    	float cFileSize = fileVector.get(i).getAttrs().getSize();
				    	cFileSize /= 1048576.0; 
				    	
				    	//récupération de la taille du fichier
				    	 String fileSize = f.format(cFileSize);
				    	
				    	//instanciation de la HashMap 
						map = new HashMap<String, String>();
						//Ajout des informations du fichier à la liste
						map.put("name", fileName);
						map.put("place", "/" + fileName);
						map.put("taille", fileSize);
						map.put("index", Integer.toString(i));
					
						listeItemFtp.add(map);
						
						nbrFichier++;
						
					}
		    	}
			    
			    
			    //transmet le nombre de fichier présent sur le serveur
			    publishProgress(nbrFichier);
			} catch (final JSchException e) {
				Log.v("SFTPError",e.toString());
				runOnUiThread(new Runnable() {
				    public void run() {
				        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				    }
				});
				//Journalisation de l'erreur dans la BDD
				VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
				while(!db.open()) {
            		try {
						Thread.sleep(DB_REFRESH);
					} catch (InterruptedException e1) {
						Log.v("InterruptedException",e.toString());
					}
            	}
				db.insertEvent(new VamsillaEvent("Erreur","Erreur de connexion : " + e.getMessage(),0,"System"));
				db.close();
			} catch(final SftpException e) {
				Log.v("SFTPError",e.toString());
				runOnUiThread(new Runnable() {
				    public void run() {
				        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				    }
				});
				//Journalisation de l'erreur dans la BDD
				VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
				while(!db.open()) {
            		try {
						Thread.sleep(DB_REFRESH);
					} catch (InterruptedException e1) {
						Log.v("InterruptedException",e.toString());
					}
            	}
				db.insertEvent(new VamsillaEvent("Erreur","Erreur de connexion : " + e.getMessage(),0,"System"));
				db.close();
			}
			
			return listeItemFtp;
			
	    }

    /**
     * Traitement de l'évolution	
     * 
     * @param progress évolution
     */
    protected void onProgressUpdate(Integer... progress){
    	super.onProgressUpdate(progress);
    	Toast.makeText(getApplicationContext(), nbrFichier + " fichiers trouvés",Toast.LENGTH_LONG).show();
    }

    
    /**
     *  Remplissage de la liste avec les fichiers trouvés en fin de traitement
     *  
     *  @param result ArrayList contenant les données des champs à remplir dans la vue
     *  
     */
    protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
    	
    	if(!result.isEmpty()){
    	//Utilisation de notre adaptateur qui se chargera de placer les valeurs de notre liste automatiquement et d'affecter un tag é nos checkbox	    		
    			downloadsAdapter adapter = new downloadsAdapter(getBaseContext(), R.layout.download_row, listeItemFtp);

    					// On attribue à notre listView l'adaptateur que l'on vient de créer
    					listView.setAdapter(adapter);
    	}				
    }
}
	
	/**
	 *  Filtre les fichiers à afficher dans la listview
	 * @param fileName Nom du fichier
	 * @return true : afficher le fichier
	 * 		   false : ne pas afficher le fichier
	 */
	private boolean showFile(String fileName)
	{
		//Dossier actuel
		if(fileName.equals(".")) {
			return false;
		}
		//Dossier parent
		if(fileName.equals("..")) {
			return false;
		}
		//fichier md5
		if(fileName.endsWith("md5")) {
			return false;
		}
		
		return true;
	}


	
	/**
	 *  Redéfinition de l'array adapter pour les besoins de la classe : 
	 *   Instanciation à partir d'une liste de HashMap contenant les informations
	 *   Gestion de la liste des fichiers séléctionnés par un clic permettant de passer outre les limitations 
	 *   du recyclage des vues pour un nombre important de fichiers dans la liste
	 *
	 */
	public class downloadsAdapter extends ArrayAdapter<HashMap<String, String>> implements OnClickListener {
		
		private ArrayList<HashMap<String, String>> items;
		
		/**
		 * 	Constructeur de l'adapter 
		 * @param context Contexte d'utilisation de l'adapter permettant de faire le lien avec l'application
		 * @param textViewResourceId Ressource xml utilisée pour inflater la liste 
		 * @param items Liste de HashMaps contenant les informations
		 */
		public downloadsAdapter (Context context, int textViewResourceId, ArrayList<HashMap<String, String>> items)
		{
			super(context, textViewResourceId, items);
			this.items = (ArrayList<HashMap<String, String>>) items;

		}
		
		
		/**
		 *  Méthode appelée au remplissage de la vue 
		 *  
		 *  @param position position de l'objet dans la liste
		 *  @param ConvertView lien entre l'objet dans la liste et dans la listview
		 *  @param parent liste de vues affichées (ne tiens pas compte des objets hors écran)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			//Si la vue passée est vide, sinon c'est simplement un recyclage et elle n'a pas a être instanciée
			if(v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.download_row, null);
			}
			//Hashmap temporaire permettant de clarifier le traitement
			HashMap<String, String> map = items.get(position);
			Log.v("TestInflate", "Position : " + position + " FileName : " + map.get("name"));
			
			if(map != null)
			{
				//Récupération du champ comportant le nom du fichier
				TextView tn = (TextView) v.findViewById(R.id.name);
				if(tn != null ) {
					tn.setText(map.get("name"));
				}
				
				//Récupération du champ comportant le chemin d'accès au fichier
				TextView tp = (TextView) v.findViewById(R.id.place);
				if(tp != null) {
					tp.setText(map.get("place"));
				}
				
				//Récupération du champ comportant la taille du fichier
				TextView ts = (TextView) v.findViewById(R.id.taille);
				if(ts != null) {
					ts.setText(map.get("taille"));
				}
				
				//Récupération du champ caché comportant l'index du fichier dans la liste récupérée via ssh
				TextView ti = (TextView) v.findViewById(R.id.index);
				if(ti != null) {
					ti.setText(map.get("index"));
				}
			}
			
			//Ajout de l'écouteur permettant de traiter un clic sur la ligne
			v.setOnClickListener(this);	
			
			return v;
		}
		
		/**
		 * Traitement d'un clic sur la vue
		 * Le fonctionnement d'androïd pour les listview permettant d'économiser la mémoire rend l'accès aux données dans
		 * une listview complèxe : afin d'éviter les débordements de mémoire pour de longues listes seuls les éléments à l'écran
		 * sont traités nativement. Il est nécessaire de garder une liste des éléments pour un traitement poussé
		 * 
		 * @param v ligne émettant l'évènement
		 */
		public void onClick(View v) {
			//Récupération de la checkbox dans la liste
			CheckBox cb = (CheckBox) v.findViewById(R.id.check);
			//Nom du fichier
			String name =((TextView) v.findViewById(R.id.name)).getText().toString();
			//Index du fichier dans la liste récupérée via ssh
			int index = Integer.valueOf(((TextView) v.findViewById(R.id.index)).getText().toString());
			
			//Si il s'agit de la séléction de l'objet
			if(!cb.isChecked())
			{
				//On coche la checkbox
				cb.setChecked(true);
				//On ajoute le nom du fichier ainsi que son index dans la hashmap 
				selectedItems.put(index, name);
				//On change la couleur du fond
				v.setBackgroundColor(getResources().getColor(R.color.cbChecked));
			}
			//Sinon il s'agit d'une désélection 
			else
			{
				//On décoche la checkbox
				cb.setChecked(false);
				//On supprime l'entrée dans la hashmap
				selectedItems.remove(index);
				//On remet le fond naturel
				v.setBackgroundColor(getResources().getColor(R.color.bodyBg));
			}
		}
		
	}

}
