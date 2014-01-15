/***********************************************************
*	Fichier : VamsillaEvent.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 01/12/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe représentant un évènement à stocker dans la BDD
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
	 * 	Constructeur d'un évènement à insérer dans la bdd
	 * @param type Type d'évènement 
	 * @param body Corps de l'évènement
	 * @param size Taille du fichier 
	 * @param user Utilisateur de la requête
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
	 *  Constructeur d'un évènement 
	 * @param id id dans la ligne
	 * @param date date d'évènement
	 * @param type Type d'évènement 
	 * @param body Corps de l'évènement
	 * @param size Taille du fichier 
	 * @param user Utilisateur de la requête
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
	 * Méthode permettant d'afficher la ligne, notamment dans les dump
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
