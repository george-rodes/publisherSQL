package br.com.anagnostou.publisher.objetos;


public class Assistencia {
    private int numero, total;
    private float media;
    private String mes;
    private String ano;
    private String reuniao;

    public Assistencia(String mes, String ano, String reuniao, int numero, int total, float media) {

        this.mes = getMonth(mes);

        this.ano = ano;

        if (reuniao.contentEquals("midweek")) {
            this.reuniao = "Reunião durante a semana";
        } else this.reuniao = "Reunião de fim de semana";


        this.numero = numero;
        this.total = total;
        this.media = media;
    }


    private String getMonth(String mes) {
        switch (mes) {
            case "01":
                return "Janeiro";

            case "02":
                return "Fevereiro";

            case "03":
                return "Março";

            case "04":
                return "Abril";

            case "05":
                return "Maio";

            case "06":
                return "Junho";

            case "07":
                return "Julho";

            case "08":
                return "Agosto";

            case "09":
                return "Setembro";

            case "10":
                return "Outubro";

            case "11":
                return "Novembro";

            case "12":
                return "Dezembro";
        }
        return "São Nunca";
    }


    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }


    public String getReuniao() {
        return reuniao;
    }

    public void setReuniao(String reuniao) {
        this.reuniao = reuniao;
    }


    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public float getMedia() {
        return media;
    }

    public void setMedia(float media) {
        this.media = media;
    }
}
