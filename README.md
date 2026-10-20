# GAKKI STORE

E-commerce de instrumentos musicais. Projeto da disciplina de Laboratório de
Engenharia de Software (ADS - FATEC Mogi das Cruzes), implementado a partir do
Documento de Requisitos de Software (DRS) e da Especificação de Caso de Uso
**Gerenciar Venda Eletrônica**.

Back-end em Java/Spring Boot, front-end em React, PostgreSQL versionado por
Flyway e suíte de aceitação em Selenium.

---

## O que está implementado

| Módulo | Cobertura | Requisitos |
|---|---|---|
| **Cliente** | cadastro, endereços, cartões, senha isolada, inativação, consulta por filtros | RF0021 a RF0028, RN0021 a RN0027 |
| **Catálogo** | vitrine, busca e detalhe do instrumento | RF0011 |
| **Vendas - carrinho e compra** | carrinho, frete, endereço e cartão no checkout, pagamento com cartões e cupons, finalização | RF0031 a RF0038, RN0031, RN0033 a RN0036 |
| **Vendas - pagamento e entrega** | validação pela operadora, aprovação/reprovação, baixa em estoque, despacho e entrega | RF0039, RF0040, RF0053, RN0028, RN0037 a RN0040 |
| **Vendas - troca** | solicitação, autorização, notificação, recebimento, cupom de troca | RF0041 a RF0045, RF0054, RN0041 a RN0043, RN0046 |
| **Vendas - bloqueio do carrinho** | prazo parametrizado, expiração, itens retirados, compra desabilitada | RN0044, RN0045, RNF0042 |
| **Transversais** | tempo de resposta e log de transação | RNF0011, RNF0012 |

### O que ainda não está

- **RN0032** está parcial: a disponibilidade é revalidada na finalização e a
  compra é recusada com mensagem, mas não há ajuste automático de quantidade
  nem remoção do item indisponível com alerta.
- **RN0044** diverge em um ponto: a regra pede aviso 5 minutos antes de o prazo
  expirar; o carrinho mostra contagem regressiva contínua, vermelha no último
  minuto.
- **RF0042 não tem recusa de troca** — o DRS descreve apenas o caminho da
  autorização e não prevê estado nem regra para a negativa.
- Entrada de estoque, análise de vendas e recomendação por IA ainda respondem
  pelo mock do front (`src/api/mockApi.js`); não há endpoint Java para elas.
  As telas existem, mas os dados são de mentira.

---

## Stack

**Back-end** - Java 21, Spring Boot 3.5.16, Spring Web, Spring Data JPA
(Hibernate 6.6), Spring Security com JWT (jjwt), Bean Validation, Flyway 11.7,
PostgreSQL, Maven, Lombok.

**Front-end** - React 19, Vite, React Router, lucide-react.

**Testes** - JUnit 5, Mockito, Selenium 4.49 (Chrome).

---

## Estrutura

```
gakki-project-proguess/
├── backend/                 repositório Git próprio
│   ├── src/main/java/com/gakki/store/
│   │   ├── controller/      recebe a requisição e delega
│   │   ├── service/         regra de negócio e @Transactional
│   │   ├── repository/      Spring Data JPA
│   │   ├── domain/          entidades (cliente/, vendas/)
│   │   ├── dto/             records de request e response
│   │   ├── mapper/          conversão DTO ↔ entidade
│   │   ├── security/        filtro JWT, UserDetails
│   │   ├── auditoria/       log de transação (RNF0012)
│   │   └── exception/       @RestControllerAdvice, ProblemDetail
│   ├── src/main/resources/db/migration/   V1 a V16 + V999 (seed)
│   ├── src/test/java/.../e2e/             suíte Selenium
│   ├── scripts/             limpeza de dados de teste
│   ├── postman/             coleção da API
│   └── docker-compose.yml   PostgreSQL de desenvolvimento
└── frontend/                repositório Git próprio
    └── src/
        ├── pages/           telas ligadas a rotas
        ├── components/      peças reutilizáveis
        ├── context/         sessão, carrinho, favoritos
        ├── api/             client.js — único ponto de acesso ao servidor
        └── utils/
```

O fluxo é estritamente **Controller → Service → Repository → Entity**. O
controller nunca acessa repository nem carrega regra de negócio, entidade JPA
nunca trafega como corpo HTTP (sempre DTO) e `@Transactional` vive no service.

---

## Como rodar

### Pré-requisitos

- JDK 21
- Node 20 ou superior
- Docker (ou um PostgreSQL 16+ já instalado)
- Google Chrome, se for rodar a suíte Selenium

### 1. Banco

```bash
cd backend
docker compose up -d
```

Sobe um PostgreSQL em `localhost:5432`, base `gakki`, usuário e senha `gakki`.
Quem já tem PostgreSQL local pode pular e apontar o `.env` para ele.

### 2. Back-end

```bash
cd backend
cp .env.example .env     # defina JWT_SECRET
./mvnw spring-boot:run   # Windows: .\mvnw.cmd spring-boot:run
```

Sobe em `http://localhost:8080/api/v1`. O Flyway aplica as migrations e o seed
no primeiro start — não é preciso criar tabela nenhuma à mão.

A aplicação **não sobe sem `JWT_SECRET`**, e isso é de propósito: evita que uma
chave de desenvolvimento vaze para produção por esquecimento. Gere a sua com
`openssl rand -base64 48`.

### 3. Front-end

```bash
cd frontend
npm install
npm run dev
```

Sobe em `http://localhost:5173`. O `.env` é opcional: o `client.js` já aponta
para `http://localhost:8080/api/v1` por padrão.

### Entrar no sistema

A tela de login tem cartões de perfil — um clique entra. O seed cria:

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | `admin@gakkistore.com` | `Admin@2026` |
| Cliente | `cliente@gakkistore.com` | `Cliente@2026` |

---

## Variáveis de ambiente

| Variável | Padrão | Para que serve |
|---|---|---|
| `JWT_SECRET` | *(sem padrão)* | chave de assinatura do token; mínimo 32 bytes |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | `localhost:5432/gakki`, `gakki`, `gakki` | conexão com o banco |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | origem do front |
| `CARRINHO_PRAZO_BLOQUEIO` | `PT15M` | prazo de bloqueio do item no carrinho (RN0044), em ISO-8601 |
| `PAGAMENTO_DIGITOS_RECUSADOS` | `0000` | final de cartão que a operadora simulada recusa (RN0037) |
| `LOG_SQL` | `INFO` | `DEBUG` mostra o SQL do Hibernate |
| `VITE_API_URL` *(front)* | `http://localhost:8080/api/v1` | endereço da API |

**Para demonstrar a expiração do carrinho** sem esperar 15 minutos, suba o
back-end com `CARRINHO_PRAZO_BLOQUEIO=PT20S`.

**Para demonstrar uma compra reprovada**, cadastre um cartão terminado em
`0000` e finalize a compra com ele: a operadora simulada recusa.

---

## Banco de dados

O schema é versionado **exclusivamente por Flyway** — `ddl-auto` está em
`validate`, então o Hibernate confere o mapeamento contra o banco e recusa
subir se divergir. Nenhuma tabela é criada pela aplicação.

- `V1` a `V16` — estrutura
- `V999__seed_dev.sql` — massa de demonstração: 6 instrumentos (um com estoque
  zero, de propósito, para a RN0031 poder ser demonstrada), cliente com dois
  endereços e dois cartões, cupons de troca e promocional

Como o seed é `V999`, **toda migration estrutural nova entra fora de ordem**.
Por isso `spring.flyway.out-of-order` está ligado: é o preço da convenção.

Para limpar o que as execuções de teste deixaram para trás sem recriar o
schema:

```bash
psql -h localhost -U gakki -d gakki -f backend/scripts/limpar_dados_de_teste.sql
```

O script é idempotente e preserva o seed.

---

## Testes

```bash
cd backend
./mvnw test              # unitários; a suíte de interface fica de fora
./mvnw test -Pe2e        # suíte Selenium — exige back-end e front no ar
```

A suíte de aceitação tem **28 casos** e dirige o navegador de verdade, pelas
telas, como o enunciado exige demonstrar. Rode-a com o prazo padrão
(`PT15M`): com um prazo curto, um carrinho pode vencer sozinho no meio da
execução e derrubar um caso que não tem nada a ver.

| Classe | Cobre |
|---|---|
| `RF0031CarrinhoTest` | carrinho, quantidade, exclusão, estoque insuficiente |
| `RF0035EnderecoECartaoTest` | endereço e cartão, existentes e novos |
| `RF0037PagamentoTest` | dois cartões, cupom com cartão, cupom promocional |
| `RF0038FinalizacaoTest` | pedido nasce `EM_PROCESSAMENTO` e persiste |
| `RN0036CupomDeTrocoTest` | troco pela diferença e recusa de cupom desnecessário |
| `RN0037ValidacaoDePagamentoTest` | aprovação debita estoque, reprovação não |
| `RF0039DespachoEEntregaTest` | ciclo até `ENTREGUE` pelo painel do administrador |
| `RF0041TrocaTest` | troca da solicitação ao cupom, com notificação |
| `RN0044BloqueioDeCarrinhoTest` | contagem, expiração, itens retirados, compra bloqueada |
| `RequisitosNaoFuncionaisTest` | tempo de resposta e log de transação |

Dois atalhos existem só para **preparar e conferir** estado, nunca para
executar o fluxo sob teste: `ApiDeApoio` (HTTP) e `BancoDeApoio` (JDBC). É o
`BancoDeApoio` que adianta o relógio do carrinho — esperar 15 minutos não é
teste, é sorteio.

---

## Decisões que vale conhecer antes de ler o código

**O bloqueio de item é consequência, não coluna.** Um item está bloqueado
porque existe num carrinho cujo prazo não venceu, ou num pedido ainda
`EM_PROCESSAMENTO`. Um contador de "reservado" no instrumento criaria um
segundo lugar para a verdade, e os dois divergiriam no primeiro erro de fluxo.

**Item expirado sai do carrinho.** Vai para `item_retirado`, e com isso
`carrinho.getItens()` continua significando "itens ativos" em todo o código que
já existia — total, frete e finalização não precisaram ganhar filtro.

**A baixa em estoque é da aprovação, não da finalização.** Entre finalizar e a
operadora responder, a mercadoria fica bloqueada mas não debitada.

**Troca é entidade própria**, não mais um status no pedido: tem número, ciclo
de vida, valor e cupom próprios, e uma compra entregue pode gerar mais de uma.

**A fórmula do frete é nossa.** O DRS não define: R$ 15 + R$ 5 por peça
adicional + R$ 10 fora de SP. O valor mostrado na tela é prévia; o que entra no
pedido é recalculado no servidor.

**O log de transação é gravado depois do commit**, por evento de domínio, e não
por callback dentro do flush do Hibernate — assim um rollback não deixa log
órfão.

---

## Documentos do projeto
