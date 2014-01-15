/***********************************************************
*	Fichier : WifiAuthentificationAT.java
*	Auteurs : Thomas JAN MAHAMAD
*			  Augustin BESSETTE
*	Date de Création : 19/11/2012
*	Date de Modification : 19/12/2012
*	Dernière révision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	AsyncTask gérant la connexion wifi
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
 * Class permettant l'authentification au réseau wifi spécifié dans le champ SSID
 * si la connexion est effectué, on passe à l'activité "DownloadActivity"
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
	
	//Durée de mise en pause de la Thread
	private int timeSleep = 500;
	//Nombre de test de connexion
	private int nbrTestConnexion = 30;
	//connexion existant initialisé à false
	private boolean existingConnection = false;
	
/**
 *  Constructeur de l'activité
 * @param ssid SSID du réseau
 * @param key Clé du réseau
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
	 * Traitement avant execution de l'activité
	 */
	protected void onPreExecute(){ 
		//initialisation des variables permettants de surveiller l'état de connexion de la carte wifi
		connManager = (ConnectivityManager)applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		//initialisation de la variable permettant de surveiller l'état de la carte wifi (enabled, disabled, enabling)
		wifiManager = (WifiManager)applicationContext.getSystemService(Context.WIFI_SERVICE);
		
		//activation du WIFI (si non activé)
		if(!wifiManager.isWifiEnabled())
		{	
			wifiManager.setWifiEnabled(true);
			Toast.makeText(applicationContext,"Activation du Wifi",Toast.LENGTH_LONG).show();
		}
		else{
			//vérification d'une connexion préexistante à un réseau
			if(mWifi.isConnected()){
				WifiInfo currentWifiInfo = wifiManager.getConnectionInfo();
				
				//cas ou le périphérique est connecté au réseau spécifié dans le champ SSID
				if(currentWifiInfo.getSSID().equals(ssid)){
					existingConnection = true;
				}
			}
		}
	}
	
	/**
	 * Traitement des opérations réalisées par l'activité
	 */
    protected Boolean doInBackground(Void... arg0) {
    	
    	//si le périphérique n'est pas connecté au réseau spécifié dans le SSID 
    	if(!existingConnection)
    	{
	    	//on attend que le wifi soit activé avant de commencé l'authentification au réseau 
			while(!wifiManager.isWifiEnabled()){
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					Log.e("InterruptedException",e.toString());
				}	
			}	
	    	
	    	//Récupération de la liste des réseaux wifi configurés
	    	List<WifiConfiguration> accessPointsPhone = wifiManager.getConfiguredNetworks();
			
	    	//Point d'accès déjà configuré
			boolean isAccessPointRegistred = false;
			
			//numéro du wifi configuré
			int configuredApId = 0;
			
			//Vérification de l'existance d'une configuration réseau (à fin d'éviter la création d'une nouvelle configuration réseau à chaque fois)
			for(int j=0; !isAccessPointRegistred && j < accessPointsPhone.size(); j++)
			{
				WifiConfiguration aph = accessPointsPhone.get(j);
				
				if(aph.SSID.equals(ssid))
				{	
					isAccessPointRegistred = true;					
					configuredApId = aph.networkId;
				}	
			}
			
			//Cas où l'utilisateur se connecte pour la première fois au réseau (où la configuration du réseau spécifié n'est pas déjà enregistré)
			if(!isAccessPointRegistred)
			{
				
				WifiConfiguration config = new WifiConfiguration();
				
				config.SSID = ssid;
				config.priority = 1;
				config.preSharedKey = "\"" + key + "\"";
				
				config.status = WifiConfiguration.Status.ENABLED;
				
				configuredApId = wifiManager.addNetwork(config);
			}	
			
			//lancement de la connection au point d'accès
			wifiManager.enableNetwork(configuredApId, true);
		
			//Testes de vérification de la connexion au réseau | Test effectué "nbrTestConnexion" avant l'affichage d'un message d'erreur 
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
     * Traitement après execution de l'activité
     */
    protected void onPostExecute(Boolean isConnected) {
    	
    	//Dans le cas où le périphérique est connecté au réseau spécifié | information retourné par la méthode doInBackground()
    	//passage à l'activité "DownloadActivity"
    	if(isConnected){
			Toast.makeText(applicationContext,"Connexion réussie",Toast.LENGTH_SHORT).show();
			
			//Dans le cas où la class parent est ConnectionActivity, on lance la page de sélection des fichiers à télécharger
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
