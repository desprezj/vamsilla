/***********************************************************
*	Fichier : RunningDownloadsActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*			  Augustin BESSETTE 
*	Date de Création : 26/06/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Activité affichant la liste des téléchargements en cours
*
*	VAMSI v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.db.VamsillaFileDB;
import com.android.Vamsilla.tools.GlobalEnum;
import com.android.Vamsilla.tools.VamsillaTools;

/**
 * Classe RunningDownloadsActivity
 */
public class RunningDownloadsActivity extends ListActivity {

	private ListView listView;
	private ArrayAdapter<String> adapter;
    private List<String> fileList=new ArrayList<String>();
    private DownloadReceiverRD downloadFileReceiverRD;
    private Button listButton;
    private LinearLayout pbLayout;
    private static final int N0 = 100; //Rafraichissement pour interblocage de la DB
    
	
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
		setContentView(R.layout.running_downloads);
		
		//Récupération automatique de la liste (l'id de cette liste est nommé obligatoirement @android:id/list afin d'être détecté)
		listView = getListView();
		
		View footerView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.running_downloads_footer, null, true);
		listView.addFooterView(footerView);
		
		//Utilisation de notre adaptateur qui se chargera de placer les valeurs de notre liste automatiquement
		//le tableau de String "fileList" est vide au départ, et est mise à jour grace aux informations reçu par l'IntentService traité par le BroadcastReceiver 
		if(fileList.isEmpty())
		{
			pbLayout = (LinearLayout) findViewById(R.id.runningDownloadsPB);
			pbLayout.setVisibility(View.GONE);
		}
		adapter = new ArrayAdapter<String>(this,R.layout.running_downloads_row,R.id.name,fileList);
		// On attribue à notre listView l'adaptateur que l'on vient de créer
		listView.setAdapter(adapter);
		
		
		//mise en place du BroadCastReceiver pour mettre à jour la barre de progression 		
		IntentFilter filter = new IntentFilter(DownloadReceiverRD.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        downloadFileReceiverRD = new DownloadReceiverRD();
        registerReceiver(downloadFileReceiverRD, filter);		
        
		listButton = (Button) this.findViewById(R.id.listButton);
		listButton.setOnClickListener(new OnClickListener(){
			/**
			 * Ecoute du clic sur le bouton
			 */
			public void onClick(View v) {
				Intent iFileListAcitivty = new Intent(getApplicationContext(),FileListActivity.class);
				startActivity(iFileListAcitivty);
			}       
        });

	}
	
	
	/**
	 * on supprime le BroadcastReceiver lorsque l'activité est détruite lorsqu'elle n'est plus au premier plan
	 * (cela évite les erreurs, et optmise les ressources de l'application (à quoi bon mettre à jour une barre de progression qui n'est plus à l'écran)
	 */
	protected void onDestroy(){
	    super.onDestroy();
		unregisterReceiver(downloadFileReceiverRD);
	}
	
	
	
	/**
	 * Mise à jour de la barre de progression
	 *
	 */
	public class DownloadReceiverRD extends BroadcastReceiver{
		 
        public static final String PROCESS_RESPONSE = "com.android.Vamsilla.Activities.intent.action.PROCESS_RESPONSE";
        public static final String TAG = "RunningDownloadsActivity";
        private int iProgressBarNbrFile = 0;
        private TextView tProgressBarNbrFile;
 
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	//récupération des informations sur le fichier en cours de téléchargement
        	String downloadError = intent.getStringExtra(GlobalEnum.DOWNLOAD.DOWNLOAD_ERROR);
            long progress = intent.getLongExtra(GlobalEnum.DOWNLOAD.PROGRESS_DATA_LOAD,-1);
            float globalProgress = intent.getFloatExtra(GlobalEnum.DOWNLOAD.GLOBAL_PROGRESS_DATA_LOAD,-1);
            double fileSize = intent.getDoubleExtra(GlobalEnum.DOWNLOAD.FILE_SIZE,-1);
            ArrayList<String> downloadList = intent.getStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST); 
            String fileName = intent.getStringExtra(GlobalEnum.DOWNLOAD.FILE_NAME);
            int nbrFiles = downloadList.size();
                        
            if(downloadError == null){
	            //initialisation du numéro de fichier à mettre à jour dans la liste des téléchargement
	            int listFileNum = -1;
	            
	            if(nbrFiles>0){
	            	
		            //cas où la ListView est vide, ou que le fichier en cours de téléchargement ne fais pas encore parti de la liste
	            	for(String tempFileName : downloadList){
	            		if(!fileList.contains(tempFileName)){
	            			//on ajout le nom du fichier à la liste (cette liste étant attaché à l'adapter de la ListView 
	    	            	fileList.add(tempFileName);
	    	            	//on signale à l'adapter que le tableau de String "fileList" a été mis à jour
	    	        		adapter.notifyDataSetChanged();
	            		}
	            	}
		            	
	            	//le fichier appartient déjà à la listView, on récupère son numéro 
	            	listFileNum = fileList.indexOf(fileName);
	            	
	            	 //initialisation du nombre de fichier total du champ TextView 
	            	 if(listFileNum == 0){
	            		 tProgressBarNbrFile = (TextView)findViewById(R.id.progressBarNbrFile);
	            		 tProgressBarNbrFile.setText(getString(R.string.pbNbrFile) + iProgressBarNbrFile + "/" + nbrFiles);
	            	 }
	            	 
	            	// vérification de la présence de l'Item dans la liste (pour éviter les "NullPointerExceptions"
	            	if(listFileNum !=-1 && listView.getChildAt(listFileNum) != null && listView.getChildAt(listFileNum).findViewById(R.id.progress) != null){
	            		
	            		//permettra de formater la progression en centième
	            		DecimalFormat f = new DecimalFormat();
	                    f.setMaximumFractionDigits(2);
	            		
	            		//barer de progression globale
	            		ProgressBar globalProgressBar = (ProgressBar)findViewById(R.id.globalProgress);
	            		
	            		
	            		//progression du téléchargement du fichier courant en %
	            		double currentFileDownloadProgress =  (progress/fileSize) * N0;
	            		
	            		//mise à jour de la barre de progression du fichier
		            	ProgressBar downloadProgressBar = (ProgressBar) listView.getChildAt(listFileNum).findViewById(R.id.progress);
		                downloadProgressBar.setProgress((int)currentFileDownloadProgress + 1);
		                
		                //mise à jour du texte affichant la progression %
		                TextView fileProgress = (TextView) listView.getChildAt(listFileNum).findViewById(R.id.fileProgress);
		                fileProgress.setText(f.format(currentFileDownloadProgress) + "%");	
		                
		                //mise à jour de la barre de progression globale
	                	globalProgressBar.setProgress((int) globalProgress);
	                	
		                //Lorsque le téléchargement est fini, on cache la barre de progression
		                if(progress == fileSize){
		                	
		                	iProgressBarNbrFile++;
		                	
		                	tProgressBarNbrFile = (TextView)findViewById(R.id.progressBarNbrFile);
		                	tProgressBarNbrFile.setText(getString(R.string.pbNbrFile) + iProgressBarNbrFile +"/" + nbrFiles);
		                
		                	if(iProgressBarNbrFile == nbrFiles){
		                		globalProgressBar.setProgress(N0);
		                	}
		                	
		                	downloadProgressBar.setVisibility(View.INVISIBLE);
		                			            	
		                	VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
		                	while(!db.open()) {
		                		try {
									Thread.sleep(400);
								} catch (InterruptedException e) {
									Log.v("InterruptedException",e.toString());
								}
		                	}
		                	db.insertEvent(new VamsillaEvent("Telechargement","Téléchargement de : " + fileName +" réussi", fileSize, "system"));
		                	db.close();
		                	md5Sum(fileName);
		            		Toast.makeText(getApplicationContext(),"Téléchargement du fichier " + fileName + " réussi.",Toast.LENGTH_LONG).show();
	            		}
	            	}
		            
	            }
            }else{
            	Log.v(TAG, "une erreur est survenu lors du téléchargement : " + downloadError);
            }
        }
        
        private void md5Sum(String currentFileName) {
    		VamsillaTools tools = new VamsillaTools(getApplicationContext());
    		
    		//Ouverture de la base de stockage des fichiers
    		VamsillaFileDB db = new VamsillaFileDB(getApplicationContext());
    		
    		//Attente de l'accès à la BDD
    		try {
    			while(!db.open()){
    				try {
    					Thread.sleep(200);
    				} catch (InterruptedException e) {
    					Log.v("Erreur", "Thread error");
    				}
    			}
    		} catch (Exception e) {
    			
    			Toast.makeText(getApplicationContext(), "Impossible d'ecrire dans la DB", Toast.LENGTH_SHORT).show();
    		}
    		
    		//Insertion du résultat dans la BDD
    		int result = tools.checkMD5(currentFileName);
    		db.insertFile(currentFileName, result);
    		switch(result) {
    			case -1 : Log.v("MD5", "Problème lors de la comparaison md5 : " + currentFileName); break;
    			case 0 : Log.v("MD5", "Comparaison md5 fausse : " + currentFileName); break;
    			case 1 : Log.v("MD5", "Succès de la comparaison md5 pour : " + currentFileName); break;
    		}
    		db.close();
        }
    }
}
