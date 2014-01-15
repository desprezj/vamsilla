/***********************************************************
*	Fichier : VamsillaEventDB.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 01/12/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe de gestion de la table évènements
*	
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe VamsillaEventDB
 */
public class VamsillaEventDB {
		
	private static final String EVENT_TABLE = "EVENT_TABLE";
	
	private static final String ROW_ID = "ID";
	private static final int N_ROW_ID = 0;
	private static final String ROW_DATE = "DATE";
	private static final int N_ROW_DATE = 1;
	private static final String ROW_USER = "USER";
	private static final int N_ROW_USER = 2;
	private static final String ROW_TYPE = "TYPE";
	private static final int N_ROW_TYPE = 3;
	private static final String ROW_BODY = "BODY";
	private static final int N_ROW_BODY = 4;
	private static final String ROW_SIZE = "SIZE";
	private static final int N_ROW_SIZE = 5;
	
	private SQLiteDatabase db;
	
	private VamsillaDB vamsillaDB;
	
	/**
	 *  Constructeur de la classe de traitement de la table VamsillaEvent
	 * @param context 
	 */
	public VamsillaEventDB(Context context)
	{
		vamsillaDB = new VamsillaDB(context, GlobalEnum.DB.DB_NAME, null, GlobalEnum.DB.DB_VERSION);
	}
	
	/**
	 *  Ouverture de la bdd
	 * @return résultat de l'ouverture
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
	 *  Accesseur à la BDD
	 * @return Connecteur à la BDD
	 */
	public SQLiteDatabase getDB()
	{
		return db;
	}
	
	/** Procédure d'insertion d'un évènement dans la BDD
	 *  
	 * @param event évènement à insérer
	 * @return id de la ligne insérée, -1 si il n'y a pas eu insertion
	 */
	public long insertEvent(VamsillaEvent event)
	{
		ContentValues values = new ContentValues();
		values.put(ROW_DATE, event.getDate());
		values.put(ROW_USER, event.getUser());
		values.put(ROW_TYPE, event.getType());
		values.put(ROW_BODY, event.getBody());
		values.put(ROW_SIZE, event.getSize());
		return db.insert(EVENT_TABLE, null, values);
		
	}
	
	/**
	 *  Vide la table VAMSILLA_EVENT
	 */
	public void emptyEvents()
	{
		db.delete(EVENT_TABLE, null, null);
	}
	
	/**
	 * 	Procédure de modification d'une ligne par son id
	 * @param id	ligne à modifier
	 * @param event	évènement à insérer
	 * @return	nombre de lignes affectées
	 */
	public long updateEvent(long id, VamsillaEvent event)
	{
		ContentValues values = new ContentValues();
		values.put(ROW_DATE, event.getDate());
		values.put(ROW_USER, event.getUser());
		values.put(ROW_TYPE, event.getType());
		values.put(ROW_BODY, event.getBody());
		values.put(ROW_SIZE, event.getSize());
		return db.update(EVENT_TABLE, values, ROW_ID + " = " + id, null);
	}
	
	/**
	 * 	Destruction d'un élément de la bd
	 * @param id	id de l'évènement à détruire
	 * @return	nombre de lignes affectées
	 */
	public long removeEventWithID(long id)
	{
		return db.delete(EVENT_TABLE, ROW_ID + " = "+ id, null);
	}
	
	/**
	 * 		Procédure pour rattraper tous les évènements de la bdd
	 * @return	liste d'évènements
	 */
	public List<VamsillaEvent> getEvents()
	{
		String sqliteQuery = "SELECT * FROM "+ EVENT_TABLE +" ORDER BY " + ROW_DATE +" DESC";
		Cursor c = db.rawQuery(sqliteQuery, null);
		return cursorToArray(c);
	}
	
	/**
	 * 		Procédure pour rattraper les évènements de la bdd par type
	 * @param type type d'évènement
	 * @return liste d'évènements
	 */
	public List<VamsillaEvent> getEventByType(String type)
	{
		String sqliteQuery = "SELECT * FROM "+ EVENT_TABLE +" WHERE " + ROW_TYPE + " = \"" + type + "\" ORDER BY " + ROW_DATE +" DESC";
		Cursor c = db.rawQuery(sqliteQuery, null);
		return cursorToArray(c);
	}
	
	/**
	 * 	   Conversion d'un curseur en liste d'évènements
	 * @param c curseur sur le résultat de la requête
	 * @return	liste d'évènements sous forme d'arraylist
	 */
	public List<VamsillaEvent> cursorToArray(Cursor c)
	{
		List<VamsillaEvent> eventList = new ArrayList<VamsillaEvent>();
		VamsillaEvent event;
		
		if(c.getCount() == 0) {
			return null;
		}
		
		while(c.moveToNext())
		{
			event = new VamsillaEvent(c.getLong(N_ROW_ID)
									  , c.getString(N_ROW_DATE)
									  , c.getString(N_ROW_TYPE)
									  , c.getString(N_ROW_BODY)
									  , c.getDouble(N_ROW_SIZE)
									  , c.getString(N_ROW_USER));
			eventList.add(event);
		}
		
		c.close();
		
		return eventList;
	}
	
	/**
	 *  Sauvegarde de la table VamsillaEvent
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public boolean dumpTable(Context context) throws IOException
	{
		
		//Récupération des évènments en base 
		List<VamsillaEvent> eventList = getEvents();
		
		//Si la table n'est pas vide
		if(!eventList.isEmpty()) {
			//Variable permettant de tester l'existence du fichier
			File dumpFileTest;
			
			//Récupération des préférences de l'utilisateur pour le chemin d'accès vamsilla
			SharedPreferences vamsillaPref = context.getSharedPreferences(GlobalEnum.PREFS.NAME, Context.MODE_PRIVATE);
			
			//Premier test sur le répertoire de dump
			String dbDumpDir = Environment.getExternalStorageDirectory() +
							    vamsillaPref.getString(GlobalEnum.PREFS.PATH, "/vamsilla") +
							    GlobalEnum.DB.BASE_DUMP_PATH;
			String dbDumpPath ;
			
			dumpFileTest = new File(dbDumpDir);
			//Si le répertoire n'existe pas
			if(!dumpFileTest.isDirectory() && !dumpFileTest.mkdir()) {
				//Si on ne peut le créer
					return false;
			}

			boolean fileExists = true;
			int nb = 0;

			PrintWriter dumpWriter;
			//Récupération de la date pour la création du fichier
			SimpleDateFormat fmt = new SimpleDateFormat("ddMMyy");
			Date dateTemp = new Date();
			String date = fmt.format(dateTemp);
			
			
			
			do
			{
				//Création du nom de fichier : dump_JJMMAA puis dump_JJMMAA-1 etc 
				if(nb == 0)
				{
					dbDumpPath = Environment.getExternalStorageDirectory() +
								  vamsillaPref.getString(GlobalEnum.PREFS.PATH, "/vamsilla") +
								  GlobalEnum.DB.BASE_DUMP_PATH +
								  "/dump_" +
								  date +
								   ".txt";
				}
				else
				{
					dbDumpPath = Environment.getExternalStorageDirectory() + 
					  vamsillaPref.getString(GlobalEnum.PREFS.PATH, "/vamsilla") +
					  GlobalEnum.DB.BASE_DUMP_PATH +
					  "/dump_" +
					  date +
					  "-"+ nb +
					   ".txt";
				}
				
				//Instanciation du fichier de dump
				dumpFileTest= new File(dbDumpPath);
				
				if(!dumpFileTest.exists()) {
					fileExists = false;
				}
				
				nb++;
			}while(fileExists);
			
			
			dumpWriter = new PrintWriter(new FileWriter(dbDumpPath));
			
			ListIterator<VamsillaEvent> it = eventList.listIterator();
			
			//ecriture des lignes dans le fichier. voir VamsillaEvent.toString() pour le format
			while(it.hasNext()) {
				dumpWriter.println(it.next().toString());
			}
			
			dumpWriter.close();
			
			return true;
		}
		//sinon la table est vide
		else
			return false;
	}
	
	
}
