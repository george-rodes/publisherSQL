package br.com.anagnostou.publisher.objetos;

import java.util.Calendar;
import java.util.GregorianCalendar;

import br.com.anagnostou.publisher.utils.L;

import static br.com.anagnostou.publisher.utils.Utilidades.modulo;

/**
 * Created by George on 07/05/2017.
 */

public class DozeMeses {

    private String anoIni;
    private String anoFim;
    private String mesIni;
    private String mesFim;

    public DozeMeses() {
        int anoIni, anoFim, mesIni, mesFim;

        Calendar cal = new GregorianCalendar();
        cal.setTime(Calendar.getInstance().getTime());

        mesFim = cal.get(Calendar.MONTH) + 1; //mes atual
        anoFim = cal.get(Calendar.YEAR); // ano atual

        mesIni = mesFim;
        anoIni = anoFim - 1;

        //L.m(anoIni + " - " + mesIni +" - " + anoFim +" - " + mesFim);
        this.anoIni = "" + anoIni;
        this.anoFim = "" + anoFim;
        this.mesIni = "" + mesIni;
        this.mesFim = "" + mesFim;
    }

    public String getAnoIni() {
        return anoIni;
    }

    public String getAnoFim() {
        return anoFim;
    }

    public String getMesIni() {
        return mesIni;
    }

    public String getMesFim() {
        return mesFim;
    }

}
