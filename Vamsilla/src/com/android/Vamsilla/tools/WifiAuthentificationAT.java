/***********************************************************
*	Fichier : WifiAuthentificationAT.java
*	Auteurs : Thomas JAN MAHAMAD
*			  Augustin BESSETTE
*	Date de Cr�ation : 19/11/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	AsyncTask g�rant la connexion wifi
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.tools;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.Vamsilla.Activities.DownloadActivity;

/**
 * Class permettant l'authentification au r�seau wifi sp�cifi� dans le champ SSID
 * si la connexion est effectu�, on passe � l'activit� "DownloadActivity"
 * 
 * 
 *@see android.app.Activity#onCreate(android.os.Bundle)
 */
public class WifiAuthentificationAT extends AsyncTask<Void, Void, Boolean> {
		
	private String ssid;
	private String key;
	private Context applicationContext;
	private NetworkInfo mWifi;
	private WifiManager wifiManager;
	private ConnectivityManager connManager;
	
	private String parentClass;
	
	//Dur�e de mise en pause de la Thread
	private int timeSleep = 500;
	//Nombre de test de connexion
	private int nbrTestConnexion = 30;
	//connexion existant initialis� � false
	private boolean existingConnection = false;
	
/**
 *  Constructeur de l'activit�
 * @param ssid SSID du r�seau
 * @param key Cl� du r�seau
 * @param applicationContext Contexte d'execution
 * @param parentClass Classe appelante
 */
	public WifiAuthentificationAT(String ssid, String key, Context applicationContext, String parentClass){
		super();
		this.ssid = "\"" + ssid + "\"";
		this.key = key;
		this.applicationContext = applicationContext;
		this.parentClass = parentClass;
	}
	
	/**
	 * Traitement avant execution de l'activit�
	 */
	protected void onPreExecute(){ 
		//initialisation des variables permettants de surveiller l'�tat de connexion de la carte wifi
		connManager = (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		//initialisation de la variable permettant de surveiller l'�tat de la carte wifi (enabled, disabled, enabling)
		wifiManager = (WifiManager)applicationContext.getSystemService(Context.WIFI_SERVICE);
		
		//activation du WIFI (si non activ�)
		if(!wifiManager.isWifiEnabled())
		{	
			wifiManager.setWifiEnabled(true);
			Toast.makeText(applicationContext,"Activation du Wifi",Toast.LENGTH_LONG).show();
		}
		else{
			//v�rification d'une connexion pr�existante � un r�seau
			if(mWifi.isConnected()){
				WifiInfo currentWifiInfo = wifiManager.getConnectionInfo();
				
				//cas ou le p�riph�rique est connect� au r�seau sp�cifi� dans le champ SSID
				if(currentWifiInfo.getSSID().equals(ssid)){
					existingConnection = true;
				}
			}
		}
	}
	
	/**
	 * Traitement des op�rations r�alis�es par l'activit�
	 */
    protected Boolean doInBackground(Void... arg0) {
    	
    	//si le p�riph�rique n'est pas connect� au r�seau sp�cifi� dans le SSID 
    	if(!existingConnection)
    	{
	    	//on attend que le wifi soit activ� avant de commenc� l'authentification au r�seau 
			while(!wifiManager.isWifiEnabled()){
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					Log.e("InterruptedException",e.toString());
				}	
			}	
	    	
	    	//R�cup�ration de la liste des r�seaux wifi configur�s
	    	List<WifiConfiguration> accessPointsPhone = wifiManager.getConfiguredNetworks();
			
	    	//Point d'acc�s d�j� configur�
			boolean isAccessPointRegistred = false;
			
			//num�ro du wifi configur�
			int configuredApId = 0;
			
			//V�rification de l'existance d'une configuration r�seau (� fin d'�viter la cr�ation d'une nouvelle configuration r�seau � chaque fois)
			for(int j=0; !isAccessPointRegistred && j < accessPointsPhone.size(); j++)
			{
				WifiConfiguration aph = accessPointsPhone.get(j);
				
				if(aph.SSID.equals(ssid))
				{	
					isAccessPointRegistred = true;					
					configuredApId = aph.networkId;
				}	
			}
			
			//Cas o� l'utilisateur se connecte pour la premi�re fois au r�seau (o� la configuration du r�seau sp�cifi� n'est pas d�j� enregistr�)
			if(!isAccessPointRegistred)
			{
				
				WifiConfiguration config = new WifiConfiguration();
				
				config.SSID = ssid;
				config.priority = 1;
				config.preSharedKey = "\"" + key + "\"";
				
				config.status = WifiConfiguration.Status.ENABLED;
				
				configuredApId = wifiManager.addNetwork(config);
			}	
			
			//lancement de la connection au point d'acc�s
			wifiManager.enableNetwork(configuredApId, true);
		
			//Testes de v�rification de la connexion au r�seau | Test effectu� "nbrTestConnexion" avant l'affichage d'un message d'erreur 
			for(int i=0; i<nbrTestConnexion && !mWifi.isConnected(); i++){
				try {						 
					Thread.sleep(timeSleep);
					mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				}catch (InterruptedException e) {
					Log.e("InterruptedException",e.toString());
				}
			}
	    	
    	}	
		
    	return mWifi.isConnected();
    }

    /**
     * Traitement apr�s execution de l'activit�
     */
    protected void onPostExecute(Boolean isConnected) {
    	
    	//Dans le cas o� le p�riph�rique est connect� au r�seau sp�cifi� | information retourn� par la m�thode doInBackground()
    	//passage � l'activit� "DownloadActivity"
    	if(isConnected){
			Toast.makeText(applicationContext,"Connexion r�ussie",Toast.LENGTH_SHORT).show();
			
			//Dans le cas o� la class parent est ConnectionActivity, on lance la page de s�lection des fichiers � t�l�charger
			if(parentClass.equals("ConnectionActivity")){
				Intent iConnect=new Intent(applicationContext,DownloadActivity.class);
				iConnect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				applicationContext.startActivity(iConnect);
			}
		}
		else{
			Toast.makeText(applicationContext,"Echec de la connexion",Toast.LENGTH_LONG).show();
		}
    }
}
