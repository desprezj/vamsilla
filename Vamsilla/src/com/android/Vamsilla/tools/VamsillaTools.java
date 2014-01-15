/***********************************************************
*	Fichier : VamsillaTools.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 10/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe d'utilitaires	
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

/**
 * Classe VamsillaTools
 */
public class VamsillaTools {

	private String vamsillaPath;
	/**
	 *  Constructeur de vamsillatools
	 * @param context contexte de l'application
	 */
	public VamsillaTools(Context context)
	{
		SharedPreferences prefs = context.getSharedPreferences(GlobalEnum.PREFS.NAME, Context.MODE_PRIVATE);
		vamsillaPath = prefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
	}
	
	/**
	 * V�rification par comparaison MD5 d'un fichier
	 * @param fileName
	 * @return -1 : erreur de comparaison
	 * 			0 : comparaison fausse
	 * 			1 : comparaison r�ussie
	 */
	public int checkMD5(String fileName)
	{
		//Empreinte md5 t�l�charg�e sur le serveur
		String downloadedMD5 = null;
		//Empreinte md5 g�n�r�e localement
		String localMD5 = null;
		
		
		try {
			localMD5 = createMD5(fileName);
			downloadedMD5 = readMD5(fileName);
		} catch (NoSuchAlgorithmException e) {
			Log.e("TestMD5", "MD5 non impl�ment� : " + e.toString());
			return -1;
		} catch (FileNotFoundException e) {
			Log.e("TestMD5", "Fichier non trouv� : " + e.toString());
			return -1;
		} catch (IOException e) {
			Log.e("TestMD5", "Erreur d'E/S dans le fichier t�l�charg� : " + e.toString());
			return -1;
		}
		
		if(downloadedMD5 != null && localMD5 != null) {
			//Comparaison des empreintes
			if(localMD5.equals(downloadedMD5)) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}
	
	/**
	 *  Lecture du md5 t�l�charg� sur le serveur 
	 *  
	 * @param fileName Nom du fichier t�l�charg�
	 * @return Cha�ne contenant le md5
	 * @throws IOException Erreur d'E/Ss
	 * @throws FileNotFoundException Fichier non trouv�
	 */
	public String readMD5(String fileName) throws IOException
	{
		//R�cup�ration de l'emplacement de l'extension du fichier
		int lastDot = fileName.lastIndexOf(".");
		
		String md5path;
		
		//Si c'est .tar.gz il faut recommencer la r�cup�ration de l'extension pour avoir le nom de base
		if(fileName.endsWith("tar.gz"))
		{
			String md5TempPath = fileName.substring(0, lastDot);
			md5path = md5TempPath.substring(0, md5TempPath.lastIndexOf("."));
		} else {
			md5path = fileName.substring(0, lastDot);
		}
		
		//Ouverture du fichier contenant le hash MD5
		File sum = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + md5path + ".md5");
		
		BufferedReader md5Reader = new BufferedReader(new FileReader(sum));
		
		String md5Local = md5Reader.readLine();
		
		md5Reader.close();
		
		return md5Local;		
		
	}
	
	/**
	 *  Cr�ation du md5 de l'empreinte MD5 du fichier t�l�charg�
	 * @param fileName	nom du fichier
	 * @return Chaine contenant le md5
	 * @throws IOException erreur d'E/S
	 * @throws NoSuchAlgorithmException MD5 non impl�ment� 
	 */
	public String createMD5(String fileName) throws IOException, NoSuchAlgorithmException
	{
		//Ouverture de l'instance MD5 (renvoie NoSuchAlgorithmException)
		MessageDigest digest = MessageDigest.getInstance("MD5");
		
		//Ouverture du fichier t�l�charg�
		File f = new File(Environment.getExternalStorageDirectory() + vamsillaPath + "/" + fileName);
		
		//Ouverture du flux d'entr�e pour lire le fichier
		InputStream is = new FileInputStream(f);
		
		byte[] buffer = new byte[8192];
		int read = 0;
		
		//Tant qu'il y a des bytes � lire dans le fichier, on les ajoute au MessageDigest
		while((read = is.read(buffer)) > 0 ) {
			digest.update(buffer, 0, read);
		}
		
		//Digestion du message, et conversion en chaine lisible
		byte[] md5sum = digest.digest();
		BigInteger bigInt = new BigInteger(1, md5sum);
		String md5Sum = bigInt.toString(16);
		
		is.close();
		return md5Sum;
	}
	
	
	
}
