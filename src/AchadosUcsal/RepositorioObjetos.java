package AchadosUcsal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RepositorioObjetos {

    private final Path arquivo;

    public RepositorioObjetos() {
        arquivo = Path.of(
            System.getProperty("user.home"),
            "AchadosUcsal",
            "objetos.properties"
        );
    }

    public List<ObjetoAchado> carregar() throws IOException {
        List<ObjetoAchado> objetos = new ArrayList<>();

        if (Files.notExists(arquivo)) {
            return objetos;
        }

        Properties dados = new Properties();

        try (Reader leitor = Files.newBufferedReader(
                arquivo, StandardCharsets.UTF_8)) {
            dados.load(leitor);
        } catch (IllegalArgumentException erro) {
            throw new IOException(
                "O arquivo de cadastros está inválido.",
                erro
            );
        }

        try {
            int quantidade = Integer.parseInt(
                exigirCampo(dados, "quantidade")
            );

            if (quantidade < 0) {
                throw new IllegalArgumentException(
                    "Quantidade inválida."
                );
            }

            Set<Integer> ids = new HashSet<>();

            for (int i = 0; i < quantidade; i++) {
                String prefixo = "objeto." + i + ".";

                int id = Integer.parseInt(
                    exigirCampo(dados, prefixo + "id")
                );

                if (!ids.add(id)) {
                    throw new IllegalArgumentException(
                        "ID duplicado: " + id
                    );
                }

                String devolvido = exigirCampo(
                    dados, prefixo + "devolvido"
                );

                if (!devolvido.equals("true")
                        && !devolvido.equals("false")) {
                    throw new IllegalArgumentException(
                        "Status inválido."
                    );
                }

                ObjetoAchado objeto = new ObjetoAchado(
                    id,
                    exigirCampo(dados, prefixo + "nome"),
                    exigirCampo(dados, prefixo + "categoria"),
                    exigirCampo(dados, prefixo + "local"),
                    LocalDate.parse(
                        exigirCampo(dados, prefixo + "data")
                    ),
                    Boolean.parseBoolean(devolvido)
                );

                // Registros antigos podem não ter dados da retirada.
                String cpfSalvo = dados.getProperty(
                    prefixo + "cpfRetirante", ""
                );

                if (!cpfSalvo.isBlank()) {
                    objeto.restaurarRetirada(
                        exigirCampo(dados, prefixo + "nomeRetirante"),
                        cpfSalvo,
                        LocalDate.parse(
                            exigirCampo(dados, prefixo + "dataRetirada")
                        )
                    );
                }

                objetos.add(objeto);
            }

            return objetos;

        } catch (RuntimeException erro) {
            throw new IOException(
                "Não foi possível ler os cadastros: arquivo inválido.",
                erro
            );
        }
    }

    public void salvar(List<ObjetoAchado> objetos) throws IOException {
        Properties dados = new Properties();

        dados.setProperty(
            "quantidade", String.valueOf(objetos.size())
        );

        for (int i = 0; i < objetos.size(); i++) {
            ObjetoAchado objeto = objetos.get(i);
            String prefixo = "objeto." + i + ".";

            dados.setProperty(
                prefixo + "id", String.valueOf(objeto.getId())
            );
            dados.setProperty(
                prefixo + "nome", objeto.getNome()
            );
            dados.setProperty(
                prefixo + "categoria", objeto.getCategoria()
            );
            dados.setProperty(
                prefixo + "local", objeto.getLocal()
            );
            dados.setProperty(
                prefixo + "data", objeto.getData().toString()
            );
            dados.setProperty(
                prefixo + "devolvido",
                String.valueOf(objeto.isDevolvido())
            );

            // Identificação de quem retirou o objeto.
            dados.setProperty(
                prefixo + "nomeRetirante",
                objeto.getNomeRetirante()
            );
            dados.setProperty(
                prefixo + "cpfRetirante",
                objeto.getCpfRetirante()
            );

            if (objeto.getDataRetirada() != null) {
                dados.setProperty(
                    prefixo + "dataRetirada",
                    objeto.getDataRetirada().toString()
                );
            }
        }

        Files.createDirectories(arquivo.getParent());

        Path temporario = Files.createTempFile(
            arquivo.getParent(), "objetos-", ".tmp"
        );

        try {
            try (Writer escritor = Files.newBufferedWriter(
                    temporario, StandardCharsets.UTF_8)) {
                dados.store(escritor, "Cadastros do Achados UCSal");
            }

            try {
                Files.move(
                    temporario,
                    arquivo,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException erro) {
                Files.move(
                    temporario,
                    arquivo,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    private String exigirCampo(Properties dados, String chave) {
        String valor = dados.getProperty(chave);

        if (valor == null) {
            throw new IllegalArgumentException(
                "Campo ausente: " + chave
            );
        }

        return valor;
    }
}