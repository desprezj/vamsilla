/***********************************************************
*	Fichier : DownloadSFTPService.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Création : 08/12/2012
*	Date de Modification : 10/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Service gérant la connexion et la demande de téléchargement au serveur sFTP
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.Activities;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.Vamsilla.tools.GlobalEnum;

/**
 * Classe OptionMenuActivity
 */
public class OptionMenuActivity extends Activity {

	private EditText vamsillaPathEdit;
	private EditText ipServerEdit;
	private EditText loginFtpEdit;
	private EditText passwordFtpEdit;
	private String vamsillaPath;
	private String ipServer;
	private String loginFtp;
	private String passwordFtp;
	private SharedPreferences vamsillaPrefs;
	
	
	@Override
	/**
	 * Création du menu
	 */
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.option_menu);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
	    //Récupération des vues
		vamsillaPathEdit = (EditText)findViewById(R.id.pathEdit);
		ipServerEdit = (EditText)findViewById(R.id.ipServerEdit);
		loginFtpEdit = (EditText)findViewById(R.id.loginFtpEdit);
		passwordFtpEdit = (EditText)findViewById(R.id.passwordFtpEdit);		
		
		//Récupération du fichier de  préférences partagées
		vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
		
		//Récupération des valeurs des préférences partagées
		vamsillaPath = vamsillaPrefs.getString(GlobalEnum.PREFS.PATH, "/vamsilla");
		ipServer = vamsillaPrefs.getString(GlobalEnum.PREFS.IP_SERVER, "192.168.92.45");
		loginFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.LOGIN_FTP, "roger");
		passwordFtp = vamsillaPrefs.getString(GlobalEnum.PREFS.PASSWORD_FTP, "roger");
		
		//Initialisation des EditText
		vamsillaPathEdit.setText(vamsillaPath);
		ipServerEdit.setText(ipServer);
		loginFtpEdit.setText(loginFtp);
		passwordFtpEdit.setText(passwordFtp);
		
		super.onCreate(savedInstanceState);
	}
	
	/**
	 *  Clic de l'utilisateur sur le bouton valider 
	 * @param v
	 */
	public void valid(View v)
	{

		//Instantiation de l'éditeur de préférences
		SharedPreferences.Editor prefEdit = vamsillaPrefs.edit();
		
		//Si le chemin vamsilla a été modifié, on édite la préférence
		if(!vamsillaPathEdit.getText().toString().equals(vamsillaPath)) {
			prefEdit.putString(GlobalEnum.PREFS.PATH, vamsillaPathEdit.getText().toString());
		}

		//Si le l'ip du serveur a été modifiée, on édite la préférence
		if(!ipServerEdit.getText().toString().equals(ipServer)) {
			prefEdit.putString(GlobalEnum.PREFS.IP_SERVER, ipServerEdit.getText().toString());
		}

		//Si le login du ftp a été modifié, on édite la préférence
		if(!loginFtpEdit.getText().toString().equals(loginFtp)) {
			prefEdit.putString(GlobalEnum.PREFS.LOGIN_FTP, loginFtpEdit.getText().toString());
		}

		//Si le mot de passe du ftp a été modifié, on édite la préférence
		if(!passwordFtpEdit.getText().toString().equals(passwordFtp)) {
			prefEdit.putString(GlobalEnum.PREFS.PASSWORD_FTP, passwordFtpEdit.getText().toString());	
		}
		
		//Commit des changements dans les préférences
		if(prefEdit.commit()) {
			Toast.makeText(getBaseContext(), "Préférences changées avec succès", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getBaseContext(), "Echec du changement des préférences", Toast.LENGTH_SHORT).show();
		}
			
		this.finish();
	}
	
	/**
	 * Annulation des modifications
	 * @param v
	 */
	public void cancel(View v)
	{
		this.finish();
	}
	
	
}
