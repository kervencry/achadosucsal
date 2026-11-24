package teste;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import AchadosUcsal.ObjetoAchado;
import AchadosUcsal.RepositorioObjetos;
import AchadosUcsal.ServicoAchadosPerdidos;

public class TesteSimples {
	  static int passou = 0;
	    static int falhou = 0;

	    interface Caso { void rodar() throws Exception; }

	    static void teste(String nome, Caso caso) {
	        try {
	            limparDados();
	            caso.rodar();
	            passou++;
	            System.out.println("[OK]    " + nome);
	        } catch (Throwable erro) {
	            falhou++;
	            System.out.println("[FALHA] " + nome + "  ->  " + erro);
	        }
	    }

	    static void verificar(boolean condicao) {
	        if (!condicao) throw new AssertionError("condição falsa");
	    }


	    static boolean lanca(Class<? extends Throwable> tipo, Caso caso) {
	        try {
	            caso.rodar();
	            return false;
	        } catch (Throwable erro) {
	            return tipo.isInstance(erro);
	        }
	    }

	  
	    static void limparDados() throws IOException {
	        Files.deleteIfExists(Path.of(
	            System.getProperty("user.home"),
	            "AchadosUcsal", "objetos.properties"));
	    }

	    static ServicoAchadosPerdidos novoServico() throws IOException {
	        return new ServicoAchadosPerdidos(new RepositorioObjetos());
	    }

	    static ObjetoAchado novoObjeto() {
	        return new ObjetoAchado(
	            1, "Mochila", "Acessórios", "Biblioteca",
	            LocalDate.now(), false);
	    }

	    public static void main(String[] args) throws Exception {

	  
	        System.setProperty("user.home",
	            Files.createTempDirectory("achados-teste").toString());

	        System.out.println("=== ObjetoAchado ===");

	        teste("cria objeto válido", () -> {
	            ObjetoAchado o = novoObjeto();
	            verificar(o.getNome().equals("Mochila"));
	            verificar(o.getStatus().equals("Encontrado"));
	        });

	        teste("rejeita nome vazio", () ->
	            verificar(lanca(IllegalArgumentException.class, () ->
	                new ObjetoAchado(1, "", "Outros", "Sala",
	                    LocalDate.now(), false))));

	        teste("aceita CPF válido", () -> {
	            ObjetoAchado o = novoObjeto();
	            o.registrarDevolucao("Maria Silva", "529.982.247-25");
	            verificar(o.getCpfRetirante().equals("52998224725"));
	        });

	        teste("rejeita CPF com dígito errado", () ->
	            verificar(lanca(IllegalArgumentException.class, () ->
	                novoObjeto().registrarDevolucao(
	                    "Maria Silva", "529.982.247-24"))));

	        teste("rejeita CPF com números repetidos", () ->
	            verificar(lanca(IllegalArgumentException.class, () ->
	                novoObjeto().registrarDevolucao(
	                    "Maria Silva", "111.111.111-11"))));

	        teste("rejeita nome sem sobrenome", () ->
	            verificar(lanca(IllegalArgumentException.class, () ->
	                novoObjeto().registrarDevolucao(
	                    "Maria", "529.982.247-25"))));

	        teste("devolução muda o status", () -> {
	            ObjetoAchado o = novoObjeto();
	            o.registrarDevolucao("Maria Silva", "529.982.247-25");
	            verificar(o.isDevolvido());
	            verificar(o.getStatus().equals("Devolvido"));
	        });

	        teste("não devolve o mesmo objeto duas vezes", () -> {
	            ObjetoAchado o = novoObjeto();
	            o.registrarDevolucao("Maria Silva", "529.982.247-25");
	            verificar(lanca(IllegalStateException.class, () ->
	                o.registrarDevolucao("João Souza", "111.444.777-35")));
	        });

	        System.out.println("\n=== ServicoAchadosPerdidos ===");

	        teste("cadastrar gera ID 1 e IDs seguintes", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            s.cadastrar("Chave", "Outros", "Portaria");
	            s.cadastrar("Livro", "Material escolar", "Sala 1");
	            List<ObjetoAchado> lista = s.listar();
	            verificar(lista.size() == 2);
	            verificar(lista.get(0).getId() == 1);
	            verificar(lista.get(1).getId() == 2);
	        });

	        teste("cadastro inválido não salva nada", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            verificar(lanca(IllegalArgumentException.class, () ->
	                s.cadastrar("", "Outros", "Portaria")));
	            verificar(s.listar().isEmpty());
	        });

	        teste("buscar por nome", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            s.cadastrar("iPhone", "Eletrônicos", "Cantina");
	            s.cadastrar("Casaco", "Roupas", "Sala 2");
	            verificar(s.buscar("iphone").size() == 1);
	        });

	        teste("buscar vazio devolve tudo", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            s.cadastrar("Chave", "Outros", "Portaria");
	            s.cadastrar("Livro", "Material escolar", "Sala 1");
	            verificar(s.buscar("").size() == 2);
	        });

	        teste("devolução fica salva no arquivo", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            s.cadastrar("Chave", "Outros", "Portaria");
	            s.registrarDevolucao(1, "Maria Silva", "529.982.247-25");

	            
	            verificar(novoServico().listar().get(0).isDevolvido());
	        });

	        teste("devolver ID inexistente dá erro", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            verificar(lanca(IllegalArgumentException.class, () ->
	                s.registrarDevolucao(99, "Maria Silva", "529.982.247-25")));
	        });

	        teste("listar devolve cópia (não altera o original)", () -> {
	            ServicoAchadosPerdidos s = novoServico();
	            s.cadastrar("Chave", "Outros", "Portaria");
	            s.listar().get(0).registrarDevolucao(
	                "Maria Silva", "529.982.247-25");
	            verificar(!s.listar().get(0).isDevolvido());
	        });

	        System.out.println("\n=== RepositorioObjetos ===");

	        teste("sem arquivo, carrega lista vazia", () ->
	            verificar(new RepositorioObjetos().carregar().isEmpty()));

	        teste("salvar e carregar mantém os dados", () -> {
	            RepositorioObjetos repo = new RepositorioObjetos();
	            repo.salvar(List.of(novoObjeto()));
	            ObjetoAchado lido = repo.carregar().get(0);
	            verificar(lido.getNome().equals("Mochila"));
	            verificar(lido.getCategoria().equals("Acessórios"));
	        });

	        teste("arquivo corrompido dá IOException", () -> {
	            Path pasta = Path.of(System.getProperty("user.home"),
	                "AchadosUcsal");
	            Files.createDirectories(pasta);
	            Files.writeString(pasta.resolve("objetos.properties"),
	                "quantidade=abc");
	            verificar(lanca(IOException.class, () ->
	                new RepositorioObjetos().carregar()));
	        });

	        System.out.println("\n==== " + passou + " passaram, "
	            + falhou + " falharam ====");
	    }
	}

