package farmacia;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


public class Farmacia {
    private List<Cliente> clientes;
    private List<Produto> produtos;
    private List<Venda> vendas;

    public Farmacia() {
        clientes = new ArrayList<>();
        produtos = new ArrayList<>();
        vendas = new ArrayList<>();
        carregarDados();
        if (produtos.isEmpty()) {
            inicializarProdutos();
        }
    }

    private void inicializarProdutos() {
        produtos.add(new Produto("Paracetamol", 100, 50));
        produtos.add(new Produto("Ibuprofeno", 80, 50));
        produtos.add(new Produto("Creme para as maos", 300, 50));
        produtos.add(new Produto("Preservativos (Prudence)", 20, 50));
        produtos.add(new Produto("Caixa de Mascaras", 300, 50));
        produtos.add(new Produto("Teste de Gravidez", 150, 50));
        produtos.add(new Produto("Repelente", 100, 50));
        produtos.add(new Produto("Antibiotico", 200, 50));
        produtos.add(new Produto("Termometro", 700, 50));
        produtos.add(new Produto("Liquido Antisseptico (Dettol)", 40, 50));
        salvarDados();
    }

    public void adicionarCliente(Cliente cliente) {
        clientes.add(cliente);
        logOperacao("Adicionar Cliente: " + cliente);
        salvarDados();
    }

    public void atualizarCliente(String id, Cliente novoCliente) {
        for (Cliente cliente : clientes) {
            if (cliente.getId().equals(id)) {
                cliente.setNome(novoCliente.getNome());
                cliente.setId(novoCliente.getId());
                logOperacao("Atualizar Cliente: " + id + " para " + novoCliente);
                salvarDados();
                return;
            }
        }
        System.out.println("Cliente nao encontrado.");
    }

    public void removerCliente(String id) {
        clientes.removeIf(cliente -> cliente.getId().equals(id));
        logOperacao("Remover Cliente: " + id);
        salvarDados();
    }

    public void adicionarProduto(Produto produto) {
        produtos.add(produto);
        logOperacao("Adicionar Produto: " + produto);
        salvarDados();
    }

    public void atualizarProduto(String nome, Produto novoProduto) {
        for (Produto produto : produtos) {
            if (produto.getNome().equals(nome)) {
                produto.setNome(novoProduto.getNome());
                produto.setPreco(novoProduto.getPreco());
                produto.setQuantidade(novoProduto.getQuantidade());
                logOperacao("Atualizar Produto: " + nome + " para " + novoProduto);
                salvarDados();
                return;
            }
        }
        System.out.println("Produto nao encontrado.");
    }

    public void removerProduto(String nome) {
        produtos.removeIf(produto -> produto.getNome().equals(nome));
        logOperacao("Remover Produto: " + nome);
        salvarDados();
    }

    public void realizarVenda(String id, String nomeProduto, int quantidade) {
        Cliente cliente = clientes.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst()
                .orElse(null);

        Produto produto = produtos.stream()
                .filter(p -> p.getNome() != null && p.getNome().equals(nomeProduto))
                .findFirst()
                .orElse(null);

        if (cliente == null) {
            System.out.println("Cliente nao encontrado.");
            return;
        }

        if (produto == null) {
            System.out.println("Produto nao encontrado.");
            return;
        }

        if (produto.getQuantidade() < quantidade) {
            System.out.printf("O produto %s que esta a tentar vender nãa tem disponibilidade em stock ou a quantidade pretendida nao esta disponível, apenas tem %d.\n", nomeProduto, produto.getQuantidade());
            return;
        }

        double valorTotal = produto.getPreco() * quantidade * 0.17;  // Adicionando 17% de IVA
        Venda venda = new Venda(cliente, produto, quantidade, valorTotal);
        vendas.add(venda);
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produto.incrementarVendidos(quantidade);
        logOperacao("Realizar Venda: " + id + " comprou " + quantidade + "x " + nomeProduto + " por " + valorTotal);
        salvarDados();
        System.out.printf("Venda realizada com sucesso. Valor total: %.2f Mtn (incluindo IVA)\n", valorTotal);
    }

    public void exibirContaCorrente(String id) {
        Cliente cliente = clientes.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (cliente == null) {
            System.out.println("Cliente nao encontrado.");
            return;
        }

        System.out.println("Conta corrente do cliente " + cliente.getNome() + ":");
        vendas.stream()
                .filter(v -> v.getCliente().equals(cliente))
                .forEach(System.out::println);
    }

    public void emitirRelatorios() {
        System.out.println("Produtos abaixo de 5 unidades:");
        produtos.stream()
                .filter(p -> p.getQuantidade() < 5)
                .forEach(System.out::println);

        System.out.println("\nProdutos mais vendidos:");
        produtos.stream()
                .sorted(Comparator.comparingInt(Produto::getVendidos).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    private void logOperacao(String operacao) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("operacoes.log", true))) {
            bw.write(operacao);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarDados() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("farmacia.dat"))) {
            oos.writeObject(clientes);
            oos.writeObject(produtos);
            oos.writeObject(vendas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarDados() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("farmacia.dat"))) {
            clientes = (List<Cliente>) ois.readObject();
            produtos = (List<Produto>) ois.readObject();
            vendas = (List<Venda>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Arquivo não existe ou está vazio, iniciar com listas vazias
            clientes = new ArrayList<>();
            produtos = new ArrayList<>();
            vendas = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        Farmacia farmacia = new Farmacia();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Menu:");
            System.out.println("1. Clientes");
            System.out.println("2. Produtos");
            System.out.println("3. Inventarios");
            System.out.println("4. Vendas");
            System.out.println("5. Relatorios");
            System.out.println("6. Sair");
            System.out.print("Escolha uma opcao: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1:
                    System.out.println("1.1 Criar Cliente");
                    System.out.println("1.2 Atualizar Cliente");
                    System.out.println("1.3 Remover Cliente");
                    System.out.println("1.4 Ver Conta Corrente do Cliente");
                    System.out.print("Escolha uma opcao: ");
                    int opcaoCliente = scanner.nextInt();
                    scanner.nextLine();

                    if (opcaoCliente == 1) {
                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();
                        System.out.print("ID: ");
                        String id = scanner.nextLine();
                        Cliente cliente = new Cliente(nome, id);
                        farmacia.adicionarCliente(cliente);
                    } else if (opcaoCliente == 2) {
                        System.out.print("ID do cliente a ser atualizado: ");
                        String id = scanner.nextLine();
                        System.out.print("Novo nome: ");
                        String nome = scanner.nextLine();
                        System.out.print("Novo ID: ");
                        String novoId = scanner.nextLine();
                        Cliente novoCliente = new Cliente(nome, novoId);
                        farmacia.atualizarCliente(id, novoCliente);
                    } else if (opcaoCliente == 3) {
                        System.out.print("ID do cliente a ser removido: ");
                        String id = scanner.nextLine();
                        farmacia.removerCliente(id);
                    } else if (opcaoCliente == 4) {
                        System.out.print("ID do cliente: ");
                        String id = scanner.nextLine();
                        farmacia.exibirContaCorrente(id);
                    }
                    break;
                case 2:
                    System.out.println("2.1 Criar Produto");
                    System.out.println("2.2 Atualizar Produto");
                    System.out.println("2.3 Remover Produto");
                    System.out.print("Escolha uma opcao: ");
                    int opcaoProduto = scanner.nextInt();
                    scanner.nextLine();

                    if (opcaoProduto == 1) {
                        System.out.print("Nome: ");
                        String nome = scanner.nextLine();
                        System.out.print("Preço: ");
                        double preco = scanner.nextDouble();
                        System.out.print("Quantidade: ");
                        int quantidade = scanner.nextInt();
                        Produto produto = new Produto(nome, preco, quantidade);
                        farmacia.adicionarProduto(produto);
                    } else if (opcaoProduto == 2) {
                        System.out.print("Nome do produto a ser atualizado: ");
                        String nome = scanner.nextLine();
                        System.out.print("Novo nome: ");
                        String novoNome = scanner.nextLine();
                        System.out.print("Novo preço: ");
                        double preco = scanner.nextDouble();
                        System.out.print("Nova quantidade: ");
                        int quantidade = scanner.nextInt();
                        Produto novoProduto = new Produto(novoNome, preco, quantidade);
                        farmacia.atualizarProduto(nome, novoProduto);
                    } else if (opcaoProduto == 3) {
                        System.out.print("Nome do produto a ser removido: ");
                        String nome = scanner.nextLine();
                        farmacia.removerProduto(nome);
                    }
                    break;
                case 3:
                    System.out.println("Inventario:");
                    farmacia.produtos.forEach(System.out::println);
                    break;
                case 4:
                    System.out.print("ID do cliente: ");
                    String id = scanner.nextLine();
                    System.out.print("Nome do produto: ");
                    String nomeProduto = scanner.nextLine();
                    System.out.print("Quantidade: ");
                    int quantidade = scanner.nextInt();
                    farmacia.realizarVenda(id, nomeProduto, quantidade);
                    break;
                case 5:
                    farmacia.emitirRelatorios();
                    break;
                case 6:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        }
    }
}