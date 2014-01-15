/***********************************************************
*	Fichier : FileListAdapter.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 13/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Adapter personnalis� pour la liste de fichiers g�rant les couleurs
*
*	Vert : Succ�s de la comparaison md5
*	Rouge : Echec de la comparaison md5
*	Bleu : Erreur de lecture du md5 / md5 non pr�sent sur le serveur
*	Gris : pas d'information relative dans la BDD
*	
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.Vamsilla.db.VamsillaFileDB;

/**
 * Classe FileListAdapter
 */
public class FileListAdapter extends ArrayAdapter<String> {

	private List<String> items;
	private Map<String, Integer> itemState;
	private Context context;
	
	
	/**
	 *  Constructeur de l'adapter 
	 * @param context 			 Context permettant de lier la classe � l'application
	 * @param textViewResourceId Ressource (layout) � remplir via l'inflater
	 * @param items 			 Liste de fichiers
	 */
	public FileListAdapter(Context context,	int textViewResourceId, List<String> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		
		//Ouverture de la BDD
		VamsillaFileDB db = new VamsillaFileDB(context.getApplicationContext());
		
		//Attente d'un acc�s � la BDD
		while(!db.open())
		{
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Log.e("Erreur", "Erreur de thread");
			}
			
		}
		
		//R�cup�ration des fichiers en liste
		itemState = db.getFiles();
		
		//Fermeture de la BDD
		db.close();
	}
	
	/**
	 * M�thode permettant � l'adapter de remplir la listview
	 */
	public View getView(int position, View convertView, ViewGroup parent ) {
		
		View v = convertView;

		//Nom du fichier courant
		String currentItem = items.get(position);
		
		//Si la vue n'a pas � �tre recycl�e, on la cr�e 
		if(v == null) {
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.list_row, null);
		}		
		//Sinon on recycle la vue
		if(currentItem != null) {
			//R�cup�ration du champs filename
			TextView tn = (TextView) v.findViewById(R.id.fileName);
			
			//Si il existe
			if(tn != null) {
				
				//On attribue le nom du fichier
				tn.setText(currentItem);
			
				//Si la liste de fichiers dans la BDD existe et qu'elle contient une entr�e relative au fichier,
				if(itemState != null && itemState.containsKey(currentItem)) {
					//On met la couleur adapt�e 
					switch(itemState.get(currentItem)) {
						case -1 : tn.setTextColor(context.getResources().getColor(R.color.errorState));break;
						case 0 : tn.setTextColor(context.getResources().getColor(R.color.failState));break;
						case 1 : tn.setTextColor(context.getResources().getColor(R.color.successState));break;
					}
					//Sinon le fichier n'est pas contenu dans la BDD, 
				} else {
					tn.setTextColor(context.getResources().getColor(R.color.unknowState));
				}
			}
		}
		
		return v;
	}
	
}