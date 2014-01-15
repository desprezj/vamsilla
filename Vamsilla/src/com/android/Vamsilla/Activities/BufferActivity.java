/***********************************************************
*	Fichier : FileListActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 17/06/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*	
*	Activit� permettant de supprimer des fichiers de l'application.
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;

import java.io.File;
import java.util.List;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.color;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe BufferActivity
 */
public class BufferActivity extends ListActivity {
	
	private ListView listView;
	private String vamsillaPath;
	private boolean isAllChecked;
	private List<HashMap<String, String>> fileMap;

	
	@Override
	 /** Proc�dure de cr�ation de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activit� est r�initialis�e apr�s avoir �t� 
	 * 								arr�t�e, les donn�es sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.buffer_list);
        SharedPreferences vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
        
		vamsillaPath =  vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
		fileMap = new ArrayList<HashMap<String, String>>();
		
		//R�cup�ration automatique de la liste (l'id de cette liste est nomm� obligatoirement @android:id/list afin d'�tre d�tect�)
		listView = getListView();
		
		initialisingFileList(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath));
		if(!fileMap.isEmpty())
		{
			SimpleAdapter adapter = new SimpleAdapter(this.getBaseContext(), fileMap,R.layout.download_row, new String[] { "name", "place", "taille" }, new int[] {R.id.name, R.id.place, R.id.taille});
		// On attribue � notre listView l'adaptateur que l'on vient de cr�er
			listView.setAdapter(adapter);
		}
		
		
	}
	
	 /** Traitement d'un clic sur une checkbox par l'utilisateur
	 * 
	 * 
	 * @param v : source du clic
	 */
	public void checkHandler(View v){
		
		CheckBox cb = (CheckBox) v;
		
		//Changement de couleur du fond lors du click sur une checkbox
		if(cb.isChecked())
		{
			v.setBackgroundResource(R.color.cbChecked);
		}
		else
		{
			v.setBackgroundResource(color.transparent);
		}
	}
		
	
	
	 /** Traitement d'un clic sur le bouton tout cocher/d�cocher
	 * 
	 * 
	 * @param v : source du clic
	 */
	public void checkAll(View v)
	{
			Button b = (Button) v;
			
			//S�l�ction de tous les fichiers
			for(int i=0;i<listView.getAdapter().getCount(); i++)
			{
				CheckBox cb=(CheckBox)listView.getChildAt(i).findViewById(R.id.check);
				if(!isAllChecked) {
					cb.setChecked(true);
					listView.getChildAt(i).setBackgroundResource(R.color.cbChecked);
				}
				else {
					cb.setChecked(false);
					listView.getChildAt(i).setBackgroundResource(color.transparent);
				}
			}

			//Changement du texte du bouton
			if(!isAllChecked) {
				b.setText(R.string.uncheckAllButton);
			}
			else {
				b.setText(R.string.checkAllButton);
			}
			isAllChecked=!isAllChecked;
	}
	
	/**
	 * 	M�thode de suppression des fichiers s�l�ctionn�s
	 * @param v �l�ment appelant
	 */
	public void delete(View v)
	{
		
		ArrayList<String> deletedFiles = new ArrayList<String>();
		//D�termination des fichiers s�l�ctionn�s
		for(int i=0; i<listView.getAdapter().getCount();i++) {
			CheckBox cb=(CheckBox)listView.getChildAt(i).findViewById(R.id.check);
			if(cb.isChecked()) {
				String tFileName = ((TextView)listView.getChildAt(i).findViewById(R.id.place)).getText().toString();
				deletedFiles.add(tFileName);
			}
		}
		//Suppression des fichiers s�l�ctionn�s
		if(!deletedFiles.isEmpty()) {
			
			File tempFile;
			String tempFileName;
			double tempFileSize;
			
			VamsillaEventDB db = new VamsillaEventDB(getBaseContext());
			
			while(!db.open()) {
							try {
								Thread.sleep(GlobalEnum.MISC.DB_REFRESH);
							} catch (InterruptedException e) {
								Log.v("InterruptedException",e.toString());
							}
						}
			
			for(int i=0; i< deletedFiles.size();i++) { 
				
				tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+vamsillaPath+deletedFiles.get(i));
				tempFileName = tempFile.getName();
				tempFileSize = tempFile.length();
				//Fichier innexistant
				if(!tempFile.exists()) {
					db.insertEvent(new VamsillaEvent("Suppression","Demande de suppression de " + tempFileName + " : fichier innexistant",tempFileSize,"user"));
				}
				//Fichier existant
				if( tempFile.delete()) {
					db.insertEvent(new VamsillaEvent("Suppression","Demande de suppression de " + tempFileName + " : succ�s ",0,"user"));
					db.insertEvent(new VamsillaEvent("Suppression","Demande de suppression de " + tempFileName + " : succ�s ",tempFileSize,"user"));
				}
				//Suppression impossible
				else {
					db.insertEvent(new VamsillaEvent("Suppression","Demande de suppression de " + tempFileName + " : �chec",tempFileSize,"user"));
				}
			}
			
			db.close();
			this.finish();
		}
		
	}
	
	/**
	 *  Initialisation de la liste de fichiers pr�sents dans le  vamsilla path
	 * @param dir dossier � utiliser
	 */
	public void initialisingFileList(File dir){
		if (dir.exists()) {
            File[] fileList = dir.listFiles();
            // On d�clare la HashMap qui contiendra les informations pour un item (ou un fichier)
			HashMap<String, String> map;
			
			//formatage de l'affichage de la taille du fichier
			DecimalFormat f = new DecimalFormat();
	        f.setMaximumFractionDigits(2); 
	        
            for(int i = 0; i < fileList.length; i++) {
            	//R�cup�ration du nom du fichier
		    	String fileName = fileList[i].getName();
		    	
		    	float cFileSize = fileList[i].length();
		    	cFileSize /= 1048576.0; 
		    	
		    	//r�cup�ration de la taille du fichier
		    	 String fileSize = f.format(cFileSize) + " Mo";
		    	
		    	//instanciation de la HashMap 
				map = new HashMap<String, String>();
				
				//Ajout des informations du fichier � la liste
				map.put("name", fileName);
				map.put("place", "/" + fileName);
				map.put("taille", fileSize);
			
				fileMap.add(map);
            }
        }else{
    		Toast.makeText(getApplicationContext(), "Merci de cr�e la dossier \"vamsilla\" !",Toast.LENGTH_LONG).show();
        }
	}
}
