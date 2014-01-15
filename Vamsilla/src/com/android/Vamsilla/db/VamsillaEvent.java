/***********************************************************
*	Fichier : VamsillaEvent.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 01/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe repr�sentant un �v�nement � stocker dans la BDD
*	
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.db;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.format.Time;

/**
 * Classe VamsillaEvent
 */
public class VamsillaEvent {
	
	private long id;
	private String date;
	private String type;
	private String body;
	private double size;
	private String user;
	
	/**
	 * Constructeur de l'objet
	 */
	public VamsillaEvent()
	{
		SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Date dateTemp = new Date();
		date = fmt.format(dateTemp);
	}
	
	/**
	 * 	Constructeur d'un �v�nement � ins�rer dans la bdd
	 * @param type Type d'�v�nement 
	 * @param body Corps de l'�v�nement
	 * @param size Taille du fichier 
	 * @param user Utilisateur de la requ�te
	 */
	public VamsillaEvent(String type, String body, double size, String user)
	{

		SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		Date dateTemp = new Date();
		date = fmt.format(dateTemp);
		this.type = type;
		this.body = body;
		this.size = size;
		this.user = user;		
	}
	
	/**
	 *  Constructeur d'un �v�nement 
	 * @param id id dans la ligne
	 * @param date date d'�v�nement
	 * @param type Type d'�v�nement 
	 * @param body Corps de l'�v�nement
	 * @param size Taille du fichier 
	 * @param user Utilisateur de la requ�te
	 */
	public VamsillaEvent(long id,String date,String type, String body, double size, String user)
	{
		Time t =  new Time();
		t.setToNow();
		this.id = id;
		this.date = date;
		this.type = type;
		this.body = body;
		this.size = size;
		this.user = user;		
	}
	
	/**
	 * M�thode permettant d'afficher la ligne, notamment dans les dump
	 */
	public String toString()
	{
		return "ID : " + id + " Date : " + date + "Utilisateur : " + user + " Type : " + type + " \n" + body + (size>0 ? size : "");
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	
}
