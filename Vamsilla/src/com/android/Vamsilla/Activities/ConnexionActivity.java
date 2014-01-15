/***********************************************************
*	Fichier : ConnexionActivity.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 17/06/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe g�rant la vue connexion, dans laquelle il est demand�
*	� l'utilisateur un SSID et une cl� pour se connecter � un 
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
	private static final int DB_REFRESH = 400; //Fr�quence des demandes d'acc�s � la base de donn�es au cas o� elle soit bloqu�e
	
	@Override
	/**
	 * Proc�dure de cr�ation de la vue.
	 * 
	 * @param savedInstanceState : si l'activit� est r�initialis�e apr�s avoir �t� 
	 * 								arr�t�e, les donn�es sont ici.
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
	 * Copie du ssid dans le champ cl�
	 * @param v
	 */
	public void copy(View v)
	{
		key.setText(ssid.getText().toString());
	}


	/**
	 * M�thode appell�e lors d'un clic.
	 * 
	 * 
	 * @param v : source du clic.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onClick(View v) {
		
		//Si l'utilisateur a cliqu� sur le bouton valider, on v�rifie si les champs sont remplis
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
	 * M�thode appell�e lors d'un clic sur la checkbox. Rend le texte visible
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
