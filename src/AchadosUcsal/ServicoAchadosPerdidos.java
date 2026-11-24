package AchadosUcsal;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ServicoAchadosPerdidos {

    private final RepositorioObjetos repositorio;
    private List<ObjetoAchado> objetos;
    private long proximoId;

    public ServicoAchadosPerdidos(
            RepositorioObjetos repositorio) throws IOException {

        this.repositorio = repositorio;
        this.objetos = repositorio.carregar();

        this.proximoId = objetos.stream()
            .mapToLong(ObjetoAchado::getId)
            .max()
            .orElse(0) + 1;
    }

    public void cadastrar(
            String nome,
            String categoria,
            String local) throws IOException {

        if (proximoId > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                "O limite de cadastros foi atingido."
            );
        }

        ObjetoAchado novo = new ObjetoAchado(
            (int) proximoId,
            nome,
            categoria,
            local,
            LocalDate.now(),
            false
        );

        List<ObjetoAchado> atualizados = new ArrayList<>(objetos);
        atualizados.add(novo);

        // Confirma a alteração na memória somente após salvar.
        repositorio.salvar(atualizados);
        objetos = atualizados;
        proximoId++;
    }

    public List<ObjetoAchado> listar() {
        return objetos.stream()
            .map(this::copiar)
            .collect(Collectors.toList());
    }

    public List<ObjetoAchado> buscar(String texto) {
        String termo = texto == null
            ? ""
            : texto.trim().toLowerCase(Locale.ROOT);

        return objetos.stream()
            .filter(objeto ->
                contem(objeto.getNome(), termo)
                || contem(objeto.getCategoria(), termo)
                || contem(objeto.getLocal(), termo)
                || contem(objeto.getStatus(), termo)
                || String.valueOf(objeto.getId()).contains(termo)
                || objeto.getData().toString().contains(termo)
            )
            .map(this::copiar)
            .collect(Collectors.toList());
    }

    public void registrarDevolucao(
            int id,
            String nomeRetirante,
            String cpfRetirante) throws IOException {

        List<ObjetoAchado> atualizados = listar();

        ObjetoAchado objeto = atualizados.stream()
            .filter(item -> item.getId() == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Objeto não encontrado."
            ));

        objeto.registrarDevolucao(
            nomeRetirante,
            cpfRetirante
        );

        // Se o salvamento falhar, os dados originais são preservados.
        repositorio.salvar(atualizados);
        objetos = atualizados;
    }

    private boolean contem(String valor, String termo) {
        return valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    private ObjetoAchado copiar(ObjetoAchado objeto) {
        ObjetoAchado copia = new ObjetoAchado(
            objeto.getId(),
            objeto.getNome(),
            objeto.getCategoria(),
            objeto.getLocal(),
            objeto.getData(),
            objeto.isDevolvido()
        );

        // Devoluções antigas podem não ter identificação.
        if (!objeto.getCpfRetirante().isBlank()) {
            copia.restaurarRetirada(
                objeto.getNomeRetirante(),
                objeto.getCpfRetirante(),
                objeto.getDataRetirada()
            );
        }

        return copia;
    }
}