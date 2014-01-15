/***********************************************************
*	Fichier : FileListActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 17/06/2012
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
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.db.VamsillaFileDB;
import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe FileListActivity
 */
public class FileListActivity extends ListActivity {
	
	private ListView listView;
	private String vamsillaPath ;
	private List<String> fileList;
	private ArrayAdapter<String> adapter;
	private static final int N1 = 200;
	private ProgressDialog m_progress;
	private FileReceiverRD fileReceiverRD;
	
	@Override
	 /** Procédure de création de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activité est réinitialisée après avoir été 
	 * 								arrêtée, les données sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_list);
        SharedPreferences vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
        
		//Enregistrement du broadcastreceiver permettant de savoir si la décompression est terminée
        IntentFilter filter = new IntentFilter(FileReceiverRD.UNTAR_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        fileReceiverRD = new FileReceiverRD();
		registerReceiver(fileReceiverRD, filter);
        
		vamsillaPath =  vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
		//Récupération automatique de la liste (l'id de cette liste est nommé obligatoirement @android:id/list afin d'être détecté)
		listView = getListView();
		
		registerForContextMenu(listView);
		initialiseFileList();
		
	}
	
	/**
	 * on supprime le BroadcastReceiver lorsque l'activité est détruite lorsqu'elle n'est plus au premier plan
	 * (cela évite les erreurs, et optmise les ressources de l'application (à quoi bon mettre à jour une barre de progression qui n'est plus à l'écran)
	 */
	protected void onDestroy(){
	    super.onDestroy();
		unregisterReceiver(fileReceiverRD);
	}
	
	
	
	/**
	 * Méthode appelée au clic sur un élément de la liste
	 * 
	 * @param l listview contenant l'élément appelé
	 * @param v ligne de l'élément appelant 
	 * @param id id de l'élément appelant
	 *  
	 */
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		openContextMenu(v);
		unregisterForContextMenu(v);
	}
	
	/**
	 * 	Remplissage de la liste avec les fichiers trouvés 
	 * 
	 * @param dir répertoire à traiter
	 */
	public void initialiseFileList(){
		File dir = new File(Environment.getExternalStorageDirectory() + vamsillaPath);
		fileList = new ArrayList<String>();
		
		if (dir.exists()) {
            File[] tFileList = dir.listFiles();
            for(int i = 0; i < tFileList.length; i++) {
            	if(showFile(tFileList[i].getName())) {
                	fileList.add(tFileList[i].getName());
            	}
            }
        }else{
    		Toast.makeText(getApplicationContext(), "Merci de créér le dossier \"vamsilla\" !",Toast.LENGTH_LONG).show();
        }
		
		adapter= new FileListAdapter(this,R.id.fileName,(ArrayList<String>)fileList);
		// On attribue à notre listView l'adaptateur que l'on vient de créer
		listView.setAdapter(adapter);
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
		
		File file = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + fileName);
		 
		//Si c'est un dossier
		if(file.isDirectory()) {
			return false;
		}
		
		return true;
	}

	/**
	 * Création du menu contextuel
	 */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.filelist_context_menu, menu);
	}
	
	
	/** Séléction d'un élément dans le menu contextuel
	 * 
	 * @param élément séléctionné
	 */
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		//Fichier séléctionné
		String selectedFileName = fileList.get(info.position);
		//Ouverture du fichier
		final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath + "/" + selectedFileName);
		
		//Détermination de l'action demandée
		switch(item.getItemId()) { 
			case R.id.openFile :
				
				//Si c'est une archive, on décompresse
				if(file.getName().endsWith("tar") || file.getName().endsWith("tar.gz")){
					
					//Configuration du progressDialog
					m_progress = new ProgressDialog(this);
					m_progress.setTitle("Décompression...");
					m_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					m_progress.setIndeterminate(false);
					m_progress.show(); 
					
					//Lancement de la décompression
					Intent iUntar = new Intent(this, UntarService.class);
					iUntar.putExtra(GlobalEnum.DOWNLOAD.FILE_NAME, file.getName());
					iUntar.putExtra(GlobalEnum.PREFS.PATH, vamsillaPath);
					startService(iUntar);
					
				} 
				
				//Sinon on détermine le type de fichier
				else {
					Intent iFileLauncher = new Intent();
					iFileLauncher.setAction(android.content.Intent.ACTION_VIEW);
					//Détermination du type de fichier à ouvrir
					MimeTypeMap mime = MimeTypeMap.getSingleton();
					String ext = file.getName().substring(file.getName().lastIndexOf(".")+1);
					String type = mime.getMimeTypeFromExtension(ext);
					//Ouverture du fichier
					iFileLauncher.setDataAndType(Uri.fromFile(file),type);
					try {
						startActivity(iFileLauncher);
					} catch (ActivityNotFoundException e) {
						Toast.makeText(this, "Aucune activité trouvée pour ouvrir le fichier", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			
			//Suppression du fichier
			case R.id.deleteFile : 
				
				//Demande de validation par l'utilisateur
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				
				alertDialog.setTitle("Suppression...");
				
				alertDialog.setMessage("Souhaitez-vous supprimer le fichier ?");
				
				//Validation de la suppression
				alertDialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
					/**
					 * écouteur du clic sur le bouton 
					 */
		            public void onClick(DialogInterface dialog,int which) {
		        		VamsillaEventDB dbEvents = new VamsillaEventDB(getApplicationContext());

		        		//Attente d'un accès à la BDD
		        		while(!dbEvents.open()) {
		        			try {
								Thread.sleep(N1);
							} catch (InterruptedException e) {
								Log.e("Erreur", "Erreur de thread");
							}
		        		}
		        		
		        		//Journalisation de la demande de suppression
		        		dbEvents.insertEvent(new VamsillaEvent(GlobalEnum.DB.DELETE,GlobalEnum.DB.DELETE_BODY + file.getName(),file.length(),"User"));
		        		
		        		//Suppression réussie
		        		if(file.delete())
		        		{
		        			dbEvents.insertEvent(new VamsillaEvent(GlobalEnum.DB.DELETE,GlobalEnum.DB.DELETE_BODY_SUCCESS + file.getName(),file.length(),"System"));
		        			Toast.makeText(getApplicationContext(),GlobalEnum.DB.DELETE_BODY_SUCCESS + file.getName() , Toast.LENGTH_SHORT).show();
		        		}
		        		//Echec de la suppression
		        		else
		        		{
		        			dbEvents.insertEvent(new VamsillaEvent(GlobalEnum.DB.DELETE,GlobalEnum.DB.DELETE_BODY_FAIL + file.getName(),file.length(),"System"));
		        			Toast.makeText(getApplicationContext(),GlobalEnum.DB.DELETE_BODY_FAIL + file.getName() , Toast.LENGTH_SHORT).show();
		        		}
		        		
		        		dbEvents.close();
		        		
		        		//Suppression du fichier de la table dans la BDD
		        		VamsillaFileDB dbFiles = new VamsillaFileDB(getApplicationContext());
		        		
		        		//Attente d'un accès à la BDD
		        		while(!dbFiles.open()) {
		        			try {
								Thread.sleep(N1);
							} catch (InterruptedException e) {
								Log.e("Erreur", "Erreur de thread");
							}
		        		}
		        		
		        		try {
	        				dbFiles.removeEventByName(file.getName());		        			
		        		} catch(SQLException e) {
		        			//Rien à faire ici...
		        		} finally {
			        		dbFiles.close();
			        				        			
		        		}//Rechargement de la liste de fichiers
		        		initialiseFileList();
		            }
		        });
				
				//Annulation de la suppression
				alertDialog.setNegativeButton("NON", new DialogInterface.OnClickListener() {
					/**
					 * Clic sur le bouton non
					 */
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.cancel();
					}
					
				});
				
				alertDialog.show();
				break;
		
		}
		
		return true;
	}
	
	/**
	 * Récupération del a fin de la décompression
	 */
	public class FileReceiverRD extends BroadcastReceiver {
	
		public static final String UNTAR_RESPONSE = "com.android.Vamsilla.Activities.intent.action.UNTAR_RESPONSE";
		
		@Override
		/**
		* Méthode appelée à la réception d'une information par broadcastReceiver
		*/
		public void onReceive(Context context, Intent intent) {
			if(m_progress != null){
				m_progress.dismiss();
			}
			initialiseFileList();
		}
		
	}
		
}
