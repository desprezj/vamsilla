/***********************************************************
*	Fichier : ConnexionActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 17/06/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe gérant la vue connexion, dans laquelle il est demandé
*	à l'utilisateur un SSID et une clé pour se connecter à un 
*	serveur.
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;


import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.Vamsilla.db.VamsillaEvent;
import com.android.Vamsilla.db.VamsillaEventDB;
import com.android.Vamsilla.tools.WifiAuthentificationAT;

/**
 * Classe ConnexionActivity
 *
 */
public class ConnexionActivity extends Activity implements OnClickListener {

	private EditText ssid;
	private EditText key;
	private Button validButton;
	private static final int DB_REFRESH = 400; //Fréquence des demandes d'accès à la base de données au cas où elle soit bloquée
	
	@Override
	/**
	 * Procédure de création de la vue.
	 * 
	 * @param savedInstanceState : si l'activité est réinitialisée après avoir été 
	 * 								arrêtée, les données sont ici.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connexion);
		
		ssid=(EditText)findViewById(R.id.SSID);
		key=(EditText)findViewById(R.id.pass);
		validButton=(Button)findViewById(R.id.bValConnect);
		validButton.setOnClickListener(this);
		
	}
	
	/**
	 * Copie du ssid dans le champ clé
	 * @param v
	 */
	public void copy(View v)
	{
		key.setText(ssid.getText().toString());
	}


	/**
	 * Méthode appellée lors d'un clic.
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onClick(View v) {
		
		//Si l'utilisateur a cliqué sur le bouton valider, on vérifie si les champs sont remplis
		if(v==validButton)
		{
			//Si il n'y a pas de SSID
			if(ssid.getText().toString().isEmpty())
			{
				Toast.makeText(this,"Veuillez entrer un SSID",Toast.LENGTH_SHORT).show();
			}
			//Si il n'y a pas de mot de passe
			else {
				if(key.getText().toString().isEmpty()) {
					Toast.makeText(this,"Veuillez entrer un mot de passe",Toast.LENGTH_SHORT).show();
				}
				else {
					//Journalisation de la connexion
					VamsillaEventDB db = new VamsillaEventDB(getBaseContext());
					while(!db.open()) {
						try {
							Thread.sleep(DB_REFRESH);
						} catch (InterruptedException e) {
							Log.v("InterruptedException",e.toString());
						}
					}
					db.insertEvent(new VamsillaEvent("Connexion","SSID : " + ssid.getText().toString(),0,"user"));
					db.close();
					new WifiAuthentificationAT(ssid.getText().toString(), key.getText().toString(), getApplicationContext(), "ConnectionActivity").execute();
				}
			}
		}
	}
	
	
	/**
	 * Méthode appellée lors d'un clic sur la checkbox. Rend le texte visible
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void showKey(View v){
		
		CheckBox cb = (CheckBox) v;
		
		if(cb.isChecked()) {
			key.setInputType(InputType.TYPE_CLASS_TEXT);
		} else {
			key.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}
	}
}
