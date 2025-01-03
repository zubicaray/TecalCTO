package org.tecal.scheduler;

public class TecalOrdoParams {
	private static volatile TecalOrdoParams instance;
	
	
	
	private int mTEMPS_ZONE_OVERLAP_MIN = CST.TEMPS_ZONE_OVERLAP_MIN;
	
	// temps incompresible d'un mouvement de pont	
	private int mTEMPS_MVT_PONT_MIN_JOB = CST.TEMPS_MVT_PONT_MIN_JOB;
	
	private int mGAP_ZONE_NOOVERLAP = CST.GAP_ZONE_NOOVERLAP;
	
	// temps autour d'un début de grosse zone		
	private int mTEMPS_MVT_PONT = CST.TEMPS_MVT_PONT;
	// temps de sécurité entre deux gammes différentes sur un même poste d'ano	
	private int mTEMPS_ANO_ENTRE_P1_P2 = CST.TEMPS_ANO_ENTRE_P1_P2;	
	
	private int mTEMPS_MAX_SOLVEUR = CST.TEMPS_MAX_SOLVEUR;	
	private int mNUMZONE_ANODISATION = CST.ANODISATION_NUMZONE;	
	private int mCAPACITE_ANODISATION = CST.CAPACITE_ANODISATION;
	

	
	

    public int getTEMPS_ZONE_OVERLAP_MIN() {
		return mTEMPS_ZONE_OVERLAP_MIN;
	}

	public void setTEMPS_ZONE_OVERLAP_MIN(int TEMPS_ZONE_OVERLAP_MIN) {
		this.mTEMPS_ZONE_OVERLAP_MIN = TEMPS_ZONE_OVERLAP_MIN;
	}

	public int getTEMPS_MVT_PONT_MIN_JOB() {
		return mTEMPS_MVT_PONT_MIN_JOB;
	}

	public void setTEMPS_MVT_PONT_MIN_JOB(int TEMPS_MVT_PONT_MIN_JOB) {
		this.mTEMPS_MVT_PONT_MIN_JOB = TEMPS_MVT_PONT_MIN_JOB;
	}

	public int getGAP_ZONE_NOOVERLAP() {
		return mGAP_ZONE_NOOVERLAP;
	}

	public void setGAP_ZONE_NOOVERLAP(int GAP_ZONE_NOOVERLAP) {
		this.mGAP_ZONE_NOOVERLAP = GAP_ZONE_NOOVERLAP;
	}

	public int getTEMPS_MVT_PONT() {
		return mTEMPS_MVT_PONT;
	}

	public void setTEMPS_MVT_PONT(int TEMPS_MVT_PONT) {
		this.mTEMPS_MVT_PONT = TEMPS_MVT_PONT;
	}

	public int getTEMPS_ANO_ENTRE_P1_P2() {
		return mTEMPS_ANO_ENTRE_P1_P2;
	}

	public void setTEMPS_ANO_ENTRE_P1_P2(int TEMPS_ANO_ENTRE_P1_P2) {
		this.mTEMPS_ANO_ENTRE_P1_P2 = TEMPS_ANO_ENTRE_P1_P2;
	}

	public int getTEMPS_MAX_SOLVEUR() {
		return mTEMPS_MAX_SOLVEUR;
	}

	public void setTEMPS_MAX_SOLVEUR(int TEMPS_MAX_SOLVEUR) {
		this.mTEMPS_MAX_SOLVEUR = TEMPS_MAX_SOLVEUR;
	}

	public int getNUMZONE_ANODISATION() {
		return mNUMZONE_ANODISATION;
	}

	public void setNUMZONE_ANODISATION(int NUMZONE_ANODISATION) {
		this.mNUMZONE_ANODISATION = NUMZONE_ANODISATION;
	}

	public int getCAPACITE_ANODISATION() {
		return mCAPACITE_ANODISATION;
	}

	public void setCAPACITE_ANODISATION(int CAPACITE_ANODISATION) {
		this.mCAPACITE_ANODISATION = CAPACITE_ANODISATION;
	}

	private TecalOrdoParams() {
        // Constructeur privé pour empêcher l'instanciation
    }

    public static TecalOrdoParams getInstance() {
        if (instance == null) {
            synchronized (TecalOrdoParams.class) {
                if (instance == null) {
                    instance = new TecalOrdoParams();
                }
            }
        }
        return instance;
    }
    public static void setParams() {
    	
    }

}
