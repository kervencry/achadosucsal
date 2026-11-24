package AchadosUcsal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                RepositorioObjetos repositorio =
                    new RepositorioObjetos();

                ServicoAchadosPerdidos servico =
                    new ServicoAchadosPerdidos(repositorio);

                TelaPrincipal tela = new TelaPrincipal(servico);
                tela.setVisible(true);

            } catch (IOException erro) {
                JOptionPane.showMessageDialog(
                    null,
                    "Não foi possível carregar os cadastros.\n"
                        + erro.getMessage(),
                    "Erro ao iniciar",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}