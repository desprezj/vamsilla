/***********************************************************
*	Fichier : VamsillaFileDB.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 01/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe de gestion de la table de correspondance entre les 
*	fichiers et leur int�grit�
*
*	VAMSILLA v6 2012
***************************************************************/


package com.android.Vamsilla.db;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.Vamsilla.tools.GlobalEnum;

@SuppressWarnings("unused")
/**
 * Classe VamsillaFileDB
 */
public class VamsillaFileDB {
	
	private static final String FILES_TABLE = "FILES_TABLE";
	
	private static final String ROW_ID = "ID";
	private static final int N_ROW_ID = 0;
	private static final String ROW_NAME = "NAME";
	private static final int N_ROW_NAME = 1;
	private static final String ROW_STATE = "STATE";
	private static final int N_ROW_STATE = 2;
	
	private SQLiteDatabase db;
	
	private VamsillaDB vamsillaDB;
	
	/**
	 *  Constructeur de la classe de traitement de la table FilesEvent
	 * @param context 
	 */
	public VamsillaFileDB(Context context)
	{
		vamsillaDB = new VamsillaDB(context, GlobalEnum.DB.DB_NAME, null, GlobalEnum.DB.DB_VERSION);
	}

	/**
	 *  Ouverture de la bdd
	 * @return r�sultat de l'ouverture
	 */
	public boolean open()
	{
		db = vamsillaDB.getWritableDatabase();
		return !(db.isDbLockedByOtherThreads() || db.isDbLockedByCurrentThread());
	}
	
	/**
	 * Fermeture de la bdd
	 */
	public void close()
	{
		db.close();
	}
	
	/**
	 *  Accesseur � la BDD
	 * @return Connecteur � la BDD
	 */
	public SQLiteDatabase getDB()
	{
		return db;
	}
	
	/** Proc�dure d'insertion d'un �v�nement dans la BDD
	 *  
	 * @param event �v�nement � ins�rer
	 * @return id de la ligne ins�r�e, -1 si il n'y a pas eu insertion
	 */
	public long insertFile(String name, int state)
	{
		ContentValues values = new ContentValues();
		values.put(ROW_NAME, name);
		values.put(ROW_STATE, state);
		return db.insert(FILES_TABLE, null, values);
	}
	
	/**
	 *  Vide la table FILES_TABLE
	 */
	public void emptyEvents()
	{
		db.delete(FILES_TABLE, null, null);
	}
	
	/**
	 * 	Proc�dure de modification d'une ligne par son id
	 * @param id	ligne � modifier
	 * @param name nom de la ligne � modifier
	 * @parame state �tat de la ligne � modifier
	 * @return	nombre de lignes affect�es
	 */
	public long updateEvent(long id, String name, int state)
	{
		ContentValues values = new ContentValues();
		values.put(ROW_NAME, name);
		values.put(ROW_STATE, state);
		return db.update(FILES_TABLE, values, ROW_ID + " = " + id, null);
	}
	
	/**
	 * 	Destruction d'un �l�ment de la bd
	 * @param id	id de l'�l�ment � d�truire
	 * @return	nombre de lignes affect�es
	 */
	public long removeEventByID(long id)
	{
		return db.delete(FILES_TABLE, ROW_ID + " = " + id, null);
	}
	
	/**
	 * 	Destruction d'un �l�ment de la bd
	 * @param name	nom du fichier associ�
	 * @return	nombre de lignes affect�es
	 */
	public long removeEventByName(String name)
	{
		return db.delete(FILES_TABLE, ROW_NAME + " = " + name, null);
	}
	
	/**
	 * 		M�thode pour rattraper tous les �v�nements de la bdd
	 * @return	Map de fichiers
	 */
	public Map<String, Integer> getFiles()
	{
		String sqliteQuery = "SELECT * FROM " + FILES_TABLE;
		Cursor c = db.rawQuery(sqliteQuery, null);
		return cursorToMap(c);
	}
	
	/**
	 * 		Proc�dure pour rattraper tous les fichiers de la bdd par �tat
	 * @param state �tat des �l�ments � r�cup�rer
	 * @return	liste d'�v�nements 
	 */
	public Map<String, Integer> getFilesByState(int state)
	{
		String sqliteQuery = "SELECT * FROM " + FILES_TABLE + " WHERE " + ROW_STATE + " = " + state;
		Cursor c = db.rawQuery(sqliteQuery, null);
		return cursorToMap(c);
	}
	
	/**
	 * 	   Conversion d'un curseur en map de fichiers
	 * @param c curseur sur le r�sultat de la requ�te
	 * @return	liste d'�v�nements sous forme d'une HashMap
	 */
	private Map<String, Integer> cursorToMap(Cursor c)
	{
		Map<String, Integer> filesMap = new HashMap<String, Integer>();
		
		if(c.getCount() == 0) {
			return null;
		}
		
		while(c.moveToNext()) {
			filesMap.put(c.getString(N_ROW_NAME), c.getInt(N_ROW_STATE));
		}
		
		return filesMap;
	}
}
