/***********************************************************
*	Fichier : LogActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 01/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Activit� affichant la liste des �v�nements en bdd
*	
*
*	VAMSILLA v6 2012
***************************************************************/


package com.android.Vamsilla.Activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe LogActivity
 */
public class LogActivity extends ListActivity implements OnItemSelectedListener, OnItemLongClickListener {
	private ListView listView;
	private SimpleAdapter adapter;
	private List<HashMap<String, String>> eventMaps;
	private Spinner spinner;
	
	/**
	 * M�thode appel�e � la cr�ation de l'activit�
	 */
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.logger);
		
		listView=getListView();
		inflateList("Tous");
		
		spinner = (Spinner) findViewById(R.id.spinnerDBType);
		spinner.setOnItemSelectedListener(this);
		ArrayAdapter<?> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),  R.array.db_types, android.R.layout.simple_spinner_dropdown_item);
        // Sets the layout resource to create the drop down views
		
		spinner.setAdapter(spinnerAdapter);
		listView.setOnItemLongClickListener(this);

		
	}
	
	/**
	 * S�lection d'un item dans le spinner
	 */
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3)
	{
		inflateList(arg0.getItemAtPosition(pos).toString());
		
	}
	
	/** Proc�dure de remplissage de la listview
	 * 
	 */
	private void inflateList(String table)
	{	
		
		VamsillaEventDB db = new VamsillaEventDB(this);
		
		while(!db.open()) {
						try {
							Thread.sleep(GlobalEnum.MISC.DB_REFRESH);
						} catch (InterruptedException e) {
							Log.v("InterruptedException",e.toString());
						}
					}

		eventMaps = new ArrayList<HashMap<String, String>>();
		
		List<VamsillaEvent> eventList;

		//Si l'utilisateur a demand� tous les �v�nements 
		if(table.equals("Tous")) {
			eventList = db.getEvents();
		}
		//Sinon on s�l�ctionne un type
		else {
			eventList = db.getEventByType(table);
		}
		
		//Si la table contient des entr�es
		if(eventList != null && !eventList.isEmpty())
		{
			
			ListIterator<VamsillaEvent> it = eventList.listIterator();
			HashMap<String, String> map;
			VamsillaEvent event;
			
			//Cr�ation de la liste de map
			while(it.hasNext())
			{
				event = it.next();
				map = new HashMap<String, String>();
				
				//Initialisation des valeurs de la map
				map.put("date", event.getDate());
				map.put("type", event.getType());
				map.put("body", event.getBody());
				map.put("size", Double.toString(event.getSize()));
				map.put("user", event.getUser());
				map.put("ID", Long.toString(event.getId()));

				eventMaps.add(map);
			}
		}
		
		//Cr�ation de l'adepteur
		adapter = new SimpleAdapter(getBaseContext()
				, eventMaps
				, R.layout.log_row
				, new String[] {"date","type","body","ID"}
				, new int[] {R.id.date,R.id.type,R.id.body,R.id.rowId});
		db.close();
		
		//Mise � jour de la listview
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	

	/**
	 * Rien n'est s�l�ctionn�
	 */
	public void onNothingSelected(AdapterView<?> arg0) {
		inflateList(null);
		
	}

	/**
	 * Clic long de l'utilisateur sur un �v�nement
	 */
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		 
        alertDialog.setTitle("Confirmer la suppression");
 
        //Confirmation de la suppression
        alertDialog.setMessage("Voulez vous supprimer l'�v�nement ?");
 
       	//Oui
        alertDialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
        	/**
        	 * Clic sur le bouton oui
        	 */
            public void onClick(DialogInterface dialog,int which) {
        		VamsillaEventDB db = new VamsillaEventDB(getApplicationContext());
        		
				while(!db.open()) {
		                		try {
									Thread.sleep(GlobalEnum.MISC.DB_REFRESH);
								} catch (InterruptedException e) {
									Log.v("InterruptedException",e.toString());
								}
		                	}

        		db.removeEventWithID(Long.valueOf(eventMaps.get(position).get("ID")));
        		
        		inflateList(spinner.getSelectedItem().toString());
        		db.close();
            }
        });
 
        //Non
        alertDialog.setNegativeButton("NON", new DialogInterface.OnClickListener() {
        	/**
        	 * Clic sur le bouton non
        	 */
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
        
        alertDialog.show();
		
		return false;
	}
}
