/***********************************************************
*	Fichier : DownloadSFTPService.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 08/12/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Service gérant la connexion et la demande de téléchargement au serveur sFTP
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.Vamsilla.Activities.RunningDownloadsActivity.DownloadReceiverRD;
import com.android.Vamsilla.tools.GlobalEnum;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Classe DownloadSFTPService
 */
public class DownloadSFTPService extends IntentService implements SftpProgressMonitor {

	

	private String vamsillaPath;
	private List<String> downloadList;
	
	private Notification nDownloadProgress;
	private NotificationManager notificationManager;
	private RemoteViews contentView;
	private Intent broadcastIntentRD;	
	private int notificationId = 1024;
	private long lastTime;
	private long refresh = 500;
	private int percent = 100;
	
	private double totalFileSize;
	private String currentFileName;
	private float totalFilesBytesTransferred = 0;
	private double currentFileSize;
	private long currentDownload;
	private ChannelSftp sftpChannel;
	
	private DecimalFormat f;

	
	/**
	 * Constructeur de la classe
	 */
	public DownloadSFTPService() {
		super("downloadSFTPService");
		
		//Mise en forme du format d'affichage du pourcentage 
		f = new DecimalFormat();
		f.setMaximumFractionDigits(2);
	}

	@Override
	/**
	 * méthode appelée lors du lancement du service
	 * 
	 * @param intent : Intent permettant de passer les paramètres depuis l'appelant
	 */
	protected void onHandleIntent(Intent intent) {
		
		String loginFtp;
		String passwordFtp;
		String ipServer;
		
		//Récupération des informations passées depuis l'appelant
		vamsillaPath = intent.getStringExtra(GlobalEnum.PREFS.PATH);
		loginFtp = intent.getStringExtra(GlobalEnum.PREFS.LOGIN_FTP);
		passwordFtp = intent.getStringExtra(GlobalEnum.PREFS.PASSWORD_FTP);
		ipServer = intent.getStringExtra(GlobalEnum.PREFS.IP_SERVER);
		
		downloadList = intent.getStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST);
		totalFileSize = intent.getDoubleExtra(GlobalEnum.DOWNLOAD.TOTAL_FILES_SIZE, 0);
				
		//Création de la session avec le serveur
		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(loginFtp, ipServer, GlobalEnum.DOWNLOAD.PORT);
			if(session == null)
			{
				Log.v("TestSSH","Impossible d'instancier la session");
			}
			else
			{

				//Configuration de la connexion par mot de passe plutot que certificat
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				config.put("PreferredAuthentifications", "password");
				session.setConfig(config);
				
				//Récupération du mot de passe dans les préférences et ouverture de session avec le serveur
				session.setPassword(passwordFtp);
				session.connect();
				
				//Configuration du canal en mode sFTP et ouverture
				Channel channel = session.openChannel("sftp");
				sftpChannel = (ChannelSftp) channel;
				sftpChannel.connect();
				
				//Initialisation de la notification
				initialiseNotif();
				
				//Lancement du téléchargement des fichiers
				downloadFiles();
				
				//Clôture de la connexion
				sftpChannel.disconnect();
				
			}
		} catch (JSchException e) {
			Log.e("JschException", e.toString());
		} catch (SftpException e) {
			Log.e("SftpException", e.toString());
		}
		
	}
	
	
	
	/**
	 * Initialisation de la notification de progression globale
	 * 
	 * 
	 */
	private void initialiseNotif()
	{
		Intent iNotif = new Intent(this, RunningDownloadsActivity.class);
		PendingIntent pNotif = PendingIntent.getActivity(this, 0, iNotif, 0);
		
		//Création de la notification
		nDownloadProgress = new Notification.Builder(getApplicationContext())
							.setContentTitle("Lancement du téléchargement...")
							.setContentText("Subject").setSmallIcon(R.drawable.ic_launcher)
							.setContentIntent(pNotif).getNotification();
		
		
		//L'utilisateur ne peut fermer la notification 
		notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		nDownloadProgress.flags = Notification.FLAG_NO_CLEAR;
		
		//Construction de la notification : icone, layout, ... 
		CharSequence title = "Lancement du téléchargement...";
		contentView = new RemoteViews(getPackageName(), R.layout.notification_download_progress);
		contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_launcher);
		contentView.setTextViewText(R.id.status_text, title);
		contentView.setProgressBar(R.id.notification_download_progress_bar, percent, 0, false);
		nDownloadProgress.contentView = contentView;
		nDownloadProgress.contentIntent = pNotif;
		
		notificationManager.notify(notificationId, nDownloadProgress);

		
		//setting BroadcastIntent | permettra d'informer l'activité "RunningDownloadsActivity" de la mise à jour de la barre de progression
        broadcastIntentRD = new Intent(); 
        broadcastIntentRD.setAction(DownloadReceiverRD.PROCESS_RESPONSE);
        broadcastIntentRD.addCategory(Intent.CATEGORY_DEFAULT);           
        broadcastIntentRD.putStringArrayListExtra(GlobalEnum.DOWNLOAD.FILE_LIST, (ArrayList<String>) downloadList);
		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * 	Parcours du vecteur de fichiers et téléchargement
	 * 
	 * @throws SftpException Les exceptions sont traitées dans le onHandleIntent
	 */
	private void downloadFiles() throws SftpException
	{
		Vector<ChannelSftp.LsEntry> file;
		
		//Pour chaque fichier
		for( String tempFileName : downloadList)
		{			
			if(fileExisting(tempFileName)) {
				//Récupération des infos fichier
				file = sftpChannel.ls(tempFileName);
				currentFileSize = file.get(0).getAttrs().getSize();
				currentFileName = file.get(0).getFilename();
				
				broadcastIntentRD.putExtra(GlobalEnum.DOWNLOAD.FILE_NAME, currentFileName);
				broadcastIntentRD.putExtra(GlobalEnum.DOWNLOAD.FILE_SIZE, currentFileSize);
				
				//Lancement du téléchargement 
				
				int lastDot = tempFileName.lastIndexOf(".");
				String baseFileName = null;
				if(tempFileName.endsWith("tar.gz"))
				{
					String temp = tempFileName.substring(0, lastDot);
					baseFileName = temp.substring(0, temp.lastIndexOf("."));
				} else {
					baseFileName = tempFileName.substring(0, lastDot );
				}
				
				try {
					sftpChannel.get(baseFileName + ".md5", Environment.getExternalStorageDirectory() + vamsillaPath + "/" + baseFileName + ".md5");
				} catch (SftpException e) {
					Log.v("Sftp", "md5 introuvable pour : " + currentFileName);
				}
				sftpChannel.get(file.get(0).getFilename(), Environment.getExternalStorageDirectory() + vamsillaPath + "/" + file.get(0).getFilename(),this);
			}
			
		}
	}
	
	/**
	 * Test si un fichier existe 
	 * @param fileName
	 * @return
	 */
	private boolean fileExisting(String fileName)
	{
		File file = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + fileName);
		if(file.exists()) {
			Toast.makeText(getApplicationContext(), "Le fichier : " + fileName + " existe déjà, il ne sera pas téléchargé", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Mise à jour de la notification
	 */
	private void updateNotification()
	{
		//calcul du progrès global
		float globalProgress = (float) ((totalFilesBytesTransferred) * percent / totalFileSize);
		
		//Insertion des informations pour transmission à RunningDownloads
		broadcastIntentRD.putExtra(GlobalEnum.DOWNLOAD.PROGRESS_DATA_LOAD,  currentDownload);
		broadcastIntentRD.putExtra(GlobalEnum.DOWNLOAD.GLOBAL_PROGRESS_DATA_LOAD,  globalProgress);
		sendBroadcast(broadcastIntentRD);
		
		//Mise à jour du layout de la notification

		long currentTime = System.currentTimeMillis();
		if((totalFilesBytesTransferred == totalFileSize) || (globalProgress > 99.8)) {
			
			contentView.setTextViewText(R.id.status_text, "Transfert terminé");
			contentView.setProgressBar(R.id.notification_download_progress_bar, percent, percent, false);
			nDownloadProgress.contentView = contentView;
			nDownloadProgress.flags = Notification.FLAG_ONGOING_EVENT;
			notificationManager.notify(notificationId, nDownloadProgress);
			
		} else {
			if((currentTime - lastTime) > refresh) {
			
				contentView.setTextViewText(R.id.status_text, currentFileName + ". Global : " + f.format(globalProgress) + "%");
				contentView.setProgressBar(R.id.notification_download_progress_bar, percent, (int) globalProgress, false);
				nDownloadProgress.contentView = contentView;
				notificationManager.notify(notificationId, nDownloadProgress);
		
				lastTime = currentTime;
			}
		}
	}

	/**
	 * Méthode appelée régulièrement par le progressmonitor 
	 * @param count : avancement depuis la dernière fois
	 */
	public boolean count(long count) {
		totalFilesBytesTransferred += count;
		currentDownload += count;
			updateNotification();
		
		return true;
	}

	/**
	 * Méthode appelée par le progressmonitor à la fin du téléchargement d'un fichier
	 */
	public void end() {
		
	}

	/**
	 * Initialisation du progressmonitor
	 */
	public void init(int op, String src, String dest, long max) {
		currentDownload = 0;
		lastTime = 0;
		
	}
	
	
	
		
}
	
	
		