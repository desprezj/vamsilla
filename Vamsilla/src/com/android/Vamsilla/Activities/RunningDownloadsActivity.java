/***********************************************************
*	Fichier : RunningDownloadsActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*			  Augustin BESSETTE 
*	Date de Cr�ation : 26/06/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Activit� affichant la liste des t�l�chargements en cours
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
	 /** Proc�dure de cr�ation de la vue.
	 * 
	 * 
	 * @param savedInstanceState : si l'activit� est r�initialis�e apr�s avoir �t� 
	 * 								arr�t�e, les donn�es sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.running_downloads);
		
		//R�cup�ration automatique de la liste (l'id de cette liste est nomm� obligatoirement @android:id/list afin d'�tre d�tect�)
		listView = getListView();
		
		View footerView = ((LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.running_downloads_footer, null, true);
		listView.addFooterView(footerView);
		
		//Utilisation de notre adaptateur qui se chargera de placer les valeurs de notre liste automatiquement
		//le tableau de String "fileList" est vide au d�part, et est mise � jour grace aux informations re�u par l'IntentService trait� par le BroadcastReceiver 
		if(fileList.isEmpty())
		{
			pbLayout = (LinearLayout) findViewById(R.id.runningDownloadsPB);
			pbLayout.setVisibility(View.GONE);
		}
		adapter = new ArrayAdapter<String>(this,R.layout.running_downloads_row,R.id.name,fileList);
		// On attribue � notre listView l'adaptateur que l'on vient de cr�er
		listView.setAdapter(adapter);
		
		
		//mise en place du BroadCastReceiver pour mettre � jour la barre de progression 		
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
	 * on supprime le BroadcastReceiver lorsque l'activit� est d�truite lorsqu'elle n'est plus au premier plan
	 * (cela �vite les erreurs, et optmise les ressources de l'application (� quoi bon mettre � jour une barre de progression qui n'est plus � l'�cran)
	 */
	protected void onDestroy(){
	    super.onDestroy();
		unregisterReceiver(downloadFileReceiverRD);
	}
	
	
	
	/**
	 * Mise � jour de la barre de progression
	 *
	 */
	public class DownloadReceiverRD extends BroadcastReceiver{
		 
        public static final String PROCESS_RESPONSE = "com.android.Vamsilla.Activities.intent.action.PROCESS_RESPONSE";
        public static final String TAG = "RunningDownloadsActivity";
        private int iProgressBarNbrFile = 0;
        private TextView tProgressBarNbrFile;
 
        @Override
        public void onReceive(Context context, Intent intent) {
        	
        	//r�cup�ration des informations sur le fichier en cours de t�l�chargement
        	String downloadError = intent.getStringExtra(GlobalEnum.DOWNLOAD.DOWNLOAD_ERROR);
            long progress = intent.getLongExtra(GlobalEnum.DOWNLOAD.PROGRESS_DATA_LOAD,-1);
            float globalProgress = intent.getFloatExtra(GlobalEnum.DOWNLOAD.GLOBAL_PROGRESS_DATA_LOAD,-1);
            double fileSize = intent.getDoubleExtra(GlobalEnum.DOWNLOAD.FILE_SIZE,-1);
            ArrayList<String> downloadList = intent.getStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST); 
            String fileName = intent.getStringExtra(GlobalEnum.DOWNLOAD.FILE_NAME);
            int nbrFiles = downloadList.size();
                        
            if(downloadError == null){
	            //initialisation du num�ro de fichier � mettre � jour dans la liste des t�l�chargement
	            int listFileNum = -1;
	            
	            if(nbrFiles>0){
	            	
		            //cas o� la ListView est vide, ou que le fichier en cours de t�l�chargement ne fais pas encore parti de la liste
	            	for(String tempFileName : downloadList){
	            		if(!fileList.contains(tempFileName)){
	            			//on ajout le nom du fichier � la liste (cette liste �tant attach� � l'adapter de la ListView 
	    	            	fileList.add(tempFileName);
	    	            	//on signale � l'adapter que le tableau de String "fileList" a �t� mis � jour
	    	        		adapter.notifyDataSetChanged();
	            		}
	            	}
		            	
	            	//le fichier appartient d�j� � la listView, on r�cup�re son num�ro 
	            	listFileNum = fileList.indexOf(fileName);
	            	
	            	 //initialisation du nombre de fichier total du champ TextView 
	            	 if(listFileNum == 0){
	            		 tProgressBarNbrFile = (TextView)findViewById(R.id.progressBarNbrFile);
	            		 tProgressBarNbrFile.setText(getString(R.string.pbNbrFile) + iProgressBarNbrFile + "/" + nbrFiles);
	            	 }
	            	 
	            	// v�rification de la pr�sence de l'Item dans la liste (pour �viter les "NullPointerExceptions"
	            	if(listFileNum !=-1 && listView.getChildAt(listFileNum) != null && listView.getChildAt(listFileNum).findViewById(R.id.progress) != null){
	            		
	            		//permettra de formater la progression en centi�me
	            		DecimalFormat f = new DecimalFormat();
	                    f.setMaximumFractionDigits(2);
	            		
	            		//barer de progression globale
	            		ProgressBar globalProgressBar = (ProgressBar)findViewById(R.id.globalProgress);
	            		
	            		
	            		//progression du t�l�chargement du fichier courant en %
	            		double currentFileDownloadProgress =  (progress/fileSize) * N0;
	            		
	            		//mise � jour de la barre de progression du fichier
		            	ProgressBar downloadProgressBar = (ProgressBar) listView.getChildAt(listFileNum).findViewById(R.id.progress);
		                downloadProgressBar.setProgress((int)currentFileDownloadProgress + 1);
		                
		                //mise � jour du texte affichant la progression %
		                TextView fileProgress = (TextView) listView.getChildAt(listFileNum).findViewById(R.id.fileProgress);
		                fileProgress.setText(f.format(currentFileDownloadProgress) + "%");	
		                
		                //mise � jour de la barre de progression globale
	                	globalProgressBar.setProgress((int) globalProgress);
	                	
		                //Lorsque le t�l�chargement est fini, on cache la barre de progression
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
		                	db.insertEvent(new VamsillaEvent("Telechargement","T�l�chargement de : " + fileName +" r�ussi", fileSize, "system"));
		                	db.close();
		                	md5Sum(fileName);
		            		Toast.makeText(getApplicationContext(),"T�l�chargement du fichier " + fileName + " r�ussi.",Toast.LENGTH_LONG).show();
	            		}
	            	}
		            
	            }
            }else{
            	Log.v(TAG, "une erreur est survenu lors du t�l�chargement : " + downloadError);
            }
        }
        
        private void md5Sum(String currentFileName) {
    		VamsillaTools tools = new VamsillaTools(getApplicationContext());
    		
    		//Ouverture de la base de stockage des fichiers
    		VamsillaFileDB db = new VamsillaFileDB(getApplicationContext());
    		
    		//Attente de l'acc�s � la BDD
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
    		
    		//Insertion du r�sultat dans la BDD
    		int result = tools.checkMD5(currentFileName);
    		db.insertFile(currentFileName, result);
    		switch(result) {
    			case -1 : Log.v("MD5", "Probl�me lors de la comparaison md5 : " + currentFileName); break;
    			case 0 : Log.v("MD5", "Comparaison md5 fausse : " + currentFileName); break;
    			case 1 : Log.v("MD5", "Succ�s de la comparaison md5 pour : " + currentFileName); break;
    		}
    		db.close();
        }
    }
}
