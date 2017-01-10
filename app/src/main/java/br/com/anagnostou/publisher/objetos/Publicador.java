package br.com.anagnostou.publisher.objetos;

/**
 * Created by George on 26/07/2016.
 */
public class Publicador  {
    private static int ativos;
    private String nome;
    private String familia;
    private String grupo;
    private String batismo;
    private String nascimento;
    private String fone;
    private String celular;
    private String rua;
    private String bairro;
    private String ansepu;
    private String pipu;
    private String sexo;

    public Publicador() {
        ativos++;
    }

    public Publicador(String nome, String familia, String grupo, String batismo,
                      String nascimento, String fone, String celular, String rua, String bairro,
                      String ansepu, String pipu, String sexo ) {
        this.nome = nome;
        this.familia = familia;
        this.grupo = grupo;
        this.batismo = batismo;
        this.nascimento = nascimento;
        this.fone = fone;
        this.celular = celular;
        this.rua = rua;
        this.bairro = bairro;
        this.ansepu = ansepu;
        this.pipu = pipu;
        this.sexo = sexo;
    }

    /**
     "nome": "Arisvaldo",
     "familia": "Arisvaldo Souza",
     "grupo": "Adriano",
     "databatismo": "1995-09-16",
     "datanascimento": "1974-04-12",
     "fone": "014-981.48.24.26",
     "celular": "014-981.48.24.26",
     "rua": "Rua Roque Pinson, 40",
     "bairro": "Chacara Represa",
     "ASP": "Servo",
     "PP": "Pioneiro",
     "sexo": "M"



     */

    public Publicador(String what){
        String snome;
        String sfamilia;
        String sgrupo;
        String sbatismo;
        String snascimento;
        String sfone;
        String scelular;
        String srua;
        String sbairro;
        String sansepu;
        String spipu;
        String ssexo;
        //'\uFEFF' 65279
        // Remove the leading BOM characters.
        //what = what.replaceAll("\\uFEFF", "");

        //utfFEFF =  what.substring(0, what.indexOf(";"));
        what = getRidOfUTF(what);

        try {
            snome = what.substring(0, what.indexOf(";"));
        } catch (Exception e) {
            //L.m("UTF problems");
            snome = "Desconhecido";
        }

        what = what.substring(what.indexOf(";") + 1, what.length());

        sfamilia = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        sgrupo = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        sbatismo = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        snascimento = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        sfone = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        scelular = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        srua = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        sbairro = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        sansepu = (what.substring(0, what.indexOf(";")));
        what = what.substring(what.indexOf(";") + 1, what.length());

        spipu = (what.substring(0, what.indexOf(";")));
        ssexo = what.substring(what.indexOf(";") + 1, what.length());

        this.nome = snome;
        this.familia = sfamilia;
        this.grupo = sgrupo;
        this.batismo = sbatismo;
        this.nascimento = snascimento;
        this.fone =sfone;
        this.celular = scelular;
        this.rua = srua;
        this.bairro = sbairro;
        this.ansepu = sansepu;
        this.pipu = spipu;
        this.sexo = ssexo;
        ativos++;
    }

    public String getRidOfUTF(String str){
        char[] charArray = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : charArray){
            if (c != '\uFEFF') {
                sb.append(c);
                //L.m("UTF \uFEFF");
            }
        }
        return sb.toString();
    }

    public static int getAtivos() {
        return ativos;
    }

    public static void setAtivos(int ativos) {
        Publicador.ativos = ativos;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFamilia() {
        return familia;
    }

    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getBatismo() {
        return batismo;
    }

    public void setBatismo(String batismo) {
        this.batismo = batismo;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public String getFone() {
        return fone;
    }

    public void setFone(String fone) {
        this.fone = fone;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getAnsepu() {
        return ansepu;
    }

    public void setAnsepu(String ansepu) {
        this.ansepu = ansepu;
    }

    public String getPipu() {
        return pipu;
    }

    public void setPipu(String pipu) {
        this.pipu = pipu;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
}

