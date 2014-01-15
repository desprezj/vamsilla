/***********************************************************
*	Fichier : MemorySize.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 08/10/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe gérant l'espace mémoire sur la carte sd
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.tools;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;

/**
 * Classe MemorySize
 */
public class MemorySize {
	
	private double total;
	private double free;
	private double busy;
	private double busyVamsilla;
	private String vamsillaPath;
	private int nbFiles;
	
	/**
	 * Constructeur
	 * 
	 * 
	 */
	public MemorySize(Context context)
	{
	        SharedPreferences vamsillaPrefs = context.getSharedPreferences(GlobalEnum.PREFS.NAME, Context.MODE_PRIVATE);
	        
			vamsillaPath =  vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
			init();	
	}
	
	/**
	 * Initialisation de l'objet
	 * 
	 * 
	 */
	private void init()
	{
		
		nbFiles = 0;
		
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		total = statFs.getBlockCount();
		total *= statFs.getBlockSize();
		
		free = statFs.getAvailableBlocks();
		free *= statFs.getBlockSize();
		
		busy = total - free;
		
		busyVamsilla = dirSize(new File(Environment.getExternalStorageDirectory() + vamsillaPath));
		
	}
	
	/**
	 * Méthode permettant de calculer la place libre d'un dossier
	 * 
	 * 
	 * @return Total : mémoire totale sur le système de fichier externe
	 * @see android.os.statFs
	 */
	private double dirSize(File dir)
    {
            if (dir.exists()) {
                long result = 0;
                File[] fileList = dir.listFiles();
                for(int i = 0; i < fileList.length; i++) {
                    // Recursive call if it's a directory
                    if(fileList[i].isDirectory()) {
                        result += dirSize(fileList [i]);
                    } else {
                        // Sum the file size in bytes
                        result += fileList[i].length();
                        nbFiles++;
                    }
                }
                return result; // return the file size
            }
            return 0;
    }
	
	/**
	 *  Renvoie la mémoire libre sur la carte mémoire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * @return espace libre dans l'unité demandée
	 */
	public double getFreeMemory(double returnType)
	{
		return free/returnType;
	}
	
	/**
	 *  Renvoie la mémoire totale sur la carte mémoire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace total dans l'unité demandée
	 */
	public double getTotalMemory(double returnType)
	{
		return total/returnType;
	}

	/**
	 *  Renvoie la mémoire occupée sur la carte mémoire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace occupé dans l'unité demandée
	 */
	public double getBusyMemory(double returnType)
	{
		return busy/returnType;
	}
	
	/**
	 *  Renvoie la mémoire occupée par vamsilla sur la carte mémoire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace occupée par vamsilla dans l'unité demandée
	 */
	public double getVamsillaMemory(double returnType)
	{
		return busyVamsilla/returnType;
	}
	
	
	/**
	 * Accesseur au nombre de fichiers dans le dossier vamsilla
	 * 
	 * @return double total (en Go)
	 */
	public int getNbFiles()
	{
		return nbFiles;
	}
}
