/***********************************************************
*	Fichier : MemorySize.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 08/10/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe g�rant l'espace m�moire sur la carte sd
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
	 * M�thode permettant de calculer la place libre d'un dossier
	 * 
	 * 
	 * @return Total : m�moire totale sur le syst�me de fichier externe
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
	 *  Renvoie la m�moire libre sur la carte m�moire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * @return espace libre dans l'unit� demand�e
	 */
	public double getFreeMemory(double returnType)
	{
		return free/returnType;
	}
	
	/**
	 *  Renvoie la m�moire totale sur la carte m�moire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace total dans l'unit� demand�e
	 */
	public double getTotalMemory(double returnType)
	{
		return total/returnType;
	}

	/**
	 *  Renvoie la m�moire occup�e sur la carte m�moire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace occup� dans l'unit� demand�e
	 */
	public double getBusyMemory(double returnType)
	{
		return busy/returnType;
	}
	
	/**
	 *  Renvoie la m�moire occup�e par vamsilla sur la carte m�moire
	 * @param returnType : GlobalEnum.MEMORY.BYTES -> renvoie la taille en bytes
	 * 					   GlobalEnum.MEMORY.MO -> renvoie la taille en Mo
	 * 					   GlobalEnum.MEMORY.GO -> renvoie la taille en Go
	 * 
	 * @return espace occup�e par vamsilla dans l'unit� demand�e
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
