package br.com.anagnostou.publisher.objetos;


public class AnoDeServicoDoPioneiro {
    private String nome;
    private int meses;
    private int totalhoras;
    private float mediamensal;
    private float mediarequisito;
    private float revisitas;
    private float estudos;

    public AnoDeServicoDoPioneiro(String nome, int meses, int totalhoras, float mediamensal, float mediarequisito, float revisitas, float estudos) {
        this.nome = nome;
       this.meses = meses;
        this.totalhoras = totalhoras;
        this.mediamensal = mediamensal;
        this.mediarequisito = mediarequisito;
        this.revisitas = revisitas;
        this.estudos = estudos;
    }

    public int getMeses() {
        return meses;
    }

    public void setMeses(int meses) {
        this.meses = meses;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTotalhoras() {
        return totalhoras;
    }

    public void setTotalhoras(int totalhoras) {
        this.totalhoras = totalhoras;
    }

    public float getMediamensal() {
        return mediamensal;
    }

    public void setMediamensal(float mediamensal) {
        this.mediamensal = mediamensal;
    }

    public float getMediarequisito() {
        return mediarequisito;
    }

    public void setMediarequisito(float mediarequisito) {
        this.mediarequisito = mediarequisito;
    }

    public float getRevisitas() {
        return revisitas;
    }

    public void setRevisitas(float revisitas) {
        this.revisitas = revisitas;
    }

    public float getEstudos() {
        return estudos;
    }

    public void setEstudos(float estudos) {
        this.estudos = estudos;
    }
}
