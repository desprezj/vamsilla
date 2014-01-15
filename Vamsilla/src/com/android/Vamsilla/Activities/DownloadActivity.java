/***********************************************************
*	Fichier : DownloadActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 18/06/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Activit� affichant la liste des fichiers vid�o en relation 
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
	
	//Pr�f�rences utilisateur
	private String vamsillaPath;
	private String ipServeur ;
	private String loginFtp;
	private String passwordFtp;
	
	private static final int DB_REFRESH = 400; //Fr�quence des demandes d'acc�s � la base de donn�es au cas o� elle soit bloqu�e
	
	
	
	private List<ChannelSftp.LsEntry> fileVector;
	
	//Liste d'items s�l�ctionn�s dans la liste
	private Map<Integer , String> selectedItems;
	
	private ArrayList<HashMap<String, String>> listeItemFtp  = new ArrayList<HashMap<String, String>>();
	
	
	@Override
	 /** Procedure de creation de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activit� est r�initialis�e apr�s avoir �t�
	 * 								arr�t�e, les donn�es sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);
		
		//Execution du sftp et r�cup�ration de la liste des fichiers
		new sFtp().execute();

		selectedItems = new HashMap<Integer, String>();
		
        SharedPreferences vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
        
		//Initialisation des variables
		vamsillaPath = vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
		loginFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.LOGIN_FTP, "roger");
		passwordFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.PASSWORD_FTP, "roger");
		ipServeur = vamsillaPrefs.getString(GlobalEnum.PREFS.IP_SERVER, "192.168.92.45");
		
		//R�cup�ration automatique de la liste (l'id de cette liste est nomm�e obligatoirement @android:id/list afin d'�tre d�tect�e)
		listView = getListView();
	}
	
	 /** Traitement d'un clic sur le bouton tout cocher/d�cocher
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
				//Si l'�l�ment n'est pas dans la liste de fichiers � t�l�charger, on l'ajoute
				if(!cb.isChecked())
				{
					String name = ((TextView) listView.getChildAt(i).findViewById(R.id.name)).getText().toString();
					int index = Integer.valueOf(((TextView) listView.getChildAt(i).findViewById(R.id.check)).getText().toString());
					selectedItems.put(index, name);
				}
				cb.setChecked(true);
				listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.cbChecked));
			}
			//On change le bouton en tout d�cocher
			((Button) v).setText(getString(R.string.uncheckAllButton));
		}
		//Si un clic sur le bouton tout d�cocher
		else
		{
			for(int i = 0 ; i < getListAdapter().getCount(); i++)
			{
				//On d�coche la checkbox
				cb = (CheckBox) listView.getChildAt(i).findViewById(R.id.check);
				cb.setChecked(false);
				listView.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.bodyBg));
				int index = Integer.valueOf(((TextView) listView.getChildAt(i).findViewById(R.id.check)).getText().toString());
				//On enl�ve le fichier de la liste � t�l�charger
				selectedItems.remove(index);
			}
			//On change le bouton en tout cocher
			((Button) v).setText(getString(R.string.checkAllButton));
		}
		isAllChecked = !isAllChecked;
	}
	
	/** Traitement d'un clic sur le bouton "T�l�charger"
	 * 
	 * 
	 * @param v : source du clic
	 */
	public void download(View v) {
		

		double totalSize;
		
		//on r�cup�re le dossier "vamsilla" sur la carte SD
		File vFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath);
		
		ArrayList<String> downloadList = new ArrayList<String>();
		totalSize = 0;
		MemorySize memSize = new MemorySize(getApplicationContext());
		//on test si le dossier "vamsilla" existe (sinon, on affiche une erreur)
		if(!vFolder.isDirectory()) {
			Toast.makeText(getApplicationContext(), "Merci de cr��r le dossier \"vamsilla\" !",Toast.LENGTH_LONG).show();
		}else{
			
			//Pour chaque item coch� dans la ListeView, on d�marre le service
			Set<Integer> keySet = selectedItems.keySet();
			Iterator<Integer> it = keySet.iterator();
			File testFile;
			while(it.hasNext())
			{
				int index = it.next();
				String tempFileName = fileVector.get(index).getFilename();
				testFile = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + tempFileName);
				//Si un fichier s�l�ctionn� existe d�j� sur le p�riph�rique, on ne l'ajoute pas aux t�l�chargements
				if(testFile.exists()) {
					Toast.makeText(this, tempFileName + " : Fichier existant, ne sera pas t�l�charg�", Toast.LENGTH_SHORT);
				} else {
					totalSize += fileVector.get(index).getAttrs().getSize();
					downloadList.add(tempFileName);
				}
			}
			if(totalSize < memSize.getFreeMemory(GlobalEnum.MEMORY.BYTES))
			{
				//on cr�e un nouvelle intent vers l'IntentService de t�l�chargement des fichiers
				Intent iDownloadIntent = new Intent(this, DownloadSFTPService.class);
				iDownloadIntent.setPackage("com.android.Vamsilla.Activities");
				
					//param�tres d'information du serveur transmit au service
					iDownloadIntent.putExtra(GlobalEnum.DOWNLOAD.TOTAL_FILES_SIZE, totalSize);
					iDownloadIntent.putStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST, downloadList);
					//Param�tre des pr�f�rences
					iDownloadIntent.putExtra(GlobalEnum.PREFS.PATH, vamsillaPath);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.LOGIN_FTP, loginFtp);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.IP_SERVER, ipServeur);
					iDownloadIntent.putExtra(GlobalEnum.PREFS.PASSWORD_FTP, passwordFtp);
					//D�marrage du service de t�l�chargement des fichiers
					startService(iDownloadIntent);
				
				//Lancement de l'activit� "RunningDownloadsActivity" pour avoir l'affichage de la progression des t�l�chargements
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
		
		//nombre de fichiers pr�sents sur le serveur
		private int nbrFichier = 0;
		
	    @SuppressWarnings( "unchecked" )
	    /**
	     * Execution du traitement en arri�re plan
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
				
				// On d�clare la HashMap qui contiendra les informations pour un item (ou un fichier)
				HashMap<String, String> map;
				
				//formatage de l'affichage de la taille du fichier
				DecimalFormat f = new DecimalFormat();
		        f.setMaximumFractionDigits(2); 
			    for (int i=0; i<fileVector.size(); i++) {
			    	
			    	//R�cup�ration du nom du fichier
			    	String fileName = fileVector.get(i).getFilename();
			    	if(showFile(fileName))
			    	{
				    	float cFileSize = fileVector.get(i).getAttrs().getSize();
				    	cFileSize /= 1048576.0; 
				    	
				    	//r�cup�ration de la taille du fichier
				    	 String fileSize = f.format(cFileSize);
				    	
				    	//instanciation de la HashMap 
						map = new HashMap<String, String>();
						//Ajout des informations du fichier � la liste
						map.put("name", fileName);
						map.put("place", "/" + fileName);
						map.put("taille", fileSize);
						map.put("index", Integer.toString(i));
					
						listeItemFtp.add(map);
						
						nbrFichier++;
						
					}
		    	}
			    
			    
			    //transmet le nombre de fichier pr�sent sur le serveur
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
     * Traitement de l'�volution	
     * 
     * @param progress �volution
     */
    protected void onProgressUpdate(Integer... progress){
    	super.onProgressUpdate(progress);
    	Toast.makeText(getApplicationContext(), nbrFichier + " fichiers trouv�s",Toast.LENGTH_LONG).show();
    }

    
    /**
     *  Remplissage de la liste avec les fichiers trouv�s en fin de traitement
     *  
     *  @param result ArrayList contenant les donn�es des champs � remplir dans la vue
     *  
     */
    protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
    	
    	if(!result.isEmpty()){
    	//Utilisation de notre adaptateur qui se chargera de placer les valeurs de notre liste automatiquement et d'affecter un tag � nos checkbox	    		
    			downloadsAdapter adapter = new downloadsAdapter(getBaseContext(), R.layout.download_row, listeItemFtp);

    					// On attribue � notre listView l'adaptateur que l'on vient de cr�er
    					listView.setAdapter(adapter);
    	}				
    }
}
	
	/**
	 *  Filtre les fichiers � afficher dans la listview
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
	 *  Red�finition de l'array adapter pour les besoins de la classe : 
	 *   Instanciation � partir d'une liste de HashMap contenant les informations
	 *   Gestion de la liste des fichiers s�l�ctionn�s par un clic permettant de passer outre les limitations 
	 *   du recyclage des vues pour un nombre important de fichiers dans la liste
	 *
	 */
	public class downloadsAdapter extends ArrayAdapter<HashMap<String, String>> implements OnClickListener {
		
		private ArrayList<HashMap<String, String>> items;
		
		/**
		 * 	Constructeur de l'adapter 
		 * @param context Contexte d'utilisation de l'adapter permettant de faire le lien avec l'application
		 * @param textViewResourceId Ressource xml utilis�e pour inflater la liste 
		 * @param items Liste de HashMaps contenant les informations
		 */
		public downloadsAdapter (Context context, int textViewResourceId, ArrayList<HashMap<String, String>> items)
		{
			super(context, textViewResourceId, items);
			this.items = (ArrayList<HashMap<String, String>>) items;

		}
		
		
		/**
		 *  M�thode appel�e au remplissage de la vue 
		 *  
		 *  @param position position de l'objet dans la liste
		 *  @param ConvertView lien entre l'objet dans la liste et dans la listview
		 *  @param parent liste de vues affich�es (ne tiens pas compte des objets hors �cran)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			//Si la vue pass�e est vide, sinon c'est simplement un recyclage et elle n'a pas a �tre instanci�e
			if(v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.download_row, null);
			}
			//Hashmap temporaire permettant de clarifier le traitement
			HashMap<String, String> map = items.get(position);
			Log.v("TestInflate", "Position : " + position + " FileName : " + map.get("name"));
			
			if(map != null)
			{
				//R�cup�ration du champ comportant le nom du fichier
				TextView tn = (TextView) v.findViewById(R.id.name);
				if(tn != null ) {
					tn.setText(map.get("name"));
				}
				
				//R�cup�ration du champ comportant le chemin d'acc�s au fichier
				TextView tp = (TextView) v.findViewById(R.id.place);
				if(tp != null) {
					tp.setText(map.get("place"));
				}
				
				//R�cup�ration du champ comportant la taille du fichier
				TextView ts = (TextView) v.findViewById(R.id.taille);
				if(ts != null) {
					ts.setText(map.get("taille"));
				}
				
				//R�cup�ration du champ cach� comportant l'index du fichier dans la liste r�cup�r�e via ssh
				TextView ti = (TextView) v.findViewById(R.id.index);
				if(ti != null) {
					ti.setText(map.get("index"));
				}
			}
			
			//Ajout de l'�couteur permettant de traiter un clic sur la ligne
			v.setOnClickListener(this);	
			
			return v;
		}
		
		/**
		 * Traitement d'un clic sur la vue
		 * Le fonctionnement d'andro�d pour les listview permettant d'�conomiser la m�moire rend l'acc�s aux donn�es dans
		 * une listview compl�xe : afin d'�viter les d�bordements de m�moire pour de longues listes seuls les �l�ments � l'�cran
		 * sont trait�s nativement. Il est n�cessaire de garder une liste des �l�ments pour un traitement pouss�
		 * 
		 * @param v ligne �mettant l'�v�nement
		 */
		public void onClick(View v) {
			//R�cup�ration de la checkbox dans la liste
			CheckBox cb = (CheckBox) v.findViewById(R.id.check);
			//Nom du fichier
			String name =((TextView) v.findViewById(R.id.name)).getText().toString();
			//Index du fichier dans la liste r�cup�r�e via ssh
			int index = Integer.valueOf(((TextView) v.findViewById(R.id.index)).getText().toString());
			
			//Si il s'agit de la s�l�ction de l'objet
			if(!cb.isChecked())
			{
				//On coche la checkbox
				cb.setChecked(true);
				//On ajoute le nom du fichier ainsi que son index dans la hashmap 
				selectedItems.put(index, name);
				//On change la couleur du fond
				v.setBackgroundColor(getResources().getColor(R.color.cbChecked));
			}
			//Sinon il s'agit d'une d�s�lection 
			else
			{
				//On d�coche la checkbox
				cb.setChecked(false);
				//On supprime l'entr�e dans la hashmap
				selectedItems.remove(index);
				//On remet le fond naturel
				v.setBackgroundColor(getResources().getColor(R.color.bodyBg));
			}
		}
		
	}

}
