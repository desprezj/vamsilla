/***********************************************************
*	Fichier : HomeActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 17/06/2012
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.tools.GlobalEnum;
import com.android.Vamsilla.tools.MemorySize;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * Classe HomeActivity
 */
public class HomeActivity extends Activity{

	private String vamsillaPath;
	private boolean existingVamsillaFolder = false;
	private static final int N0 = 100;
    @Override
	 /*** Proc�dure de cr�ation de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activit� est r�initialis�e apr�s avoir �t� 
	 * 								arr�t�e, les donn�es sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        SharedPreferences vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = vamsillaPrefs.edit();
        
		
		//Modification des pr�f�rences si elles ont �t� modifi�es
        if(!vamsillaPrefs.contains(GlobalEnum.PREFS.PATH)) {
        	prefEditor.putString(GlobalEnum.PREFS.PATH, "/vamsilla");
        }
        
        if(!vamsillaPrefs.contains(GlobalEnum.PREFS.IP_SERVER)) {
        	prefEditor.putString(GlobalEnum.PREFS.IP_SERVER, "192.168.92.45");
        }
        
        if(!vamsillaPrefs.contains(GlobalEnum.PREFS.LOGIN_FTP)) {
        	prefEditor.putString(GlobalEnum.PREFS.LOGIN_FTP, "roger");
        }
        
        if(!vamsillaPrefs.contains(GlobalEnum.PREFS.PASSWORD_FTP)) {
        	prefEditor.putString(GlobalEnum.PREFS.PASSWORD_FTP, "roger");
        }
        
    	
        prefEditor.commit();
        
        vamsillaPath = vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
        existingVamsillaFolder = initialisingFolder();
        
    }

    @Override
    /** Permet la mise � jour de l'affichage de l'espace m�moire
	 * (l'activit� principale est mise en pause quand elle n'est plus au premier plan)
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
    protected void onResume() {
        super.onResume();
        
        if(existingVamsillaFolder) {
        	initialisingMemoryInfo();
        }
    }

    
    /** M�thode appell�e lors d'un clic sur le bouton "connexion"
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void connectionButton(View v){
			Intent iList = new Intent(this,ConnexionActivity.class);
			startActivity(iList);
	}
	
	
	/** M�thode appell�e lors d'un clic sur le bouton "Acc�der aux fichiers"
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void listFilesButton(View v){
		if(existingVamsillaFolder){
			Intent iList = new Intent(this,FileListActivity.class);
			startActivity(iList);
		}else{
			Toast.makeText(getApplicationContext(), "Merci de cr�er le dossier \"vamsilla\" !",Toast.LENGTH_LONG).show();
		}
	}
	
	
	/** M�thode appell�e lors d'un clic sur le bouton "T�l�chargements en cours"
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void downloadsButton(View v)
	{
		Intent iList = new Intent(this,RunningDownloadsActivity.class);
		startActivity(iList);
	}
    
	/**
	 *  M�thode appel�e lors d'un clic sur le bouton Logs
	 * @param v
	 * @throws SftpException 
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void logButton(View v)
	{
		
		Intent iLogger = new Intent(this, LogActivity.class);
		startActivity(iLogger);
	}
	
	/** M�thode appell�e lors du lancement de l'activit� afin de v�rifier la pr�sence du dossier "vamsilla",
	 * o� seront sotck�s les fichiers de l'application
	 * 
	 * 
	 *@see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public boolean initialisingFolder(){
		
		File vFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath);
		Log.v("FOLDER", Environment.getExternalStorageDirectory().getAbsolutePath() + vamsillaPath);
		if(!vFolder.isDirectory()) {
			vFolder.mkdir();
			if(!vFolder.isDirectory()){
				Toast.makeText(getApplicationContext(), "Le dossier \"vamsilla\" n'a pas pu �tre cr�e. Merci de le cr�e !",Toast.LENGTH_LONG).show();
				return false;
			}else{
				Toast.makeText(getApplicationContext(), "Le dossier \"vamsilla\" a �t� cr�e avec succ�s.",Toast.LENGTH_SHORT).show();
				return true;
			}
		}else{
			return true;
		}
		
	}
	
	/** M�thode appell�e afin de mettre � jour l'affichage des informations concernant l'espace de stockage
	 * de la carte SD
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void initialisingMemoryInfo(){
		
		//Initialisation de la barre de m�moire
		ProgressBar memProgress = (ProgressBar)findViewById(R.id.pbEspaceSD);
        MemorySize memSize = new MemorySize(this.getBaseContext());
        memProgress.setMax((int)(memSize.getTotalMemory(GlobalEnum.MEMORY.GO)*N0));
        memProgress.setProgress((int)(memSize.getVamsillaMemory(GlobalEnum.MEMORY.GO)*N0));
        memProgress.setSecondaryProgress((int)(memSize.getBusyMemory(GlobalEnum.MEMORY.GO)*N0));
        
        //Chargement des vues � modifier
        TextView tvTotal= (TextView) findViewById(R.id.tvEspaceTotal);
        TextView tvBusy= (TextView) findViewById(R.id.tvEspaceUt);
        TextView tvVamsilla= (TextView) findViewById(R.id.tvVamsillaSpace);
        TextView tvNbFiles= (TextView) findViewById(R.id.nbFichiers);
        
        //Format des chiffres � afficher dans les textview
        DecimalFormat f = new DecimalFormat();
        f.setMaximumFractionDigits(2);
        
        //Remplissage des textview
        tvTotal.setText(getString(R.string.totalSpace) + f.format(memSize.getTotalMemory(GlobalEnum.MEMORY.GO)) + " Go");
        tvBusy.setText(getString(R.string.busySpace) + f.format(memSize.getBusyMemory(GlobalEnum.MEMORY.GO)) + " Go");
        tvVamsilla.setText(getString(R.string.vamsillaSpace) + f.format(memSize.getVamsillaMemory(GlobalEnum.MEMORY.GO)) + " Go");
        tvNbFiles.setText(getString(R.string.nFiles) + memSize.getNbFiles());
	}
	
	
	/**
	 * Traitement d'un appel au menu d'option
	 */
	public boolean onCreateOptionsMenu(Menu menu)
	{
		//Remplissage de la vue du menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu_list, menu);
		return true;
	}
    
	/**
	 * Traitement d'un clic sur un item du menu d'option
	 */
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			//Clic sur pr�f�rences
			case R.id.prefs : 
				Intent iMenu = new Intent(getApplicationContext(), OptionMenuActivity.class);
				startActivity(iMenu);break;
			//Sauvegarde de la bdd
			case R.id.dbDump :
				Log.v("TestDump","coucou");
				VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
				
				while(!db.open()) {
		                		try {
									Thread.sleep(GlobalEnum.MISC.DB_REFRESH);
								} catch (InterruptedException e) {
									Log.v("InterruptedException",e.toString());
								}
		                	}

				
				//Sauvegarde de la BDD
				try {
					db.dumpTable(getApplicationContext());
				} catch (IOException e) {
					Toast.makeText(this, "Erreur d'�criture...", Toast.LENGTH_SHORT).show();
				}

				db.close();
				
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				 
		        alertDialog.setTitle("Suppression");
		 
		        //Confirmation de la suppression
		        alertDialog.setMessage("Voulez-vous �galement supprimer le contenu de la base de donn�es ?");
		 
		       	//Oui
		        alertDialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
		        	/**
		        	 * R�cup�ration du clic sur un �l�ment
		        	 */
		            public void onClick(DialogInterface dialog,int which) {
		            	/**
		            	 * Clic sur oui
		            	 */
		            	VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
		            	
							while(!db.open()) {
		                		try {
									Thread.sleep(GlobalEnum.MISC.DB_REFRESH);
								} catch (InterruptedException e) {
									Log.v("InterruptedException",e.toString());
								}
		                	}
		        		db.emptyEvents();
		        		db.close();
		            }
		        });
		 
		        //Non
		        alertDialog.setNegativeButton("NON", new DialogInterface.OnClickListener() {
		        	/**
		        	 * Clic sur non
		        	 */
		            public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		            }
		        });
				
		        alertDialog.show();
				break;
			default : return false;
		}
		
		return true;
	}
}
    