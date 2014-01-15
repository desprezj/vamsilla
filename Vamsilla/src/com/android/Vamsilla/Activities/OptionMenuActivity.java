/***********************************************************
*	Fichier : DownloadSFTPService.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 08/12/2012
*	Date de Modification : 10/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Service g�rant la connexion et la demande de t�l�chargement au serveur sFTP
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
	 * Cr�ation du menu
	 */
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.option_menu);
	    getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
	    //R�cup�ration des vues
		vamsillaPathEdit = (EditText)findViewById(R.id.pathEdit);
		ipServerEdit = (EditText)findViewById(R.id.ipServerEdit);
		loginFtpEdit = (EditText)findViewById(R.id.loginFtpEdit);
		passwordFtpEdit = (EditText)findViewById(R.id.passwordFtpEdit);		
		
		//R�cup�ration du fichier de  pr�f�rences partag�es
		vamsillaPrefs = this.getSharedPreferences(GlobalEnum.PREFS.NAME, MODE_PRIVATE);
		
		//R�cup�ration des valeurs des pr�f�rences partag�es
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

		//Instantiation de l'�diteur de pr�f�rences
		SharedPreferences.Editor prefEdit = vamsillaPrefs.edit();
		
		//Si le chemin vamsilla a �t� modifi�, on �dite la pr�f�rence
		if(!vamsillaPathEdit.getText().toString().equals(vamsillaPath)) {
			prefEdit.putString(GlobalEnum.PREFS.PATH, vamsillaPathEdit.getText().toString());
		}

		//Si le l'ip du serveur a �t� modifi�e, on �dite la pr�f�rence
		if(!ipServerEdit.getText().toString().equals(ipServer)) {
			prefEdit.putString(GlobalEnum.PREFS.IP_SERVER, ipServerEdit.getText().toString());
		}

		//Si le login du ftp a �t� modifi�, on �dite la pr�f�rence
		if(!loginFtpEdit.getText().toString().equals(loginFtp)) {
			prefEdit.putString(GlobalEnum.PREFS.LOGIN_FTP, loginFtpEdit.getText().toString());
		}

		//Si le mot de passe du ftp a �t� modifi�, on �dite la pr�f�rence
		if(!passwordFtpEdit.getText().toString().equals(passwordFtp)) {
			prefEdit.putString(GlobalEnum.PREFS.PASSWORD_FTP, passwordFtpEdit.getText().toString());	
		}
		
		//Commit des changements dans les pr�f�rences
		if(prefEdit.commit()) {
			Toast.makeText(getBaseContext(), "Pr�f�rences chang�es avec succ�s", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getBaseContext(), "Echec du changement des pr�f�rences", Toast.LENGTH_SHORT).show();
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
