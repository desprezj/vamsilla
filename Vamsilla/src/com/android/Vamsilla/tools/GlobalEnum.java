/***********************************************************
*	Fichier : GlobalEnum.java
*	Auteurs : Thomas JAN MAHAMAD
*	Date de Cr�ation : 05/12/2012
*	Date de Modification : 19/12/2012
*	Derni�re r�vision : Thomas JAN MAHAMAD
*	Version : 1.0
*
*	Classe regroupant des variables partag�es entre les classes.
*
*	VAMSILLA v6 2012
***************************************************************/

package com.android.Vamsilla.tools;


/**
 *  Classe regroupant des valeurs fr�quentes
 *
 */
public final class GlobalEnum {
	
	/**
	 * Classe regroupant les chaines relatives au fichier de pr�f�rences partag�es
	 *
	 */
	public class PREFS {
		public static final String NAME = "VamsillaPrefs";
		public static final String PATH = "vamsillaPath";
		public static final String IP_SERVER = "ipServer";
		public static final String LOGIN_FTP = "loginFtp";
		public static final String PASSWORD_FTP = "passwordFtp";
		public static final String DEFAULT_PATH = "/vamsillaPath";
		
		private PREFS() {}
	}
	
	/**
	 * Classe regroupant les chaines relatives � la base de donn�es
	 *
	 */
	public class DB {
		//Chemin de sauvegarde de la BDD
		public static final String BASE_DUMP_PATH ="/dbDump";
		
		public static final String DOWNLOAD = "Telechargement";
		public static final String DOWNLOAD_BODY = "Demande de t�l�chargement de : ";
		public static final String DOWNLOAD_BODY_SUCCESS = "Succ�s du t�l�chargement de : ";
		public static final String DOWNLOAD_BODY_FAIL = "Echec du t�l�chargement de : ";
		
		public static final String DELETE = "Suppression";
		public static final String DELETE_BODY = "Demande de suppression de : ";
		public static final String DELETE_BODY_SUCCESS = "Succ�s de suppression de : ";
		public static final String DELETE_BODY_FAIL = "Echec de suppression de : ";
		
		public static final String INTEGRITY = "Integrite";
		public static final String INTEGRITY_BODY_SUCCESS = "Succ�s du contr�le d'int�grit� de : ";
		public static final String INTEGRITY_BODY_FAIL = "Echec du contr�le d'int�grit� de : ";
		
		public static final String UNTAR = "Decompression";
		public static final String UNTAR_BODY_SUCCESS = "Succ�s de la d�compressoin de : ";
		public static final String UNTAR_BODY_FAIL = "Echec de la d�compressoin de : ";
		
		public static final String CONNEXION = "Connection";
		public static final String CONNEXION_BODY = "SSID : ";
		
		public static final int DB_VERSION = 3;
		public static final String DB_NAME = "VamsillaEvent.db";
	
		private DB() {}
	}
	
	/**
	 * Chaines relatives aux t�l�chargements
	 *
	 */
	public class DOWNLOAD {
		public static final String FILE_LIST = "fileList";
		public static final int PORT = 22;
		public static final String FILE_SIZE = "fileSize";
		public static final String FILE_NAME ="fileName";
		public static final String GLOBAL_PROGRESS_DATA_LOAD = "globalProgressDataLoad";
		public static final String PROGRESS_DATA_LOAD = "progressDataLoad";
		public static final String TOTAL_FILES_SIZE = "totalFilesSize";
		public static final String DOWNLOAD_ERROR = "downloadError";
	
		private DOWNLOAD() {}
	}
	
	/**
	 * Informations relatives � la gestion de la m�moire
	 */
	public class MEMORY {
		public static final double GO = 1073741824.0;
		public static final double MO = 1048576.0;
		public static final double BYTES = 1.0;
		
		private MEMORY() {}
	}
	
	
	/**
	 * Informations diverses
	 */
	
	public class MISC {
		public static final String UNTAR_RESULT = "UntarResult";
		public static final int DB_REFRESH = 400;
		
		private MISC() {}
	}
	
	/**
	 *  Constructeur priv�, la classe n'a pas � �tre instanci�e 
	 */
	private GlobalEnum()
	{}
	
}