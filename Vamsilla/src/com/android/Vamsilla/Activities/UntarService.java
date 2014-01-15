/***********************************************************
*	Fichier : UntarService.java
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.widget.Toast;

import com.android.Vamsilla.Activities.FileListActivity.FileReceiverRD;
import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe UntarService
 */
public class UntarService extends IntentService {

	private Intent broadcastIntentRD;
	private String vamsillaPath;

	/**
	 * Constructeur par d�faut
	 */
	public UntarService()
	{
		super("com.anddroid.Vamsilla.Activities.UntarTool");
	}
	
	/**
	 * Constructeur avec le nom
	 * @param name nom � transmettre au constructeur
	 */
	public UntarService(String name) {
		super(name);
	}

	@Override
	/**
	 * M�thode appel�e � l'appel de l'intent
	 */
	protected void onHandleIntent(Intent intent) {
		
		//R�cup�ration des informations transmises
		vamsillaPath = intent.getStringExtra(GlobalEnum.PREFS.PATH);
		String fileName = intent.getStringExtra(GlobalEnum.DOWNLOAD.FILE_NAME);
		
		try {
			unTar(fileName);	
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Fichier introuvable", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, "Erreur d'E/S", Toast.LENGTH_SHORT).show();
		} 
		finally {
			//Envoi de l'information � FileListActivity afin de fermer la bo�te de dialogue
			broadcastIntentRD = new Intent();
			broadcastIntentRD.setAction(FileReceiverRD.UNTAR_RESPONSE);
			broadcastIntentRD.addCategory(Intent.CATEGORY_DEFAULT);           
	        broadcastIntentRD.putExtra(GlobalEnum.MISC.UNTAR_RESULT, true);
	        sendBroadcast(broadcastIntentRD);
		}
	}
	
	/**
	 *  D�compression d'un fichier 
	 * @param fileName nom du fichier
	 * @throws IOException,  
	 */
	public boolean unTar(String fileName) throws IOException
	{
		//Dossier de destination
		
		String path = Environment.getExternalStorageDirectory() + vamsillaPath ;
		TarInputStream tin = null;
		//Si c'est une archive
		if(fileName.toLowerCase().endsWith("tar")) {
			tin = new TarInputStream(new FileInputStream(new File(path + "/" + fileName)));
		}
		//Si c'est une archive compress�e
		else {
			if(fileName.toLowerCase().endsWith("gz") || fileName.toLowerCase().endsWith("gzip")) {
				tin = new TarInputStream(new BufferedInputStream(new GZIPInputStream( new FileInputStream( new File(path + "/" + fileName)))));
			}
		}
		
		//Tant qu'il reste un fichier a extraire dans l'archive
		TarEntry tarEntry = tin.getNextEntry();		
		while(tarEntry != null)
		{
			//Fichier de destination
			File destPath = new File(path + "/" + tarEntry.getName());
			//Si c'est un r�pertoire on le cr�e
			if(tarEntry.isDirectory()) {
				destPath.mkdir();
			} else {
				//Si le fichier est dans un r�pertoire inexistant, on le cr�e (fonctionnement r�cursif)
				if(!destPath.getParentFile().exists()) {
					destPath.mkdir();
				}
				
				//Ouverture d'un flux de sortie
				FileOutputStream fout = new FileOutputStream(destPath);
				
				//Copie du fichier
				tin.copyEntryContents(fout);
			}
			tarEntry = tin.getNextEntry();
		}
		//Si le flux d'entr�e existe, on le ferme
		if(tin !=null)
		{
			tin.close();
		}
		
		return true;
	}
	
}
