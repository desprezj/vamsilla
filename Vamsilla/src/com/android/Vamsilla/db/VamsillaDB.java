/***********************************************************
*	Fichier : VamsillaDB.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 01/12/2012
*	Date de Modification : 10/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.1
*
*	Connecteur � la base de donn�es SQLite 
*	
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Classe VamsillaDB
 */
public class VamsillaDB extends SQLiteOpenHelper {
	
	//Informations pour la table d'�v�nements
	private static final String EVENT_TABLE = "EVENT_TABLE";
	private static final String FILES_TABLE = "FILES_TABLE";
	private static final String ROW_ID = "ID";
	private static final String ROW_DATE = "DATE";
	private static final String ROW_USER = "USER";
	private static final String ROW_TYPE = "TYPE";
	private static final String ROW_BODY = "BODY";
	private static final String ROW_SIZE = "SIZE";
	private static final String ROW_NAME = "NAME";
	private static final String ROW_STATE = "STATE";
	
	//Requ�te de cr�ation de la table d'�v�nements
	private static final String CREATE_EVENTS = "CREATE TABLE " + EVENT_TABLE + " (" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
																				 + ROW_DATE + " TEXT NOT NULL, "
																				 + ROW_USER + " TEXT ,"
																				 + ROW_TYPE + " TEXT NOT NULL, "
																				 + ROW_BODY + " TEXT NOT NULL, "
																				 + ROW_SIZE + " REAL);";
	//Requ�te de cr�ation de la table des fichiers
	private static final String CREATE_FILES = "CREATE TABLE " + FILES_TABLE + " (" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
																					+ ROW_NAME + " TEXT NOT NULL, "
																					+ ROW_STATE + " INTEGER);";
	
	/**
	 * 	Constructeur de l'adaptateur DB 
	 * @param context context appelant permettant de lier � l'application
	 * @param name nom de la BDD 
	 * @param factory 
	 * @param version Version de la BDD (m�thode onUpgrade appel�e lors de la modification)
	 */
	public VamsillaDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	/**
	 *  M�thode appel�e lors de la cr�ation de la BDD
	 */
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_EVENTS);
		db.execSQL(CREATE_FILES);
	}

	@Override
	/**
	 * M�thode appel�e lors du changement de version de la BDD 
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + EVENT_TABLE + " ;");
		onCreate(db);

	}

}
