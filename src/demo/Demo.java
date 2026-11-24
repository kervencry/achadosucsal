package demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import AchadosUcsal.ObjetoAchado;
import AchadosUcsal.RepositorioObjetos;
import AchadosUcsal.ServicoAchadosPerdidos;

public class Demo {

	private static final String[][] ACHADOS = { { "Carregador de notebook Dell", "Eletrônicos", "Biblioteca Central" },
			{ "RG (carteira de identidade)", "Documentos", "Secretaria Acadêmica" },
			{ "Casaco jeans azul", "Roupas", "Bloco B - Sala 204" },
			{ "Calculadora científica Casio", "Material escolar", "Laboratório de Informática" },
			{ "Garrafa térmica verde", "Acessórios", "Auditório" },
			{ "Fone de ouvido Bluetooth preto", "Eletrônicos", "Cantina" },
			{ "Óculos de grau com estojo", "Acessórios", "Biblioteca Central" },
			{ "Guarda-chuva preto", "Outros", "Portaria Principal" } };

	private static final String CPF_VALIDO = "123.456.789-09";
	private static final String CPF_VALIDO_2 = "111.444.777-35";

	private interface Acao {
		void executar() throws Exception;
	}

	private static boolean passoAPasso;
	private static Scanner teclado;

	public static void main(String[] args) throws IOException {
		passoAPasso = args.length > 0 && args[0].equals("--passo-a-passo");

		String homeOriginal = System.getProperty("user.home");
		Path temporaria = Files.createTempDirectory("achados-demo");

		System.setProperty("user.home", temporaria.toString());

		try {
			executar();
		} catch (Exception erro) {
			System.out.println("\nA demonstração falhou: " + erro);
			erro.printStackTrace(System.out);
		} finally {
			System.setProperty("user.home", homeOriginal);
			apagar(temporaria);
		}
	}

	private static void executar() throws Exception {
		titulo("Preparação");
		System.out.println("Pasta temporária da demonstração (apagada no fim):");
		System.out.println("  " + System.getProperty("user.home"));

		RepositorioObjetos repositorio = new RepositorioObjetos();
		ServicoAchadosPerdidos servico = new ServicoAchadosPerdidos(repositorio);

		System.out.println("Objetos já cadastrados: " + servico.listar().size());
		pausar();

		titulo("Cena 1 - Cadastrar objetos encontrados");
		for (String[] achado : ACHADOS) {
			servico.cadastrar(achado[0], achado[1], achado[2]);
			System.out.println("Cadastrado: " + achado[0]);
		}
		System.out.println();
		imprimir(servico.listar());
		pausar();

		titulo("Cena 2 - Buscar");
		buscar(servico, "biblioteca");
		buscar(servico, "ELETR");
		buscar(servico, "encontrado");
		buscar(servico, "7");
		buscar(servico, "zzz");
		pausar();

		titulo("Cena 3 - Validação ao cadastrar");
		tentar("Cadastrar com nome em branco", () -> servico.cadastrar("   ", "Outros", "Cantina"));
		tentar("Cadastrar com local vazio", () -> servico.cadastrar("Mochila azul", "Acessórios", ""));
		System.out.println("Total continua: " + servico.listar().size());
		pausar();

		titulo("Cena 4 - Registrar devolução (identificação de quem retira)");
		System.out.println("Objeto 3: Casaco jeans azul\n");
		tentar("Nome sem sobrenome", () -> servico.registrarDevolucao(3, "Camila", CPF_VALIDO));
		tentar("CPF com todos os dígitos iguais",
				() -> servico.registrarDevolucao(3, "Camila Ferreira Rocha", "111.111.111-11"));
		tentar("CPF com dígito verificador errado",
				() -> servico.registrarDevolucao(3, "Camila Ferreira Rocha", "123.456.789-00"));
		tentar("Objeto que não existe (ID 99)",
				() -> servico.registrarDevolucao(99, "Camila Ferreira Rocha", CPF_VALIDO));
		tentar("Dados corretos", () -> servico.registrarDevolucao(3, "Camila Ferreira Rocha", CPF_VALIDO));
		tentar("Devolver o mesmo objeto outra vez",
				() -> servico.registrarDevolucao(3, "Camila Ferreira Rocha", CPF_VALIDO));

		System.out.println();
		buscar(servico, "devolvido");
		pausar();

		titulo("Cena 5 - Consultar retirada");
		ObjetoAchado devolvido = encontrar(servico, 3);
		System.out.println("Objeto:       " + devolvido.getNome());
		System.out.println("Retirado por: " + devolvido.getNomeRetirante());
		System.out.println("CPF:          " + formatarCpf(devolvido.getCpfRetirante()));
		System.out.println("Data:         " + devolvido.getDataRetirada());

		ObjetoAchado naoRetirado = encontrar(servico, 1);
		System.out.println("\nObjeto 1 (" + naoRetirado.getNome() + ") devolvido? " + naoRetirado.isDevolvido());
		pausar();

		titulo("Cena 6 - Cópias defensivas");
		System.out.println("listar() devolve CÓPIAS: alterar uma cópia não muda o serviço.");

		ObjetoAchado copia = servico.listar().get(0);
		copia.registrarDevolucao("João Pedro Nogueira", CPF_VALIDO_2);

		System.out.println("Cópia do objeto 1 marcada como devolvida: " + copia.isDevolvido());
		System.out.println("Objeto 1 dentro do serviço continua:      " + encontrar(servico, 1).getStatus());
		pausar();

		titulo("Cena 7 - Persistência (simula fechar e abrir o sistema)");
		System.out.println("Criando um serviço novo, que lê o arquivo salvo...\n");

		ServicoAchadosPerdidos reaberto = new ServicoAchadosPerdidos(new RepositorioObjetos());

		System.out.println("Objetos carregados: " + reaberto.listar().size());
		imprimir(reaberto.listar());

		servico.cadastrar("Pen drive Kingston 32GB", "Eletrônicos", "Laboratório de Informática");
		ServicoAchadosPerdidos terceiro = new ServicoAchadosPerdidos(new RepositorioObjetos());
		System.out.println("\nApós cadastrar mais um e reabrir: " + terceiro.listar().size() + " objetos (novo ID: "
				+ terceiro.listar().get(8).getId() + ")");

		titulo("Fim da demonstração");
		System.out.println("A pasta temporária será apagada agora.");
	}

	private static void tentar(String descricao, Acao acao) {
		System.out.print("- " + descricao + ": ");

		try {
			acao.executar();
			System.out.println("ACEITO");
		} catch (Exception erro) {
			System.out.println("RECUSADO -> " + erro.getMessage());
		}
	}

	private static void buscar(ServicoAchadosPerdidos servico, String texto) {
		List<ObjetoAchado> resultado = servico.buscar(texto);
		System.out.println("Busca \"" + texto + "\" -> " + resultado.size() + " resultado(s)");

		for (ObjetoAchado objeto : resultado) {
			System.out.println("    #" + objeto.getId() + " " + objeto.getNome() + " (" + objeto.getLocal() + ", "
					+ objeto.getStatus() + ")");
		}
	}

	private static ObjetoAchado encontrar(ServicoAchadosPerdidos servico, int id) {
		return servico.listar().stream().filter(objeto -> objeto.getId() == id).findFirst().orElseThrow();
	}

	private static void imprimir(List<ObjetoAchado> objetos) {
		String formato = "%-3s %-32s %-17s %-27s %-11s %s%n";
		System.out.printf(formato, "ID", "Objeto", "Categoria", "Local", "Data", "Status");

		for (ObjetoAchado objeto : objetos) {
			System.out.printf(formato, objeto.getId(), objeto.getNome(), objeto.getCategoria(), objeto.getLocal(),
					objeto.getData(), objeto.getStatus());
		}
	}

	private static String formatarCpf(String cpf) {
		return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
	}

	private static void titulo(String texto) {
		System.out.println("\n==================================================");
		System.out.println(texto);
		System.out.println("==================================================");
	}

	private static void pausar() {
		if (!passoAPasso) {
			return;
		}

		System.out.print("\n[ENTER para continuar] ");

		try {
			if (teclado == null) {
				teclado = new Scanner(System.in);
			}
			teclado.nextLine();
		} catch (RuntimeException erro) {
			passoAPasso = false;
		}
	}

	private static void apagar(Path pasta) {
		try (Stream<Path> caminhos = Files.walk(pasta)) {
			caminhos.sorted(Comparator.reverseOrder()).forEach(caminho -> {
				try {
					Files.deleteIfExists(caminho);
				} catch (IOException erro) {

				}
			});
		} catch (IOException erro) {

		}
	}
}
