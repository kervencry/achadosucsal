package AchadosUcsal;

import java.time.LocalDate;

public class ObjetoAchado {

    private final int id;
    private final String nome;
    private final String categoria;
    private final String local;
    private final LocalDate data;

    private boolean devolvido;
    private String nomeRetirante = "";
    private String cpfRetirante = "";
    private LocalDate dataRetirada;

    public ObjetoAchado(
            int id,
            String nome,
            String categoria,
            String local,
            LocalDate data,
            boolean devolvido) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                "ID deve ser positivo."
            );
        }

        this.id = id;
        this.nome = validarTexto(nome, "Nome");
        this.categoria = validarTexto(categoria, "Categoria");
        this.local = validarTexto(local, "Local");

        if (data == null) {
            throw new IllegalArgumentException(
                "Informe a data."
            );
        }

        this.data = data;
        this.devolvido = devolvido;
    }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                campo + " não pode ficar vazio."
            );
        }

        return texto.trim();
    }

    private static String validarNomeRetirante(String nome) {
        String nomeValidado = validarTexto(
            nome, "Nome de quem retirou"
        );

        if (nomeValidado.split("\\s+").length < 2) {
            throw new IllegalArgumentException(
                "Informe o nome completo de quem retirou."
            );
        }

        return nomeValidado;
    }

    private static String validarCpf(String cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException(
                "Informe o CPF."
            );
        }

        String numeros = cpf.trim()
            .replace(".", "")
            .replace("-", "");

        if (!numeros.matches("[0-9]{11}")
                || numeros.matches("([0-9])\\1{10}")) {
            throw new IllegalArgumentException(
                "CPF inválido."
            );
        }

        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;

            for (int i = 0; i < posicao; i++) {
                soma += (numeros.charAt(i) - '0')
                    * (posicao + 1 - i);
            }

            int digito = 11 - (soma % 11);

            if (digito >= 10) {
                digito = 0;
            }

            if (digito != numeros.charAt(posicao) - '0') {
                throw new IllegalArgumentException(
                    "CPF inválido."
                );
            }
        }

        return numeros;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getLocal() {
        return local;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean isDevolvido() {
        return devolvido;
    }

    public String getStatus() {
        return devolvido ? "Devolvido" : "Encontrado";
    }

    public String getNomeRetirante() {
        return nomeRetirante;
    }

    public String getCpfRetirante() {
        return cpfRetirante;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public void registrarDevolucao(String nome, String cpf) {
        if (devolvido) {
            throw new IllegalStateException(
                "Este objeto já foi devolvido."
            );
        }

        String nomeValidado = validarNomeRetirante(nome);
        String cpfValidado = validarCpf(cpf);

        nomeRetirante = nomeValidado;
        cpfRetirante = cpfValidado;
        dataRetirada = LocalDate.now();
        devolvido = true;
    }

    // Recupera os dados salvos sem alterar a data da retirada.
    public void restaurarRetirada(
            String nome, String cpf, LocalDate data) {

        if (!devolvido) {
            throw new IllegalStateException(
                "Objeto ainda não foi devolvido."
            );
        }

        String nomeValidado = validarNomeRetirante(nome);
        String cpfValidado = validarCpf(cpf);

        if (data == null) {
            throw new IllegalArgumentException(
                "Data de retirada ausente."
            );
        }

        nomeRetirante = nomeValidado;
        cpfRetirante = cpfValidado;
        dataRetirada = data;
    }
}