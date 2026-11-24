package AchadosUcsal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaPrincipal extends JFrame {

    private final ServicoAchadosPerdidos servico;
    private final JTextField busca = new JTextField(25);

    private final DateTimeFormatter formatoData =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DefaultTableModel modelo =
        new DefaultTableModel(
            new String[]{
                "ID", "Objeto", "Categoria",
                "Local", "Data", "Status"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int coluna) {
                return coluna == 0 ? Integer.class : String.class;
            }
        };

    private final JTable tabela = new JTable(modelo);

    public TelaPrincipal(ServicoAchadosPerdidos servico) {
        super("Achados UCSal");
        this.servico = servico;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 550);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titulo = new JLabel("Achados UCSal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));

        JButton pesquisar = new JButton("Buscar");
        JButton limpar = new JButton("Limpar busca");

        pesquisar.addActionListener(e -> atualizarTabela());
        busca.addActionListener(e -> atualizarTabela());

        limpar.addActionListener(e -> {
            busca.setText("");
            atualizarTabela();
        });

        JPanel pesquisa = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pesquisa.add(new JLabel("Pesquisar:"));
        pesquisa.add(busca);
        pesquisa.add(pesquisar);
        pesquisa.add(limpar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(titulo, BorderLayout.NORTH);
        topo.add(pesquisa, BorderLayout.SOUTH);

        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoCreateRowSorter(true);

        JButton cadastrar = new JButton("Cadastrar objeto");
        JButton devolver = new JButton("Registrar devolução");
        JButton retirada = new JButton("Consultar retirada");

        cadastrar.addActionListener(e -> cadastrarObjeto());
        devolver.addActionListener(e -> registrarDevolucao());
        retirada.addActionListener(e -> consultarRetirada());

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botoes.add(cadastrar);
        botoes.add(devolver);
        botoes.add(retirada);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(
            new JLabel("Os cadastros são salvos automaticamente."),
            BorderLayout.NORTH
        );
        rodape.add(botoes, BorderLayout.SOUTH);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);

        setContentPane(painel);
        atualizarTabela();
    }

    private void atualizarTabela() {
        List<ObjetoAchado> objetos = servico.buscar(busca.getText());

        modelo.setRowCount(0);

        for (ObjetoAchado objeto : objetos) {
            modelo.addRow(new Object[]{
                objeto.getId(),
                objeto.getNome(),
                objeto.getCategoria(),
                objeto.getLocal(),
                objeto.getData().toString(),
                objeto.getStatus()
            });
        }
    }

    private void cadastrarObjeto() {
        JTextField nome = new JTextField();
        JTextField local = new JTextField();

        JComboBox<String> categoria = new JComboBox<>(
            new String[]{
                "Eletrônicos", "Documentos", "Roupas",
                "Material escolar", "Acessórios", "Outros"
            }
        );

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Nome do objeto:"));
        formulario.add(nome);
        formulario.add(new JLabel("Categoria:"));
        formulario.add(categoria);
        formulario.add(new JLabel("Local onde foi encontrado:"));
        formulario.add(local);

        while (true) {
            int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Cadastrar objeto encontrado",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (resposta != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                servico.cadastrar(
                    nome.getText(),
                    (String) categoria.getSelectedItem(),
                    local.getText()
                );

                busca.setText("");
                atualizarTabela();

                JOptionPane.showMessageDialog(
                    this, "Objeto cadastrado e salvo!"
                );
                return;

            } catch (IllegalArgumentException erro) {
                mostrarErro(erro.getMessage());

            } catch (IOException erro) {
                mostrarErro(
                    "Não foi possível salvar o cadastro. "
                    + "Verifique a permissão de escrita na pasta "
                    + "AchadosUcsal do seu usuário."
                );
                return;

            } catch (IllegalStateException erro) {
                mostrarErro(erro.getMessage());
                return;
            }
        }
    }

    private void registrarDevolucao() {
        int selecionada = tabela.getSelectedRow();

        if (selecionada == -1) {
            mostrarErro("Selecione um objeto na tabela.");
            return;
        }

        int linha = tabela.convertRowIndexToModel(selecionada);
        int id = (Integer) modelo.getValueAt(linha, 0);
        String nomeObjeto = (String) modelo.getValueAt(linha, 1);

        if ("Devolvido".equals(modelo.getValueAt(linha, 5))) {
            mostrarErro("Este objeto já foi devolvido.");
            return;
        }

        JTextField nomeRetirante = new JTextField();
        JTextField cpfRetirante = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(0, 2, 10, 10));
        formulario.add(new JLabel("Objeto:"));
        formulario.add(new JLabel(nomeObjeto));
        formulario.add(new JLabel("Nome completo de quem retirou:"));
        formulario.add(nomeRetirante);
        formulario.add(new JLabel("CPF (com ou sem pontuação):"));
        formulario.add(cpfRetirante);

        while (true) {
            int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Identificação de quem retirou",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (resposta != JOptionPane.OK_OPTION) {
                return;
            }

            // Valida antes de apresentar a confirmação.
            try {
                ObjetoAchado validacao = new ObjetoAchado(
                    id,
                    nomeObjeto,
                    (String) modelo.getValueAt(linha, 2),
                    (String) modelo.getValueAt(linha, 3),
                    java.time.LocalDate.parse(
                        (String) modelo.getValueAt(linha, 4)
                    ),
                    false
                );

                validacao.registrarDevolucao(
                    nomeRetirante.getText(),
                    cpfRetirante.getText()
                );

            } catch (IllegalArgumentException erro) {
                mostrarErro(erro.getMessage());
                continue;
            }

            int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Confirmar a entrega de " + nomeObjeto
                    + " para:\n"
                    + nomeRetirante.getText().trim()
                    + "\nCPF: " + cpfRetirante.getText().trim(),
                "Confirmar devolução",
                JOptionPane.YES_NO_OPTION
            );

            if (confirmacao != JOptionPane.YES_OPTION) {
                continue;
            }

            try {
                servico.registrarDevolucao(
                    id,
                    nomeRetirante.getText(),
                    cpfRetirante.getText()
                );

                atualizarTabela();

                JOptionPane.showMessageDialog(
                    this, "Devolução registrada e salva!"
                );
                return;

            } catch (IllegalArgumentException erro) {
                mostrarErro(erro.getMessage());

            } catch (IllegalStateException erro) {
                mostrarErro(erro.getMessage());
                return;

            } catch (IOException erro) {
                mostrarErro(
                    "Não foi possível salvar a devolução. "
                    + "A alteração não foi confirmada."
                );
                return;
            }
        }
    }

    private void consultarRetirada() {
        int selecionada = tabela.getSelectedRow();

        if (selecionada == -1) {
            mostrarErro("Selecione um objeto na tabela.");
            return;
        }

        int linha = tabela.convertRowIndexToModel(selecionada);
        int id = (Integer) modelo.getValueAt(linha, 0);

        ObjetoAchado objeto = servico.listar().stream()
            .filter(item -> item.getId() == id)
            .findFirst()
            .orElse(null);

        if (objeto == null) {
            mostrarErro("Objeto não encontrado.");
            return;
        }

        if (!objeto.isDevolvido()) {
            mostrarErro("Este objeto ainda não foi retirado.");
            return;
        }

        if (objeto.getCpfRetirante().isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "Devolução antiga, sem identificação registrada."
            );
            return;
        }

        String texto =
            "Objeto: " + objeto.getNome()
            + "\nRetirado por: " + objeto.getNomeRetirante()
            + "\nCPF: " + formatarCpf(objeto.getCpfRetirante())
            + "\nData da retirada: "
            + objeto.getDataRetirada().format(formatoData);

        JTextArea informacoes = new JTextArea(texto, 7, 40);
        informacoes.setEditable(false);
        informacoes.setLineWrap(true);
        informacoes.setWrapStyleWord(true);
        informacoes.setCaretPosition(0);

        JOptionPane.showMessageDialog(
            this,
            new JScrollPane(informacoes),
            "Registro de retirada",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String formatarCpf(String cpf) {
        return cpf.substring(0, 3) + "."
            + cpf.substring(3, 6) + "."
            + cpf.substring(6, 9) + "-"
            + cpf.substring(9, 11);
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(
            this,
            mensagem,
            "Atenção",
            JOptionPane.ERROR_MESSAGE
        );
    }
}